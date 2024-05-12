package br.ecosynergy_app

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import br.ecosynergy_app.login.LoginActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val iv_logo: ImageView = findViewById(R.id.iv_logo)

// Start fade-out animation for the logo
        iv_logo.animate()
            .alpha(0f) // Set alpha to 0 (fully transparent)
            .setDuration(1500) // Duration of the animation
            .withEndAction {
                // Create an Intent to start the LoginActivity
                val intent = Intent(this, LoginActivity::class.java)

                // Create an ActivityOptions object for the transition animation
                val options = ActivityOptions.makeSceneTransitionAnimation(this)

                // Start the LoginActivity with the specified options
                startActivity(intent, options.toBundle())

                // Finish the current activity (optional)
                finish()
    }
    }
}