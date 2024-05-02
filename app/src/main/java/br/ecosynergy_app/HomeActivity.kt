package br.ecosynergy_app
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import br.ecosynergy_app.homefragments.Analytics
import br.ecosynergy_app.homefragments.Home
import br.ecosynergy_app.homefragments.Teams
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavView)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navDrawerButton: ImageButton = findViewById(R.id.navDrawerButton)
        val navview: NavigationView = findViewById(R.id.nav_view)

        val headerView = navview.getHeaderView(0)


        val btnlogout = headerView.findViewById<ImageButton>(R.id.btnlogout)

        replaceFragment(Home())

        bottomNavView.menu.findItem(R.id.home)?.isChecked = true

        btnlogout.setOnClickListener(){
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        }

        bottomNavView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.analytics -> replaceFragment(Analytics())
                R.id.home -> replaceFragment(Home())
                R.id.teams -> replaceFragment(Teams())
                else ->{}
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

    private fun replaceFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}
