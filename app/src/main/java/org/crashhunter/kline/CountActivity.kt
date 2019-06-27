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

        rate1.addTextChangedListener {
            if (checkEmpty()) {
                countValue()
            }
        }

        rate2.addTextChangedListener {
            if (checkEmpty()) {
                countValue()
            }

        }

        numEdit.addTextChangedListener {
            if (checkEmpty()) {
                countValue()
            }
        }

    }

    private fun checkEmpty(): Boolean {
        if (numEdit.text.toString().isEmpty() || rate1.text.toString().isEmpty() || rate2.text.toString().isEmpty()) {
            return false
        } else {
            return true
        }

    }

    private fun countValue() {


        var num = numEdit.text.toString().toFloat()
        var rate1Num = rate1.text.toString().toFloat()
        var rate2Num = rate2.text.toString().toFloat()

        var upStr = ""
        var downStr = ""
        var lastUp = num
        var lastDown = num

        for (i in 0..5) {
            lastUp *= (1 + rate1Num / 100).toFloat()
            lastDown *= (1 - rate1Num / 100).toFloat()

            upStr += "+${(i + 1) * rate1Num}% : ${lastUp} \n"
            downStr += "-${(i + 1) * rate1Num}% : ${lastDown} \n"

        }

        var upStr5 = ""
        var downStr5 = ""
        var lastUp5 = num
        var lastDown5 = num

        for (i in 0..5) {
            lastUp5 *= (1 + rate2Num / 100).toFloat()
            lastDown5 *= (1 - rate2Num / 100).toFloat()
            upStr5 += "+${(i + 1) * rate2Num}% : ${lastUp5} \n"
            downStr5 += "-${(i + 1) * rate2Num}% : ${lastDown5} \n"

        }



        up.text = upStr
        down.text = downStr

        up5.text = upStr5
        down5.text = downStr5
    }
}
