package ro.student.smartparking

// Datele unei parcări. null înseamnă că disponibilitatea nu a fost completată.
data class Parking(
    val id: Long,
    val name: String,
    val capacity: Int,
    val available: Int?
)
