package com.playtube.protube.video.music.activities

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.playtube.protube.video.music.ads.AdUtils
import java.util.concurrent.atomic.AtomicBoolean
import com.playtube.protube.video.music.R

class ThanksActivity : AppCompatActivity() {

    var tv_exit_app: TextView? = null
    var tv_exit_later: TextView? = null
    var adContainer: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_thanks)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tv_exit_app = findViewById(R.id.tv_exit_app)
        tv_exit_later = findViewById(R.id.tv_exit_app)
        adContainer = findViewById(R.id.ad_container)

        val loadMediumREC = AtomicBoolean()

        AdUtils.LoadMrecExit(this@ThanksActivity, adContainer, loadMediumREC, "big")

        tv_exit_app?.setOnClickListener {
            finish()
        }

        tv_exit_later?.setOnClickListener {
            onBackPressed()
        }
    }
}
