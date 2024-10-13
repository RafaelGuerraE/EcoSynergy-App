package br.ecosynergy_app.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import br.ecosynergy_app.NotificationService
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.fragments.Home
import br.ecosynergy_app.home.fragments.Notifications
import br.ecosynergy_app.home.fragments.Teams
import br.ecosynergy_app.login.LoginActivity
import br.ecosynergy_app.readings.ReadingsViewModel
import br.ecosynergy_app.readings.ReadingsViewModelFactory
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.readings.ReadingsRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.user.User
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.teams.TeamsViewModel
import br.ecosynergy_app.teams.TeamsViewModelFactory
import br.ecosynergy_app.user.UserSettingsActivity
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.user.UserViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import de.hdodenhof.circleimageview.CircleImageView

class HomeActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var readingsViewModel: ReadingsViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navDrawerButton: CircleImageView
    private lateinit var navView: NavigationView
    private lateinit var btnTheme: ImageButton
    private lateinit var txtTerms: TextView

    private var accessToken: String = ""

    private lateinit var progressBar: ProgressBar

    private lateinit var loginSp: SharedPreferences
    private lateinit var themeSp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {


        themeSp = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        when (themeSp.getString("theme", "system")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }


        val notificationServiceIntent = Intent(this, NotificationService::class.java)
        startService(notificationServiceIntent)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val notificationClicked = intent.getBooleanExtra("NOTIFICATION_CLICKED", false)

        val userDao = AppDatabase.getDatabase(applicationContext).userDao()
        val userRepository = UserRepository(userDao)

        val teamsDao = AppDatabase.getDatabase(applicationContext).teamsDao()
        val teamsRepository = TeamsRepository(teamsDao)

        val readingsDao = AppDatabase.getDatabase(applicationContext).readingsDao()
        val readingsRepository = ReadingsRepository(readingsDao)

        val membersDao = AppDatabase.getDatabase(applicationContext).membersDao()
        val membersRepository = MembersRepository(membersDao)

        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService, userRepository))[UserViewModel::class.java]
        readingsViewModel = ViewModelProvider(this, ReadingsViewModelFactory(RetrofitClient.readingsService, readingsRepository))[ReadingsViewModel::class.java]
        teamsViewModel = ViewModelProvider(this, TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository, membersRepository))[TeamsViewModel::class.java]



        loginSp = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = loginSp.getBoolean("isLoggedIn", false)
        val justLoggedIn = loginSp.getBoolean("just_logged_in", false)
        val open = loginSp.getBoolean("open", false)

