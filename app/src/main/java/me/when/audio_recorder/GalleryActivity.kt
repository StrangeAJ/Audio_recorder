package me.`when`.audio_recorder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.`when`.audio_recorder.Adapter

class GalleryActivity : AppCompatActivity(), OnItemClickListener {
    private lateinit var records : ArrayList<AudioRecord>
    private lateinit var mAdapter : Adapter
    private lateinit var db : AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        records = ArrayList()
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords"
        ).build()
        mAdapter = Adapter(records,this)
        recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)

        }

        fetchAll()
}
private fun fetchAll(){
    GlobalScope.launch {
        records.clear()
        var queryResult = db.audioRecordDao().getAll()
        records.addAll(queryResult)

    }
    mAdapter.notifyDataSetChanged()
}

    override fun onItemClickListner(position: Int) {
        Toast.makeText(this, "Item Simple Clicked : $position", Toast.LENGTH_SHORT).show()
    }

    override fun onItemLongClickListenr(position: Int) {
        Toast.makeText(this, "Item Long Clicked : $position",Toast.LENGTH_SHORT).show()
    }
}
