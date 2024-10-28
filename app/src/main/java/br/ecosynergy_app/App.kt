package br.ecosynergy_app

import android.app.Application
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import java.util.concurrent.Executors

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val executor = Executors.newSingleThreadExecutor()
        val config = BundledEmojiCompatConfig(this, executor).apply {
            setReplaceAll(true)
        }
        EmojiCompat.init(config)
    }
}
