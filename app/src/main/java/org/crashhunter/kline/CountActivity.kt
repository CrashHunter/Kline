package org.crashhunter.kline

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.activity_count.*


class CountActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        supportActionBar?.hide()

        setContentView(R.layout.activity_count)


        initAction()


    }

    private fun initAction() {

        numEdit.addTextChangedListener {

            if (it.toString().isEmpty()) {
                return@addTextChangedListener
            }

            var num = it.toString().toFloat()


            var upStr = ""
            var downStr = ""
            var lastUp = num
            var lastDown = num

            for (i in 0..5) {
                lastUp *= 1.1f
                upStr += "+${(i + 1) * 10}% : ${lastUp} \n"
                lastDown *= 0.9f
                downStr += "-${(i + 1) * 10}% : ${lastDown} \n"

            }


            up.text = upStr
            down.text = downStr
        }

    }
}
