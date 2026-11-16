package ro.student.smartparking

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var database: ParkingDatabase
    private lateinit var parkingList: ListView
    private var parkings = emptyList<Parking>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = ParkingDatabase(this)
        parkingList = findViewById(R.id.parking_list)
        parkingList.emptyView = findViewById<TextView>(R.id.empty_list)

        findViewById<Button>(R.id.add_parking).setOnClickListener {
            // Fără id, formularul creează o parcare.
            startActivity(Intent(this, ParkingEditorActivity::class.java))
        }

        parkingList.setOnItemClickListener { _, _, position, _ ->
            // Cu id, formularul editează parcarea selectată.
            val intent = Intent(this, ParkingEditorActivity::class.java)
            intent.putExtra("parking_id", parkings[position].id)
            startActivity(intent)
        }
    }

    // Lista se actualizează inclusiv când revenim de la adăugare sau editare.
    override fun onResume() {
        super.onResume()
        loadParkings()
    }

    private fun loadParkings() {
        try {
            parkings = database.getAllParkings()
            val rows = parkings.map { parking ->
                val status = parking.available?.let {
                    getString(R.string.availability, it, parking.capacity)
                } ?: getString(R.string.availability_unknown, parking.capacity)
                parking.name + "\n" + status
            }

            parkingList.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                rows
            )
            findViewById<TextView>(R.id.empty_list).setText(R.string.empty_list)
        } catch (_: SQLiteException) {
            findViewById<TextView>(R.id.empty_list).setText(R.string.read_error)
            Toast.makeText(this, R.string.read_error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }
}
