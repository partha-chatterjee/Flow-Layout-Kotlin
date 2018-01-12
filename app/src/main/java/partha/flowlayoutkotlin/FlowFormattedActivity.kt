package partha.flowlayoutkotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.util.*

class FlowFormattedActivity : AppCompatActivity() {

    lateinit var flow : FlowLayout
    lateinit var localCountry : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_formatted)

        initFields()
    }

    private fun initFields() {
        flow = findViewById(R.id.flow)
        fetchLocale()
    }

    private fun fetchLocale() {
        val locale = Locale.getAvailableLocales()
        localCountry = ArrayList<String>()
        for (l in locale){
            if (l.displayCountry != null && !l.displayCountry.contentEquals("")) {
                localCountry.add(l.displayCountry)
                addView(l.displayCountry, localCountry.size - 1)
            }
        }
    }

    private fun addView(name: String?, position: Int) {
        var itemView : View = LayoutInflater.from(this).inflate(R.layout.item_flow_layout, flow, false)
        var txtName : TextView = itemView.findViewById(R.id.txt_name)
        var imgCross : ImageView = itemView.findViewById(R.id.img_cross)
        txtName.setText(name)
        flow.addView(itemView)
        imgCross.setOnClickListener({
            flow.removeView(itemView)
            localCountry.removeAt(position)
        })
    }
}
