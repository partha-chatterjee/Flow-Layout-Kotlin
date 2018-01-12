package partha.flowlayoutkotlin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    companion object {
        val SPLASH_TIMEOUT = 1500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initFields(SPLASH_TIMEOUT.toLong())
    }

    private fun initFields(timeout: Long) {
        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this, FlowFormattedActivity::class.java)
            startActivity(intent)
        }, timeout)
    }
}
