package com.jkpmediana.fleetnotes

// Komentar.kt
data class Komentar(
    val id: Int,
    val komentar: String,
    val datum: String,
    val kreirao: String?,
    val poslednji_modifikovao: String?,
    val poslednja_izmena: String?

)
data class NewComment(
    val broj_konta: String,
    val komentar: String,
    val korisnik: String
)

data class UpdatedComment(
    val komentar: String,
    val korisnik: String
)

data class ApiResponse(
    val status: String,
    val message: String
)
