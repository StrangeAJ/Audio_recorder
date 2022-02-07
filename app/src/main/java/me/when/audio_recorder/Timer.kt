package me.`when`.audio_recorder

import android.os.Handler
import android.os.Looper

class Timer(listener: onTimerTickListener) {
    interface onTimerTickListener{
        fun onTimerTick(duration: String)

    }
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private  var duration = 0L
    private var delay = 50L
    init {
        runnable = Runnable {
            duration += delay
            handler.postDelayed(runnable, delay)
            listener.onTimerTick(format())
        }
    }
    fun start(){
        handler.postDelayed(runnable, delay)
    }
    fun pause(){
        handler.removeCallbacks(runnable)
    }
    fun stop(){
        handler.removeCallbacks(runnable)
        duration = 0L
    }
    private fun format(): String{
         val millis  = duration%1000
        val secs = (duration/1000)%60
        val minutes = (duration/(1000*60))%60
        val hours = (duration/(1000*60*60))
        val formatted : String =
            if(hours>0)
            "%02d:%02d:%02d.%02d".format(hours,minutes,secs,millis/10)
            else
                "%02d:%02d.%02d".format(minutes,secs,millis/10)
        return formatted
    }
}