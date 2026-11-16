package ro.student.smartparking

import android.app.Activity
import android.app.AlertDialog
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class ParkingEditorActivity : Activity() {
    private lateinit var database: ParkingDatabase
    private lateinit var nameInput: EditText
    private lateinit var capacityInput: EditText
    private lateinit var availableInput: EditText
    private var parkingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.parking_editor)

        database = ParkingDatabase(this)
        nameInput = findViewById(R.id.parking_name)
        capacityInput = findViewById(R.id.parking_capacity)
        availableInput = findViewById(R.id.parking_available)

        // -1 indică deschiderea formularului pentru o parcare nouă.
        val receivedId = intent.getLongExtra("parking_id", -1)
        parkingId = if (receivedId == -1L) null else receivedId

        findViewById<TextView>(R.id.editor_title).setText(
            if (parkingId == null) R.string.add_parking else R.string.edit_parking
        )

        // La rotire, Android restaurează automat câmpurile cu id.
        if (parkingId != null && savedInstanceState == null) {
            loadParking()
        }

        findViewById<Button>(R.id.save_parking).setOnClickListener {
            saveParking()
        }
        findViewById<Button>(R.id.cancel).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.delete_parking).apply {
            visibility = if (parkingId == null) View.GONE else View.VISIBLE
            setOnClickListener { confirmDelete() }
        }
    }

    private fun loadParking() {
        try {
            val parking = database.getAllParkings().firstOrNull { it.id == parkingId }
            if (parking == null) {
                Toast.makeText(this, R.string.parking_missing, Toast.LENGTH_LONG).show()
                finish()
                return
            }
            nameInput.setText(parking.name)
            capacityInput.setText(parking.capacity.toString())
            availableInput.setText(parking.available?.toString().orEmpty())
        } catch (_: SQLiteException) {
            Toast.makeText(this, R.string.read_error, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun saveParking() {
        val name = nameInput.text.toString().trim()
        val capacity = capacityInput.text.toString().toIntOrNull()
        val available = availableInput.text.toString().toIntOrNull()

        // Validăm datele înainte de salvare.
        if (name.isEmpty()) {
            nameInput.error = getString(R.string.name_error)
            return
        }
        if (capacity == null || capacity !in 1..99999) {
            capacityInput.error = getString(R.string.capacity_error)
            return
        }
        if (available == null || available !in 0..capacity) {
            availableInput.error = getString(R.string.available_error)
            return
        }

        try {
            val id = parkingId
            if (id == null) {
                database.addParking(name, capacity, available)
            } else {
                database.updateParking(id, name, capacity, available)
            }
            finish()
        } catch (_: SQLiteException) {
            Toast.makeText(this, R.string.save_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmDelete() {
        val id = parkingId ?: return
        AlertDialog.Builder(this)
            .setMessage(R.string.delete_confirmation)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                try {
                    database.deleteParking(id)
                    finish()
                } catch (_: SQLiteException) {
                    Toast.makeText(this, R.string.delete_error, Toast.LENGTH_LONG).show()
                }
            }
            .show()
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }
}
