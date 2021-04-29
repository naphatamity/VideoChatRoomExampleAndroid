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

        val videoChatIntent = Intent(this, com.videoment.videochatroom.MainActivity::class.java)
        videoChatIntent.putExtra(API_KEY, "apiKey")
        videoChatIntent.putExtra(CHANNEL_ID, "channelID")
        videoChatIntent.putExtra(USER_ID, "userId")
        videoChatIntent.putExtra(USER_NAME, "userName")
        videoChatIntent.putExtra(VIDEO_NAME, "videoName")
        videoChatIntent.putExtra(VIDEO_URL, "videoUrl")
        startActivity(videoChatIntent)
        this.finish()
    }
}