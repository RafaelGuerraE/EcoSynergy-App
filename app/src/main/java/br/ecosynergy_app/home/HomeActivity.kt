package br.ecosynergy_app.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import br.ecosynergy_app.R
import br.ecosynergy_app.home.homefragments.Analytics
import br.ecosynergy_app.home.homefragments.Home
import br.ecosynergy_app.home.homefragments.Teams
import br.ecosynergy_app.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView

class HomeActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavView)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navDrawerButton: CircleImageView = findViewById(R.id.navDrawerButton)
        val navView: NavigationView = findViewById(R.id.nav_view)


        val headerView = navView.getHeaderView(0)
        if (headerView == null) {
            Log.e("HomeActivity", "Header view is null")
        } else {
            val btnLogout = headerView.findViewById<ImageButton>(R.id.btnLogout)
            btnLogout?.setOnClickListener {
                logout()
            }
        }

        replaceFragment(Home())

        bottomNavView.menu.findItem(R.id.home)?.isChecked = true
        bottomNavView.selectedItemId = R.id.home
        bottomNavView.menu.findItem(R.id.home)?.setIcon(R.drawable.baseline_home_24)

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.analytics -> {
                    replaceFragment(Analytics())
                    item.setIcon(R.drawable.baseline_analytics_24)
                    bottomNavView.menu.findItem(R.id.home)?.setIcon(R.drawable.outline_home_24)
                    bottomNavView.menu.findItem(R.id.teams)?.setIcon(R.drawable.outline_people_24)
                }
                R.id.home -> {
                    replaceFragment(Home())
                    item.setIcon(R.drawable.baseline_home_24)
                    bottomNavView.menu.findItem(R.id.analytics)?.setIcon(R.drawable.outline_analytics_24)
                    bottomNavView.menu.findItem(R.id.teams)?.setIcon(R.drawable.outline_people_24)
                }
                R.id.teams -> {
                    replaceFragment(Teams())
                    item.setIcon(R.drawable.baseline_people_24)
                    bottomNavView.menu.findItem(R.id.analytics)?.setIcon(R.drawable.outline_analytics_24)
                    bottomNavView.menu.findItem(R.id.home)?.setIcon(R.drawable.outline_home_24)
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

        userViewModel.user.observe(this, Observer { user ->
            if (user != null) {
                updateNavigationHeader(navView, user)
            }
        })

        fetchUserData()

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
    }

    override fun onResume() {
        super.onResume()
        val navView: NavigationView = findViewById(R.id.nav_view)
        clearNavigationViewSelection(navView)
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

    private fun logout() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun fetchUserData() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val token = sharedPreferences.getString("accessToken", null)

        if (username != null && token != null) {
            userViewModel.fetchUserData(username, token)
        } else {
            showToast("Invalid username or Token")
            Log.e("HomeActivity", "Invalid username or token")
        }
    }

    private fun getDrawableForLetter(letter: Char): Int {
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

    private fun updateNavigationHeader(navView: NavigationView, user: Result<UserResponse>) {

        val navDrawerButton: CircleImageView = findViewById(R.id.navDrawerButton)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)

        progressBar.visibility = View.VISIBLE
        navDrawerButton.visibility = View.VISIBLE

        val headerView = navView.getHeaderView(0)

        user.onSuccess { userData ->
            headerView.findViewById<TextView>(R.id.lblUserFullname)?.text = userData.fullName
            headerView.findViewById<TextView>(R.id.lblUsername)?.text = "@${userData.userName}"
            headerView.findViewById<TextView>(R.id.lblUserEmail)?.text = userData.email
            val userPicture: CircleImageView = headerView.findViewById(R.id.userPicture)

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
                showToast("Profile Image Error")
            }
        }.onFailure { throwable ->
            Log.e("HomeActivity", "Error updating navigation header", throwable)
            showToast("Error updating navigation header")
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}