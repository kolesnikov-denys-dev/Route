package com.task.test.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.task.test.R
import com.task.test.util.Constants.SPLASH_TIME_OUT
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        job = CoroutineScope(Dispatchers.Main).launch {
            delay(SPLASH_TIME_OUT)
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        job.cancel()
    }

}