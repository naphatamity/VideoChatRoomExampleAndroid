package com.videoment.videochatroom

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ekoapp.ekosdk.EkoClient
import com.videoment.videochatroom.chatfragment.ChatHomeFragment
import com.videoment.videochatroom.videoplayer.HomeVideoFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

class MainActivity : AppCompatActivity(R.layout.video_comment) {

    val VIDEO_URL = "videoUrl"
    val VIDEO_NAME = "videoName"
    val VIDEO_COUNT = "videoCount"
    val CHANNEL_ID = "channelID"
    val USER_ID = "userId"
    val USER_NAME = "userName"
    val PROFILE_IMAGE = "profileImage"
    val API_KEY = "apiKey"

    var apikey = ""
    var chID = ""
    var userID = ""
    var userName = ""
    var videoURL = ""
    var videoName = ""
    var videoCount = 0
    lateinit var ekoDisposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        videoURL = intent.getStringExtra(VIDEO_URL) ?: ""
        apikey = intent.getStringExtra(API_KEY) ?: ""
        chID = intent.getStringExtra(CHANNEL_ID) ?: ""
        userID = intent.getStringExtra(USER_ID) ?: ""
        userName = intent.getStringExtra(USER_NAME) ?: ""
        videoName = intent.getStringExtra(VIDEO_NAME) ?: ""

        initEkoClient()
        initChatChannel()
        initFragment(savedInstanceState)

    }

    private fun initEkoClient() {
        EkoClient.setup(apikey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

        EkoClient.registerDevice(userID)
            .displayName(userName)
            .build()
            .submit()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        val channelObject = JSONObject()
        channelObject.put(VIDEO_URL, videoURL)
        channelObject.put(VIDEO_NAME, videoName)
        channelObject.put(VIDEO_COUNT, videoCount)
        channelObject.put(CHANNEL_ID, chID)

        if (savedInstanceState == null) {
            val chatFrag = newInstance(channelObject, ChatHomeFragment())

            addFragment(chatFrag, R.id.chatFragment)

            val videoFragment = newInstance(channelObject, HomeVideoFragment())
            addFragment(videoFragment, R.id.videoFragment)

        }
    }

    private fun initChatChannel() {
        val sharedPref = getPreferences(MODE_PRIVATE) ?: return
        val uuid = sharedPref.getString(USER_ID, null)
        if (uuid == null) {
            ekoDisposable = EkoClient.getCurrentUser().subscribe {
                with(sharedPref.edit()) {
                    putString(PROFILE_IMAGE, it.getAvatar()?.getUrl())
                    apply()
                }
                with(sharedPref.edit()) {
                    putString(USER_ID, it.getUserId())
                    apply()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getOnOpenPip() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val params = PictureInPictureParams.Builder()
            enterPictureInPictureMode(params.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        getOnOpenPip()
    }

    private fun newInstance(channelObject: JSONObject, frag: Fragment): Fragment {
        val args = Bundle()
        args.putString(VIDEO_URL, channelObject.getString(VIDEO_URL))
        args.putString(VIDEO_NAME, channelObject.getString(VIDEO_NAME))
        args.putInt(VIDEO_COUNT, channelObject.getInt(VIDEO_COUNT))
        args.putString(CHANNEL_ID, channelObject.getString(CHANNEL_ID))
        frag.arguments = args
        return frag
    }

    private fun addFragment(fragment: Fragment, viewID: Int) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.design_bottom_sheet_slide_in,
                R.anim.design_bottom_sheet_slide_out
            )
            .replace(
                viewID,
                fragment,
                fragment.javaClass.simpleName
            )
            .commit()
    }

}