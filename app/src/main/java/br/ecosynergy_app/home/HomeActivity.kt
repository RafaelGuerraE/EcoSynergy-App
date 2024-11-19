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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import br.ecosynergy_app.R
import br.ecosynergy_app.RetrofitClient
import br.ecosynergy_app.home.fragments.HomeFragment
import br.ecosynergy_app.home.fragments.NotificationsFragment
import br.ecosynergy_app.home.fragments.TeamsFragment
import br.ecosynergy_app.login.LoginActivity
import br.ecosynergy_app.readings.ReadingsViewModel
import br.ecosynergy_app.readings.ReadingsViewModelFactory
import br.ecosynergy_app.room.AppDatabase
import br.ecosynergy_app.room.invites.InvitesRepository
import br.ecosynergy_app.room.readings.ReadingsRepository
import br.ecosynergy_app.room.teams.MembersRepository
import br.ecosynergy_app.room.teams.TeamsRepository
import br.ecosynergy_app.room.user.User
import br.ecosynergy_app.room.user.UserRepository
import br.ecosynergy_app.teams.viewmodel.TeamsViewModel
import br.ecosynergy_app.teams.viewmodel.TeamsViewModelFactory
import br.ecosynergy_app.user.UserSettingsActivity
import br.ecosynergy_app.user.viewmodel.UserViewModel
import br.ecosynergy_app.user.viewmodel.UserViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import br.ecosynergy_app.room.notifications.NotificationsRepository

class HomeActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var readingsViewModel: ReadingsViewModel
    private lateinit var teamsViewModel: TeamsViewModel

    private lateinit var notificationsRepository: NotificationsRepository

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navDrawerButton: CircleImageView
    private lateinit var navView: NavigationView
    private lateinit var btnTheme: ImageButton
    private lateinit var txtTerms: TextView

    private lateinit var userSettingsLauncher: ActivityResultLauncher<Intent>

    private var accessToken: String = ""
    private var listTeamHandles: List<String> = listOf()
    private var teamHandlesJob: Job? = null
    private var isTeamHandlesFetched = false

    private var userId: Int = 0

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

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestNotificationPermission()
        }

        val notificationClicked = intent.getBooleanExtra("NOTIFICATION_CLICKED", false)

        val userRepository = UserRepository(AppDatabase.getDatabase(applicationContext).userDao())
        notificationsRepository =
            NotificationsRepository(AppDatabase.getDatabase(this).notificationsDao())

        val teamsRepository =
            TeamsRepository(AppDatabase.getDatabase(applicationContext).teamsDao())

        userSettingsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data?.getBooleanExtra("USERNAME_CHANGED", false) == true) {
                        logout(false)
                    }
                    if (data?.getBooleanExtra("USER_DELETED", false) == true) {
                        logout(true)
                    }
                }
            }

        val readingsRepository = ReadingsRepository(
            AppDatabase.getDatabase(applicationContext).mq7ReadingsDao(),
            AppDatabase.getDatabase(applicationContext).mq135ReadingsDao(),
            AppDatabase.getDatabase(applicationContext).fireReadingsDao()
        )

        val membersRepository =
            MembersRepository(AppDatabase.getDatabase(applicationContext).membersDao())
        val invitesRepository =
            InvitesRepository(AppDatabase.getDatabase(applicationContext).invitesDao())

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(RetrofitClient.userService, userRepository)
        )[UserViewModel::class.java]
        readingsViewModel = ViewModelProvider(
            this,
            ReadingsViewModelFactory(RetrofitClient.readingsService, readingsRepository)
        )[ReadingsViewModel::class.java]
        teamsViewModel = ViewModelProvider(
            this,
            TeamsViewModelFactory(
                RetrofitClient.teamsService,
                teamsRepository,
                RetrofitClient.invitesService,
                membersRepository,
                invitesRepository
            )
        )[TeamsViewModel::class.java]

        loginSp = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = loginSp.getBoolean("isLoggedIn", false)
        val justLoggedIn = loginSp.getBoolean("just_logged_in", false)
        val open = loginSp.getBoolean("open", false)

        if (isLoggedIn && !open) {
            updateUserInfo {
                userViewModel.getUserInfoFromDB {
                    accessToken = userViewModel.userInfo.value?.accessToken ?: ""
                    retrieveAndSendFcmToken(accessToken)
                }
            }
        } else {
            loginSp.edit().putBoolean("open", false).apply()

            userViewModel.getUserInfoFromDB {
                accessToken = userViewModel.userInfo.value?.accessToken ?: ""
                retrieveAndSendFcmToken(accessToken)
            }
        }

        bottomNavView = findViewById(R.id.bottomNavView)
        drawerLayout = findViewById(R.id.drawer_layout)
        navDrawerButton = findViewById(R.id.navDrawerButton)
        navView = findViewById(R.id.nav_view)
        btnTheme = findViewById(R.id.btnTheme)
        txtTerms = findViewById(R.id.txtTerms)

        progressBar = findViewById(R.id.progressBar)


        if (justLoggedIn) {
            showSnackBar("Conectado com sucesso", "FECHAR", R.color.greenDark)
            loginSp.edit().putBoolean("just_logged_in", false).apply()
        }

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                btnTheme.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_dark))
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                btnTheme.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_light))
            }
        }


        val headerView = navView.getHeaderView(0)
        if (headerView == null) {
            Log.e("HomeActivity", "Header view is null")
        } else {
            val btnLogout = headerView.findViewById<ImageButton>(R.id.btnLogout)
            btnLogout?.setOnClickListener {
                logout(false)
            }
        }

        if (notificationClicked) {
            replaceFragment(NotificationsFragment())
            bottomNavView.menu.findItem(R.id.notifications)?.isChecked = true
            bottomNavView.selectedItemId = R.id.notifications
            bottomNavView.menu.findItem(R.id.notifications)?.setIcon(R.drawable.ic_notification_filled)
        } else {
            replaceFragment(HomeFragment())
            bottomNavView.menu.findItem(R.id.home)?.isChecked = true
            bottomNavView.selectedItemId = R.id.home
            bottomNavView.menu.findItem(R.id.home)?.setIcon(R.drawable.ic_homefull)
        }

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    item.setIcon(R.drawable.ic_homefull)
                    bottomNavView.menu.findItem(R.id.teams)?.setIcon(R.drawable.ic_teams)
                    bottomNavView.menu.findItem(R.id.notifications)
                        ?.setIcon(R.drawable.ic_notification)
                }

                R.id.teams -> {
                    replaceFragment(TeamsFragment())
                    item.setIcon(R.drawable.ic_teams_filled)
                    bottomNavView.menu.findItem(R.id.home)?.setIcon(R.drawable.ic_home)
                    bottomNavView.menu.findItem(R.id.notifications)
                        ?.setIcon(R.drawable.ic_notification)
                }

                R.id.notifications -> {
                    replaceFragment(NotificationsFragment())
                    item.setIcon(R.drawable.ic_notification_filled)
                    bottomNavView.menu.findItem(R.id.home)?.setIcon(R.drawable.ic_home)
                    bottomNavView.menu.findItem(R.id.teams)?.setIcon(R.drawable.ic_teams)
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
                    onBackPressedDispatcher.onBackPressed()
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
                    userSettingsLauncher.launch(i)
                    true
                }

                R.id.about -> {
                    val i = Intent(this, AboutActivity::class.java)
                    startActivity(i)
                    true
                }

                else -> false
            }.also {
                drawerLayout.closeDrawer(navView)
            }
        }

        btnTheme.setOnClickListener { manageThemes() }

        txtTerms.setOnClickListener {
            val i = Intent(this, TermsActivity::class.java)
            startActivity(i)
        }

        observeUserInfoFromDB {
            getTeamHandles {
                fetchReadingsData(listTeamHandles, accessToken)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        clearNavigationViewSelection(navView)
        userViewModel.getUserInfoFromDB {}
    }

    private fun manageThemes() {
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

    private fun observeUserInfoFromDB(onComplete: () -> Unit) {
        userViewModel.userInfo.observe(this) { user ->
            if (user != null) {
                Log.i("HomeActivity", "UserData displayed: $user")
                updateNavigationHeader(navView, user)
                accessToken = user.accessToken
            } else {
                logout(false)
            }
        }

        onComplete()
    }

    private fun updateUserInfo(onComplete: () -> Unit) {
        userViewModel.getUserInfoFromDB {

            val userInfo: User = userViewModel.userInfo.value!!

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
                            ) {
                                updateTeamInfo(userData.id, refreshResponse.accessToken)
                            }

                            userViewModel.user.removeObservers(this)
                        }
                    }
                }

                refreshResult.onFailure {
                    Log.e("HomeActivity", "Failed to refresh token")
                    userViewModel.getUserInfoFromDB {}
                }

                userViewModel.refreshResult.removeObservers(this)
            }
        }
        onComplete()
    }

    private fun updateTeamInfo(userId: Int, accessToken: String) {
        teamsViewModel.getTeamsByUserId(userId, accessToken) {}
    }

    private fun getTeamHandles(onComplete: () -> Unit) {
        if (isTeamHandlesFetched) {
            onComplete()
            return
        }

        teamHandlesJob = lifecycleScope.launch {
            teamsViewModel.getAllTeamsFromDB().collect { teamData ->
                listTeamHandles = teamData.map { it.handle }
                isTeamHandlesFetched = true

                val teamIds = teamData.map { it.id }
                for (id in teamIds) {
                    teamsViewModel.findInvitesByTeam(id, accessToken)
                }

                onComplete()
            }
        }
    }

    private fun fetchReadingsData(listTeamHandles: List<String>, accessToken: String) {
        readingsViewModel.fetchAllReadings(listTeamHandles,accessToken){}
    }

    private fun logout(wasDeleted: Boolean) {
        val editTheme = themeSp.edit()
        editTheme.putString("theme", "system")
        editTheme.apply()

        val editLog = loginSp.edit()
        editLog.putBoolean("isLoggedIn", false)
        editLog.apply()

        if (!wasDeleted) {
            userViewModel.removeFCMToken(userId, accessToken)
        }

        userViewModel.deleteUserInfoFromDB {
            CoroutineScope(Dispatchers.Main).launch {
                notificationsRepository.deleteAllNotifications()
            }

            teamsViewModel.deleteTeamsFromDB{
                val i = Intent(this, LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                i.putExtra("LOGOUT_MESSAGE", "You have been logged out successfully.")
                startActivity(i)
                finish()
            }
        }
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
            else -> R.drawable.ic_default
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
                params.setMargins(
                    params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    findViewById<View>(R.id.bottomNavView).height
                )
                snackbarView.layoutParams = params
            }
        })
        snackBar.show()
    }

    private fun retrieveAndSendFcmToken(accessToken: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    if (fcmToken != null) {
                        sendFcmTokenToServer(fcmToken,accessToken)
                    }
                } else {
                    Log.e("HomeActivity", "Erro ao obter o token FCM: ${task.exception}")
                }
            }
    }

    private fun sendFcmTokenToServer(fcmToken: String, accessToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userId = userViewModel.userRepository.getUserId()
                userViewModel.saveOrUpdateFcmToken(accessToken, userId, fcmToken) {
                    val fcmRequest = userViewModel.fcmRequest.value
                    if (fcmRequest != null) {
                        if (fcmRequest.isSuccessful) {
                            Log.d("HomeActivity", "UserID: $userId, FCMToken: $fcmToken")
                        } else {
                            Log.e("HomeActivity","Erro ao Enviar ao Back")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Erro ao enviar token FCM para o servidor", e)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("HomeActivity", "Notification permission granted")
            } else {
                Log.d("HomeActivity", "Notification permission denied")
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}