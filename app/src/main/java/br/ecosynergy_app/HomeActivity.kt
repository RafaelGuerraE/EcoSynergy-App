package br.ecosynergy_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import br.ecosynergy_app.homefragments.Analytics
import br.ecosynergy_app.homefragments.Home
import br.ecosynergy_app.homefragments.Teams
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    //Variables


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavigationView: BottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        replaceFragment(Home())

        bottomNavigationView.menu.findItem(R.id.home)?.isChecked = true

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.analytics -> replaceFragment(Analytics())
                R.id.home -> replaceFragment(Home())
                R.id.teams -> replaceFragment(Teams())

                else ->{}
            }
            true
        }

    }

    fun replaceFragment(fragment : Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

}