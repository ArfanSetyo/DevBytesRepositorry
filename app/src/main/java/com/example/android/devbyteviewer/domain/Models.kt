package com.example.android.devbyteviewer.domain

import com.example.android.devbyteviewer.util.smartTruncate

/**
 * Objek domain adalah class data Kotlin biasa yang mewakili hal-hal di aplikasi kita. Ini adalah
 * objek yang harus ditampilkan di layar, atau dimanipulasi oleh aplikasi.
 *
 * @lihat database untuk objek yang dipetakan ke database
 * @lihat jaringan untuk objek yang mengurai atau menyiapkan panggilan jaringan
 */

/**
 * Video mewakili devbyte yang dapat diputar.
 */
data class DevByteVideo(val title: String,
                        val description: String,
                        val url: String,
                        val updated: String,
                        val thumbnail: String) {

    /**
     * Deskripsi singkat digunakan untuk menampilkan deskripsi terpotong di UI
     */
    val shortDescription: String
        get() = description.smartTruncate(200)
}