package br.ecosynergy_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        /*if (isLoggedIn()) {
            startHomeActivity()
            finish()
            return
        }*/

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnlogin: Button = findViewById(R.id.btnlogin)
        val txtentry: EditText = findViewById(R.id.txtemail)
        val txtpassword: EditText = findViewById(R.id.txtpassword)
        val btnregister: Button = findViewById(R.id.btnregister)

        val entry = txtentry.text.toString()
        val password = txtpassword.text.toString()

        btnlogin.setOnClickListener(){

            startHomeActivity()
            /*if (authenticateUser(entry, password)) {
                setLoggedIn(true)
                startHomeActivity()

                finish()
            } else {
                Toast.makeText(this, "Wrong Username/Email or Password", LENGTH_SHORT).show()
            }*/

        }

        btnregister.setOnClickListener(){
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)
        }
    }

    private fun authenticateUser(entry: String, password:String): Boolean {
        return entry == "GabrielBen" && password == "1234"
    }

    /*private fun isLoggedIn(): Boolean {
        // Check the stored logged in status
        // This could be checking SharedPreferences, local database, etc.
        // Return true if the user is logged in, false otherwise

    }*/

    private fun setLoggedIn(loggedIn: Boolean) {
        // Store the logged in status
        // This could be storing in SharedPreferences, local database, etc.
        loggedIn
    }

    private fun startHomeActivity() {
        // Start the Home activity
        val i = Intent(this, HomeActivity::class.java)
        startActivity(i)
    }
}



