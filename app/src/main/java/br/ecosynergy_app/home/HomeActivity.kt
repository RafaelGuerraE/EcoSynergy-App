package br.ecosynergy_app.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.fragments.Home
import br.ecosynergy_app.home.fragments.Notifications
import br.ecosynergy_app.home.fragments.Teams
import br.ecosynergy_app.login.LoginActivity
import br.ecosynergy_app.login.LoginRequest
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.User
import br.ecosynergy_app.room.UserRepository
import br.ecosynergy_app.readings.ReadingsViewModel
import br.ecosynergy_app.readings.ReadingsViewModelFactory
import br.ecosynergy_app.room.ReadingsRepository
import br.ecosynergy_app.room.TeamsRepository
import br.ecosynergy_app.teams.TeamsViewModel
import br.ecosynergy_app.teams.TeamsViewModelFactory
import br.ecosynergy_app.user.UserSettingsActivity
import br.ecosynergy_app.user.UserViewModel
import br.ecosynergy_app.user.UserViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView

import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class HomeActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var sensorsViewModel: ReadingsViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private var token: String? = ""
    private var identifier: String? = ""
    private var userId: String = ""

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navDrawerButton: CircleImageView
    private lateinit var navView: NavigationView
    private lateinit var btnTheme: ImageButton

    private lateinit var progressBar: ProgressBar

    var teamHandles: List<String> =  emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreferences: SharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        when (sharedPreferences.getString("theme", "system")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

//        val notificationServiceIntent = Intent(this, NotificationService::class.java)
//        startService(notificationServiceIntent)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val notificationClicked = intent.getBooleanExtra("NOTIFICATION_CLICKED", false)

        val userDao = AppDatabase.getDatabase(applicationContext).userDao()
        val userRepository = UserRepository(userDao)

        val teamsDao = AppDatabase.getDatabase(applicationContext).teamsDao()
        val teamsRepository = TeamsRepository(teamsDao)

        val readingsDao = AppDatabase.getDatabase(applicationContext).readingsDao()
        val readingsRepository = ReadingsRepository(readingsDao)

        userViewModel = ViewModelProvider(this, UserViewModelFactory(RetrofitClient.userService, userRepository))[UserViewModel::class.java]
        sensorsViewModel = ViewModelProvider(this, ReadingsViewModelFactory(RetrofitClient.readingsService, readingsRepository))[ReadingsViewModel::class.java]
        teamsViewModel = ViewModelProvider(this, TeamsViewModelFactory(RetrofitClient.teamsService, teamsRepository))[TeamsViewModel::class.java]

        val sp: SharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sp.getBoolean("isLoggedIn", false)
        val justLoggedIn = sp.getBoolean("just_logged_in", false)

        if(isLoggedIn && !justLoggedIn){

            getUserInfoFromDB()
        }

        bottomNavView = findViewById(R.id.bottomNavView)
        drawerLayout  = findViewById(R.id.drawer_layout)
        navDrawerButton = findViewById(R.id.navDrawerButton)
        navView = findViewById(R.id.nav_view)
        btnTheme = findViewById(R.id.btnTheme)

        progressBar = findViewById(R.id.progressBar)

        getUserInfoFromDB()

        if (justLoggedIn) {
            showSnackBar("Conectado com sucesso", "FECHAR", R.color.greenDark)
            val editor = sp.edit()
            editor.putBoolean("just_logged_in", false)
            editor.apply()
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
                R.id.account_config -> {
                    val i = Intent(this, UserSettingsActivity::class.java)
                    startActivity(i)
                    true
                }
                R.id.app_config -> {
                    val i = Intent(this, AppSettingsActivity::class.java)
                    startActivity(i)
                    true
                }
                R.id.terms -> {
                    val i = Intent(this, TermsActivity::class.java)
                    startActivity(i)
                    true
                }
                R.id.notifications -> {
                    val i = Intent(this, NotificationsActivity::class.java)
                    startActivity(i)
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawer(navView)
            }
        }

        btnTheme.setOnClickListener{
            manageThemes()
        }
    }

    override fun onResume() {
        super.onResume()
        clearNavigationViewSelection(navView)
        //getUserInfo()
    }

    private fun manageThemes(){
        val items = arrayOf("Padrão do Sistema", "Claro", "Escuro")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione o tema desejado")

        builder.setItems(items) { dialog, which ->
            val sp: SharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            val editor = sp.edit()

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
        navView.menu.findItem(R.id.terms)?.isChecked = false
        navView.menu.findItem(R.id.notifications)?.isChecked = false
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    private fun getUserInfoFromDB() {
        userViewModel.getUserInfoFromDB()

            userViewModel.userInfo.observe(this) { user ->
                Log.d("HomeActivity", "User data retrieved: $user")
                if (user != null) {
                    updateNavigationHeader(navView, user)
                } else {
                    showToast("No user found")
                }
            }

    }

    private fun requestUserInfo(username: String, password: String){

        val loginRequest = LoginRequest(username, password)

        userViewModel.loginUser(loginRequest)
        userViewModel.loginResult.observe(this) { result ->
            result.onSuccess { loginResponse ->
                Log.d("LoginActivity", "Login success")
                userViewModel.user.observe(this){result->
                    result.onSuccess { userData->
                        getTeamsByUserId(userData.id, loginResponse.accessToken)
                    }
                }
            }.onFailure { error ->
                error.printStackTrace()
                Log.d("LoginActivity", "Login failed: ${error.message}")
            }
        }
    }

    private fun getTeamsByUserId(userId: Int, token: String){
        teamsViewModel.findTeamsByUserId(userId, token)
    }

    private fun logout() {
        val spTheme: SharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val editTheme = spTheme.edit()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        editTheme.putString("theme", "system")
        editTheme.apply()

        val spLog = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val editLog = spLog.edit()
        editLog.putBoolean("isLoggedIn", false)
        editLog.apply()

        userViewModel.deleteUserInfoFromDB()
        teamsViewModel.deleteTeamsFromDB()

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