//        sectorsViewModel.getAllSectorsWithActivities().observe(this) { sectorsWithActivities ->
//            sectorsWithActivities?.let {
//                Log.d("Sectors", "Sectors with activities: $it")
//            }
//        }

        if(isLoggedIn && !open){
            updateUserInfo()
        }
        else{
            displayUserInfoFromDB{
                readingsViewModel.deleteAllReadingsFromDB()
                readingsViewModel.fetchMQ7ReadingsByTeamHandle("ecosynergyofc", accessToken)
                readingsViewModel.fetchMQ135ReadingsByTeamHandle("ecosynergyofc", accessToken)
                readingsViewModel.fetchFireReadingsByTeamHandle("ecosynergyofc", accessToken)
            }
            loginSp.edit().putBoolean("open", false).apply()
        }

        bottomNavView = findViewById(R.id.bottomNavView)
        drawerLayout  = findViewById(R.id.drawer_layout)
        navDrawerButton = findViewById(R.id.navDrawerButton)
        navView = findViewById(R.id.nav_view)
        btnTheme = findViewById(R.id.btnTheme)
        txtTerms = findViewById(R.id.txtTerms)

        progressBar = findViewById(R.id.progressBar)


        if(justLoggedIn){
            showSnackBar("Conectado com sucesso", "FECHAR", R.color.greenDark)
            loginSp.edit().putBoolean("just_logged_in", false).apply()
        }

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                btnTheme.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_dark_mode_24))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                btnTheme.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_sunny_24))
            }
        }

        val headerView = navView.getHeaderView(0)
        if (headerView == null) {
            Log.e("HomeActivity", "Header view is null")
        } else {
            val btnLogout = headerView.findViewById<ImageButton>(R.id.btnLogout)
            btnLogout?.setOnClickListener {
                logout()
            }
        }

        if (notificationClicked) {
            replaceFragment(Notifications())
            bottomNavView.menu.findItem(R.id.notifications)?.isChecked = true
            bottomNavView.selectedItemId = R.id.notifications
            bottomNavView.menu.findItem(R.id.notifications)?.setIcon(R.drawable.baseline_notifications_24)
        } else {
            replaceFragment(Home())
            bottomNavView.menu.findItem(R.id.home)?.isChecked = true
            bottomNavView.selectedItemId = R.id.home
            bottomNavView.menu.findItem(R.id.home)?.setIcon(R.drawable.baseline_home_24)
        }

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(Home())
                    item.setIcon(R.drawable.baseline_home_24)
                    bottomNavView.menu.findItem(R.id.teams)?.setIcon(R.drawable.outline_people_24)
                    bottomNavView.menu.findItem(R.id.notifications)?.setIcon(R.drawable.outline_notifications_24)
                }
                R.id.teams -> {
                    replaceFragment(Teams())
                    item.setIcon(R.drawable.baseline_people_24)
                    bottomNavView.menu.findItem(R.id.home)?.setIcon(R.drawable.outline_home_24)
                    bottomNavView.menu.findItem(R.id.notifications)?.setIcon(R.drawable.outline_notifications_24)
                }
                R.id.notifications-> {
                    replaceFragment(Notifications())
                    item.setIcon(R.drawable.baseline_notifications_24)
                    bottomNavView.menu.findItem(R.id.home)?.setIcon(R.drawable.outline_home_24)
                    bottomNavView.menu.findItem(R.id.teams)?.setIcon(R.drawable.outline_people_24)
                }
            }
            true
        }

        navDrawerButton.setOnClickListener {
            drawerLayout.openDrawer(navView)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(navView)) {
                    drawerLayout.closeDrawer(navView)
                } else {
                    isEnabled = false
                    onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.app_config -> {
                    val i = Intent(this, AppSettingsActivity::class.java)
                    startActivity(i)
                    true
                }
                R.id.account_config -> {
                    val i = Intent(this, UserSettingsActivity::class.java)
                    startActivity(i)
                    true
                }
                R.id.about -> {
                    val i = Intent(this, HelpActivity::class.java)
                    startActivity(i)
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawer(navView)
            }
        }

        btnTheme.setOnClickListener{ manageThemes()}

        txtTerms.setOnClickListener{
            val i = Intent(this, TermsActivity::class.java)
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        clearNavigationViewSelection(navView)
        displayUserInfoFromDB{}
    }

    private fun manageThemes(){
        val items = arrayOf("Padrão do Sistema", "Claro", "Escuro")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione o tema desejado")

        builder.setItems(items) { dialog, which ->
            val editor = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE).edit()

            when (which) {
                0 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    editor.putString("theme", "system")
                }
                1 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    editor.putString("theme", "light")
                }
                2 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    editor.putString("theme", "dark")
                }
            }
            editor.apply()
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun clearNavigationViewSelection(navView: NavigationView) {
        navView.menu.findItem(R.id.account_config)?.isChecked = false
        navView.menu.findItem(R.id.app_config)?.isChecked = false
        navView.menu.findItem(R.id.about)?.isChecked = false
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    private fun displayUserInfoFromDB(onComplete: () -> Unit) {
        userViewModel.getUserInfoFromDB(){
            userViewModel.userInfo.observe(this) { user ->
                if (user != null) {
                    Log.i("HomeActivity", "UserData displayed: $user")
                    updateNavigationHeader(navView, user)
                    accessToken = user.accessToken
                } else {
                    logout()
                }
                userViewModel.userInfo.removeObservers(this)
                onComplete()
            }
        }
    }


    private fun updateUserInfo() {
        userViewModel.getUserInfoFromDB{}

        userViewModel.userInfo.observe(this) { userInfo ->
            if (userInfo != null) {
                userViewModel.refreshToken(userInfo.username, userInfo.refreshToken)
                userViewModel.refreshResult.observe(this) { refreshResult ->
                    refreshResult.onSuccess { refreshResponse ->
                        userViewModel.user.observe(this) { result ->
                            result.onSuccess { userData ->
                                userViewModel.updateUserInfoDB(
                                    userData.id,
                                    userData.username,
                                    userData.fullName,
                                    userData.email,
                                    userData.gender,
                                    userData.nationality,
                                    refreshResponse.accessToken,
                                    refreshResponse.refreshToken
                                ){
                                    updateTeamInfo(userData.id, refreshResponse.accessToken)
                                    displayUserInfoFromDB{}

                                    readingsViewModel.deleteAllReadingsFromDB()
                                    readingsViewModel.fetchMQ7ReadingsByTeamHandle("ecosynergyofc", refreshResponse.accessToken)
                                    readingsViewModel.fetchMQ135ReadingsByTeamHandle("ecosynergyofc", accessToken)
                                    readingsViewModel.fetchFireReadingsByTeamHandle("ecosynergyofc", accessToken)
                                }

                                userViewModel.user.removeObservers(this)
                            }
                        }
                    }

                    refreshResult.onFailure {
                        Log.e("HomeActivity", "Failed to refresh token")
                        displayUserInfoFromDB {  }
                    }
                }
                userViewModel.userInfo.removeObservers(this)
            } else {
                Log.d("HomeActivity", "User info is null, cannot refresh token.")
                logout()
            }
        }
    }

    private fun updateTeamInfo(userId: Int, accessToken: String){
        teamsViewModel.deleteTeamsFromDB()
        teamsViewModel.getTeamsByUserId(userId, accessToken) {}
    }

    private fun logout() {
        val editTheme = themeSp.edit()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        editTheme.putString("theme", "system")
        editTheme.apply()

        val editLog = loginSp.edit()
        editLog.putBoolean("isLoggedIn", false)
        editLog.apply()

        userViewModel.deleteUserInfoFromDB()
        teamsViewModel.deleteTeamsFromDB()
        readingsViewModel.deleteAllReadingsFromDB()

        val i = Intent(this, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        i.putExtra("LOGOUT_MESSAGE", "You have been logged out successfully.")
        startActivity(i)
        finish()
    }

    fun getDrawableForLetter(letter: Char): Int {
        return when (letter.lowercaseChar()) {
            'a' -> R.drawable.a
            'b' -> R.drawable.b
            'c' -> R.drawable.c
            'd' -> R.drawable.d
            'e' -> R.drawable.e
            'f' -> R.drawable.f
            'g' -> R.drawable.g
            'h' -> R.drawable.h
            'i' -> R.drawable.i
            'j' -> R.drawable.j
            'k' -> R.drawable.k
            'l' -> R.drawable.l
            'm' -> R.drawable.m
            'n' -> R.drawable.n
            'o' -> R.drawable.o
            'p' -> R.drawable.p
            'q' -> R.drawable.q
            'r' -> R.drawable.r
            's' -> R.drawable.s
            't' -> R.drawable.t
            'u' -> R.drawable.u
            'v' -> R.drawable.v
            'w' -> R.drawable.w
            'x' -> R.drawable.x
            'y' -> R.drawable.y
            'z' -> R.drawable.z
            else -> R.drawable.default_image
        }
    }

    private fun updateNavigationHeader(navView: NavigationView, userData: User) {
        val headerView = navView.getHeaderView(0)
        val userPicture: CircleImageView = headerView.findViewById(R.id.userPicture)
        val lblUserFullname: TextView = headerView.findViewById(R.id.lblUserFullname)
        val lblUsername: TextView = headerView.findViewById(R.id.lblUsername)
        val lblUserEmail: TextView = headerView.findViewById(R.id.lblUserEmail)

        lblUserFullname.text = userData.fullName
        lblUsername.text = "@${userData.username}"
        lblUserEmail.text = userData.email
        val firstName = userData.fullName.split(" ").firstOrNull()
        if (!firstName.isNullOrEmpty()) {
            progressBar.visibility = View.GONE
            navDrawerButton.visibility = View.VISIBLE
            val firstLetter = firstName[0]
            val drawableId = getDrawableForLetter(firstLetter)
            userPicture.setImageResource(drawableId)
            navDrawerButton.setImageResource(drawableId)
        } else {
            progressBar.visibility = View.GONE
            navDrawerButton.visibility = View.VISIBLE
            showToast("ERRO: Imagem de Perfil")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSnackBar(message: String, action: String, bgTint: Int) {
        val rootView = findViewById<View>(android.R.id.content)
        val snackBar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
            .setAction(action) {}
        snackBar.setBackgroundTint(ContextCompat.getColor(this, bgTint))
        snackBar.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.white))

        snackBar.addCallback(object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar?) {
                super.onShown(sb)
                val snackbarView = snackBar.view
                val params = snackbarView.layoutParams as FrameLayout.LayoutParams
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, findViewById<View>(R.id.bottomNavView).height)
                snackbarView.layoutParams = params
            }
        })
        snackBar.show()
    }
}