package me.`when`.audio_recorder

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.*



const val REQUEST_CODE = 200

class MainActivity : AppCompatActivity(), Timer.onTimerTickListener {
    private var duration = ""
    private lateinit var amplitude: ArrayList<Float>
    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var filename = ""
    private var isRecording = false
    private var isPaused = false
    private lateinit var timer: Timer

    private lateinit var db : AppDatabase

    private lateinit var vibrator: Vibrator
    private var permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permissionsGranted = false
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionsGranted = ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionsGranted)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords"
        ).build()

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED


        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as  Vibrator

        btrecord.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isRecording -> pauseRecording()
                else -> startRecording()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
        btList.setOnClickListener{
         startActivity(Intent(this,GalleryActivity::class.java))
        }
        btDone.setOnClickListener{
           stopRecording()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBG.visibility = View.VISIBLE
            inputFileName.setText(filename)
            Toast.makeText(this,"Recording Saved to $dirPath$filename.mp3",Toast.LENGTH_SHORT).show()
        }

        btCancel.setOnClickListener{
            File("$dirPath$filename.mp3").delete()
            dismiss()
        }

        btDelete.setOnClickListener{
            stopRecording()
             File("$dirPath$filename.mp3").delete()
            Toast.makeText(this,"Recording Deleted",Toast.LENGTH_SHORT).show()
        }
        btDelete.isClickable =false
   btOK.setOnClickListener{
dismiss()
       save()
   }

bottomSheetBG.setOnClickListener{
    File("$dirPath$filename.mp3").delete()
    dismiss()
}

}

    private fun save() {
        val newFilename = inputFileName.text.toString()
        if (newFilename!= filename){
            var newFile = File("$dirPath$newFilename.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)
        }
        var filePath = "$dirPath$newFilename.mp3"
        var timestamp = Date().time
        var ampsPath = "$dirPath$newFilename"

        try {
            var fos = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fos)
            out.writeObject(amplitude)
            fos.close()
            out.close()
        }
        catch (e: IOException){}
        var Record = AudioRecord(newFilename,filePath,timestamp,duration,ampsPath)
        GlobalScope.launch {
        db.audioRecordDao().insert(Record)
        }
    }

    private fun dismiss(){
        bottomSheetBG.visibility = View.GONE
        hideKeyboard(inputFileName)

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        },100)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        recorder.pause()
        isPaused = true
        btrecord.setImageResource(R.drawable.ic_record)

        timer.pause()
    }

private fun hideKeyboard(view: View){
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken,0)
}

    @RequiresApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        recorder.resume()
        isRecording = true
        isPaused = false
        btrecord.setImageResource(R.drawable.ic_pause)

        timer.start()
    }
    private fun stopRecording(){
        timer.stop()
        recorder.apply {
            stop()
            release()
        }
        isPaused = false
        isRecording =false
        btList.visibility = View.VISIBLE
        btDone.visibility = View.GONE
        btDelete.isClickable = false
        btDelete.setImageResource(R.drawable.ic_delete_disabled)
        btrecord.setImageResource(R.drawable.ic_record)
        tvTimer.text = "00:00:00.00"
        amplitude = waveFormView.clear()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE)
            permissionsGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun startRecording() {
        if (!permissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }
        //Start Recording
        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"
        var simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        var date = simpleDateFormat.format(Date())
        filename = "AUD_${date}"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")

            try {
                prepare()
            } catch (e: IOException) {
            }
            start()
        }
        btrecord.setImageResource(R.drawable.ic_pause)
        isRecording = true
        isPaused = false

        timer.start()

        btDelete.isClickable=true
        btDelete.setImageResource(R.drawable.ic_delete)
        btList.visibility =View.GONE
        btDone.visibility = View.VISIBLE
    }

    override fun onTimerTick(duration: String) {
        tvTimer.text = duration
        this.duration = duration.dropLast(3)
        waveFormView.addAmplitude(recorder.maxAmplitude.toFloat())
    }


}