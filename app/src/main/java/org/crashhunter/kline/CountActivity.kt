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


        high.addTextChangedListener {
            if (checkEmptyMid()) {
                countMidValue()
            }
        }

        low.addTextChangedListener {
            if (checkEmptyMid()) {
                countMidValue()
            }

        }

    }

    private fun checkEmptyMid(): Boolean {
        return !(high.text.toString().isEmpty() || low.text.toString().isEmpty())

    }

    private fun checkEmpty(): Boolean {
        return !(numEdit.text.toString().isEmpty() || rate1.text.toString().isEmpty() || rate2.text.toString().isEmpty())

    }


    private fun countMidValue() {


        var highNum = high.text.toString().toFloat()
        var lowNum = low.text.toString().toFloat()

        var period = highNum - lowNum
        var midNum = period / 2 + lowNum

        var a = period * 0.75 + lowNum
        var c = period * 0.25 + lowNum

        mid.text = "75%:$a \n50%:$midNum \n25%$c"
    }
    private fun countValue() {


        var num = numEdit.text.toString().toFloat()
        var rate1Num = rate1.text.toString().toFloat()
        var rate2Num = rate2.text.toString().toFloat()

        var upStr = ""
        var downStr = ""
        var lastUp = num
        var lastDown = num

        for (i in 1..6) {
            lastUp *= (1 + rate1Num / 100).toFloat()
            lastDown *= (1 - rate1Num / 100).toFloat()

            upStr += "+${i * rate1Num}% : ${lastUp} \n"
            downStr += "-${i * rate1Num}% : ${lastDown} \n"

        }

        var upStr5 = ""
        var downStr5 = ""
        var lastUp5 = num
        var lastDown5 = num

        for (i in 1..6) {
            lastUp5 = (1 + rate2Num * i / 100) * num
            lastDown5 = (1 - rate2Num * i / 100) * num
            upStr5 += "+${i * rate2Num}% : ${lastUp5} \n"
            downStr5 += "-${i * rate2Num}% : ${lastDown5} \n"

        }



        up.text = "复利：\n$upStr"
        down.text = downStr

        up5.text = "非复利：\n$upStr5"
        down5.text = downStr5
    }
}
