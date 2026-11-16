package ro.student.smartparking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

class ParkingDatabase(context: Context) : SQLiteOpenHelper(context, "parking.db", null, 5) {

    // Android apelează această funcție la crearea bazei de date.
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE parkings (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL CHECK (length(trim(name)) > 0),
                capacity INTEGER NOT NULL CHECK (capacity > 0),
                available INTEGER CHECK (available BETWEEN 0 AND capacity)
            )
        """.trimIndent())
    }

    // Migrare pentru cei care au instalat deja varianta precedentă.
    // SQLiteOpenHelper execută migrarea într-o tranzacție.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion >= 2) {
            db.execSQL("ALTER TABLE parkings RENAME TO parkings_legacy")
            onCreate(db)
            val availableColumn = if (oldVersion >= 3) "available" else "NULL"
            db.execSQL("""
                INSERT INTO parkings (id, name, capacity, available)
                SELECT id, name, capacity, $availableColumn FROM parkings_legacy
            """.trimIndent())
        } else {
            onCreate(db)
        }
    }

    // CREATE — adaugă o parcare nouă.
    fun addParking(name: String, capacity: Int, available: Int) {
        val values = ContentValues().apply {
            put("name", name)
            put("capacity", capacity)
            put("available", available)
        }
        writableDatabase.insertOrThrow("parkings", null, values)
    }

    // READ — citește parcările pentru lista principală.
    fun getAllParkings(): List<Parking> {
        val parkings = mutableListOf<Parking>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, name, capacity, available FROM parkings ORDER BY name COLLATE NOCASE",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                parkings.add(
                    Parking(
                        id = it.getLong(0),
                        name = it.getString(1),
                        capacity = it.getInt(2),
                        available = if (it.isNull(3)) null else it.getInt(3)
                    )
                )
            }
        }
        return parkings
    }

    // UPDATE — modifică parcarea identificată prin id.
    fun updateParking(id: Long, name: String, capacity: Int, available: Int) {
        val values = ContentValues().apply {
            put("name", name)
            put("capacity", capacity)
            put("available", available)
        }
        val updatedRows = writableDatabase.update(
            "parkings", values, "id = ?", arrayOf(id.toString())
        )
        if (updatedRows != 1) {
            throw SQLiteException("Parcarea nu mai există")
        }
    }

    // DELETE — șterge parcarea identificată prin id.
    fun deleteParking(id: Long) {
        writableDatabase.delete("parkings", "id = ?", arrayOf(id.toString()))
    }
}
