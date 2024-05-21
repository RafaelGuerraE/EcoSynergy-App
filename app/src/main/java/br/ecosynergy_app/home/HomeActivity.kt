package br.ecosynergy_app.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import br.ecosynergy_app.R
import br.ecosynergy_app.home.homefragments.Analytics
import br.ecosynergy_app.home.homefragments.Home
import br.ecosynergy_app.home.homefragments.Teams
import br.ecosynergy_app.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavView)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navDrawerButton: CircleImageView = findViewById(R.id.navDrawerButton)
        val navview: NavigationView = findViewById(R.id.nav_view)

        val headerView = navview.getHeaderView(0)
        val btnLogout = headerView.findViewById<ImageButton>(R.id.btnlogout)

        replaceFragment(Home())

        bottomNavView.menu.findItem(R.id.home)?.isChecked = true
        bottomNavView.selectedItemId = R.id.home
        bottomNavView.menu.findItem(R.id.home)?.setIcon(R.drawable.baseline_home_24)

        btnLogout.setOnClickListener {
            logout()
        }

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.analytics -> {
                    replaceFragment(Analytics())
                    item.setIcon(R.drawable.baseline_analytics_24)
                    bottomNavView.menu.findItem(R.id.home)?.setIcon(R.drawable.outline_home_24) // Set unselected icon for other items
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
            drawerLayout.openDrawer(navview)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(navview)) {
                    drawerLayout.closeDrawer(navview)
                } else {
                    isEnabled = false
                    onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    private fun logout() {
        // Clear the login state in shared preferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Navigate to the LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
