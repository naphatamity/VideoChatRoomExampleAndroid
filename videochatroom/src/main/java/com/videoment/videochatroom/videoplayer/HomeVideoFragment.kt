package com.videoment.videochatroom.videoplayer

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ekoapp.ekosdk.EkoClient
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.videoment.videochatroom.R
import io.reactivex.disposables.Disposable

class HomeVideoFragment() : Fragment(R.layout.video_player) {
    private var PLAYER_CURRENT_POS_KEY = "PLAYER_CURRENT_POS_KEY"
    private var fullScreen = 0
    private var channelID = ""
    var name = ""
    private var url = ""
    private var count = 0
    private var currentPosition: Long = 0
    private var playWhenReady = true
    private lateinit var channelDisposable: Disposable

    companion object {
        const val VIDEO_URL = "videoUrl"
        const val VIDEO_NAME = "videoName"
        const val CHANNEL_ID_ARG_KEY = "channelID"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        channelID = arguments?.getString(CHANNEL_ID_ARG_KEY) ?: ""
        url = arguments?.getString(VIDEO_URL) ?: ""
        name = arguments?.getString(VIDEO_NAME) ?: ""

        if (savedInstanceState != null) {
            onViewStateRestored(savedInstanceState)
        }

        val videoCount = requireActivity().findViewById<TextView>(R.id.view_count)
        channelDisposable = EkoClient.newChannelRepository().getChannel(channelID).subscribe {
            count = it.getMemberCount()
            if (videoCount != null)
                videoCount.text = count.toString()
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            switchToolbar()
            fullScreen = 1
            initChatBtnLand()
        } else {
            val videoShareBtn = requireActivity().findViewById<ImageButton>(R.id.videoShareBtn)

            videoShareBtn.setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
                intent.putExtra(Intent.EXTRA_TEXT, url);
                intent.type = "text/plain"
                startActivity(Intent.createChooser(intent, "Share To:"))
            }
        }
    }

    private fun initChatBtnLand() {
        val chatBtn = requireActivity().findViewById<ImageButton>(R.id.chatPlayer)
        val chatLayout = requireActivity().findViewById<RelativeLayout>(R.id.chatRelative)
        val playerControllerLayout =
            requireActivity().findViewById<FrameLayout>(R.id.playerController)
        val contentRelativeLayout =
            requireActivity().findViewById<RelativeLayout>(R.id.contentRelativeLayout)

        chatBtn.setOnClickListener {
            if (chatLayout.visibility == View.VISIBLE) {
                chatBtn.visibility = View.GONE
                chatLayout.visibility = View.GONE
                playerControllerLayout.visibility = View.GONE
                contentRelativeLayout.visibility = View.GONE
            } else {
                contentRelativeLayout.visibility = View.GONE
                chatLayout.visibility = View.VISIBLE
            }
        }
    }


    private fun initialVideo() {
        val playerScreen = requireView().findViewById<PlayerView>(R.id.matchPlayer)
        val videoName = requireActivity().findViewById<TextView>(R.id.videoNameTextView)
        val videoMenu = requireView().findViewById<ImageButton>(R.id.settingPlayer)
        val playerControllerLayout =
            requireActivity().findViewById<FrameLayout>(R.id.playerController)
        val chatBtn =
            requireActivity().findViewById<ImageButton>(R.id.chatPlayer)
        val contentRelativeLayout =
            requireActivity().findViewById<RelativeLayout>(R.id.contentRelativeLayout)

        videoMenu?.setOnClickListener {
            parentFragmentManager.let {
                OptionsBottomSheetFragment.newInstance(Bundle()).apply {
                    show(it, tag)
                }
            }
        }

        videoName.text = name
        playerScreen.requestFocus()

        val player = SimpleExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(requireContext()).setLiveTargetOffsetMs(
                    5000
                )
            )
            .build()

        val mediaItem: MediaItem = MediaItem.Builder()
            .setUri(url)
            .setLiveMaxPlaybackSpeed(1.02f)
            .build()

        player.setMediaItem(mediaItem)
        playerScreen.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        playerScreen.player = player
        player.prepare()
        player.seekTo(currentPosition)
        player.playWhenReady = playWhenReady
        player.play()

        playerScreen.videoSurfaceView?.setOnClickListener {
            playerScreen.showController()
            playerControllerLayout.visibility = View.VISIBLE
            contentRelativeLayout.visibility = View.VISIBLE
            chatBtn?.visibility = View.VISIBLE
        }

        playerScreen.setControllerVisibilityListener {
            if (it == 8) {
                val chatLayout = requireActivity().findViewById<RelativeLayout>(R.id.chatRelative)
                if (chatLayout?.isVisible == false || chatLayout == null) {
                    playerControllerLayout.visibility = View.GONE
                    chatBtn?.visibility = View.GONE
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        contentRelativeLayout.visibility = View.GONE
                    }
                }
            } else {
                contentRelativeLayout.visibility = View.VISIBLE
                playerControllerLayout.visibility = View.VISIBLE
                chatBtn?.visibility = View.VISIBLE
            }
        }

        initialVideoController()
    }

    private fun initialVideoController() {
        val fullscreenBtn = requireView().findViewById<ImageButton>(R.id.fullscreenBtn)

        fullscreenBtn.setOnClickListener {
            if (fullScreen == 0) {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                fullScreen = 0
            }
        }
    }


    private fun switchToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.let {
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                it.hide(WindowInsets.Type.systemBars())
            }
        } else {
            val uiOptions = requireActivity().window.decorView.systemUiVisibility
            var newUiOptions = uiOptions
            newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_FULLSCREEN
            newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            requireActivity().window.decorView.systemUiVisibility = newUiOptions
        }
    }

    override fun onPause() {
        super.onPause()
        val playerScreen = requireView().findViewById<PlayerView>(R.id.matchPlayer)
        currentPosition = playerScreen.player?.currentPosition!!
        channelDisposable.dispose()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val playerScreen = requireView().findViewById<PlayerView>(R.id.matchPlayer)
        super.onSaveInstanceState(outState)
        outState.putLong(
            PLAYER_CURRENT_POS_KEY,
            currentPosition
        )
        playerScreen.player?.stop()
        playerScreen.player?.release()
        playerScreen.player = null
    }

    override fun onResume() {
        super.onResume()
        val playerScreen = requireView().findViewById<PlayerView>(R.id.matchPlayer)
        if (playerScreen.player == null)
            initialVideo()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            currentPosition = (it.getLong(PLAYER_CURRENT_POS_KEY))
        }
    }

    operator fun invoke(): Fragment {
        return this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val playerScreen = requireView().findViewById<PlayerView>(R.id.matchPlayer)
        channelDisposable.dispose()
        playerScreen.player?.stop()
        playerScreen.player?.release()
        playerScreen.player = null
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        val frameLayout = requireActivity().findViewById<FrameLayout>(R.id.playerController)
        if (isInPictureInPictureMode) {
            frameLayout.visibility = View.GONE
        } else {
            frameLayout.visibility = View.VISIBLE
        }
    }

}