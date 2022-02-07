package me.`when`.audio_recorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveFormView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var paint = Paint()
    private var amplitude = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()
    private var radius = 0f
    private var w = 5f
    private var d = 0f
    private var sw =0f
    private  var sh = 400f
    private  var maxSpikes = 0


    init {
        paint.color = Color.rgb(81,244,30)
        sw = resources.displayMetrics.widthPixels.toFloat()
        maxSpikes = (sw/(w+d)).toInt()
    }
    fun addAmplitude(amp: Float){
        var norm = Math.min((amp.toInt()/7),400).toFloat()
        amplitude.add(norm)
        spikes.clear()
        var amps = amplitude.takeLast(maxSpikes)
        for (i in amps.indices){
        var left = sw -  i*(w+d)
        var top = sh/2 - amps[i]/2
        var right = left + w
        var bottom = top +  amps[i]
            spikes.add(RectF(left,top,right,bottom))
        }

        invalidate()
    }
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        spikes.forEach{
            canvas?.drawRoundRect(it, radius,radius,paint)
        }
    }
    fun clear(): ArrayList<Float>{
        var amps = amplitude.clone() as ArrayList<Float>
        amplitude.clear()
        spikes.clear()
        invalidate()
        return amps
    }
}