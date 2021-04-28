package com.videoment.newappvideo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    val API_KEY = "apiKey"
    val VIDEO_URL = "videoUrl"
    val VIDEO_NAME = "videoName"
    val CHANNEL_ID = "channelID"
    val USER_ID = "userId"
    val USER_NAME = "userName"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ii = Intent(this, com.videoment.videochatroom.MainActivity::class.java)
        ii.putExtra(API_KEY, "apiKey")
        ii.putExtra(CHANNEL_ID, "channelID")
        ii.putExtra(USER_ID, "userId")
        ii.putExtra(USER_NAME, "userName")
        ii.putExtra(VIDEO_NAME, "videoName")
        ii.putExtra(VIDEO_URL, "videoUrl")
        startActivity(ii)
        this.finish();

    }
}