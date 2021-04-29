package com.videoment.videochatroom.chatfragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveDataReactiveStreams
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.EkoClient
import com.ekoapp.ekosdk.EkoMessageRepository
import com.ekoapp.ekosdk.internal.api.http.EkoOkHttp
import com.ekoapp.ekosdk.message.EkoMessage
import com.ekoapp.ekosdk.message.flag.EkoMessageFlagger
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.videoment.videochatroom.ChatReaction
import com.videoment.videochatroom.R
import com.videoment.videochatroom.chatadapter.ChatAdapter
import com.videoment.videochatroom.chatadapter.ListListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ChatHomeFragment() : Fragment(R.layout.chat_list), ListListener {
    private var channelID = ""
    var chatAdapter: ChatAdapter? = null
    private lateinit var messageQuery: LiveData<PagedList<EkoMessage>>
    private lateinit var channelDisposable: Disposable

    companion object {
        const val CHANNEL_ID_ARG_KEY = "channelID"
        const val HIDE_KEY_BOARD = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.channelID = arguments?.getString(CHANNEL_ID_ARG_KEY) ?: ""
        initChatFragment()

        val channelRepository = EkoClient.newChannelRepository()
        val messageRepository = EkoClient.newMessageRepository()
        channelRepository.joinChannel(channelID)
            .subscribe().run {
                channelDisposable = this
            }
        if (!channelDisposable.isDisposed) {
            initChatInput(messageRepository)
            initChat(messageRepository)
        }
    }

    private fun initChat(messageRepository: EkoMessageRepository) {
        messageQuery = getMessageCollection(messageRepository)
        messageQuery.observe(requireActivity(), Observer {
            if (chatAdapter?.itemCount!! < it.size) {
                chatAdapter?.submitList(it).run {
                    requireView().findViewById<RecyclerView>(R.id.content_recycler)
                        .scrollToPosition(it.size - 1)
                    requireView().findViewById<Button>(R.id.bottomBtn).visibility = View.VISIBLE
                }
            } else {
                chatAdapter?.submitList(it)
            }
        })
    }

    private fun getMessageCollection(messageRepository: EkoMessageRepository): LiveData<PagedList<EkoMessage>> {
        return LiveDataReactiveStreams.fromPublisher(
            messageRepository.getMessageCollection(channelID)
                .build()
                .query()
                .subscribeOn(Schedulers.io())
                .observeOn((AndroidSchedulers.mainThread()))
        )
    }


    override fun onPause() {
        super.onPause()
        messageQuery.removeObservers(this)
        if (!channelDisposable.isDisposed) {
            channelDisposable.dispose()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messageQuery.removeObservers(this)
        if (!channelDisposable.isDisposed) {
            channelDisposable.dispose()
        }
    }

    private fun initChatFragment() {
        val recycler = requireView().findViewById<RecyclerView>(R.id.content_recycler)
        chatAdapter = ChatAdapter(this@ChatHomeFragment)
        recycler.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = false
            adapter = chatAdapter
            onFlingListener = null
        }
        addOnScroll(recycler)
    }

    private fun addOnScroll(recycler: RecyclerView) {
        val bottomBtn = requireView().findViewById<Button>(R.id.bottomBtn)
        bottomBtn.setOnClickListener {
            chatAdapter?.itemCount?.minus(1)?.let { it1 -> recycler.scrollToPosition(it1) }
            bottomBtn.visibility = View.GONE
        }

        chatAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val lastPosition = chatAdapter!!.itemCount - 1
                if (positionStart == lastPosition) {
                    recycler.scrollToPosition(lastPosition)
                }
            }
        })

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var scroll_state = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scroll_state = newState
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (scroll_state > 0 && chatAdapter != null) {
                    val visibleItemCount =
                        (recycler.layoutManager as LinearLayoutManager).childCount
                    val positionView =
                        (recycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    chatAdapter?.itemCount?.minus(visibleItemCount + positionView).let {
                        if (it != null) {
                            if (it > 0) {
                                bottomBtn.visibility = View.VISIBLE
                            } else {
                                bottomBtn.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        })
    }

    private fun initChatInput(messageRepository: EkoMessageRepository) {
        addProfileImage()
        addBtnCickListener(messageRepository)
    }

    private fun addProfileImage() {
        val profileImage = requireActivity().findViewById<ImageView>(R.id.profileDarkImageView)
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
        val profileUrl = sharedPref.getString("profileImage", null)

        if (profileUrl != null) {
            val builder = Picasso.Builder(requireContext())
            val ekoOkHttpClient = EkoOkHttp.newBuilder().build()
            val picasso = builder.downloader(OkHttp3Downloader(ekoOkHttpClient)).build()

            picasso.load(profileUrl)
                .placeholder(R.drawable.amity_ic_avatar_placeholder)
                .error(R.drawable.ic_default_profile).into(profileImage)
        }
    }

    private fun addBtnCickListener(messageRepository: EkoMessageRepository) {
        val chatEditText = requireActivity().findViewById<EditText>(R.id.messageDarkTextview)
        val sendBtn = requireActivity().findViewById<ImageButton>(R.id.sendChatImageView)
        sendBtn.setOnClickListener {
            if (chatEditText.text.isNotBlank()) {
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(
                    view?.windowToken,
                    HIDE_KEY_BOARD
                )
                messageRepository.createMessage(channelID)
                    .with()
                    .text(chatEditText.text.toString())
                    .build()
                    .send()
                    .subscribe()
            }
            chatEditText.setText("")
        }
    }

    override fun onItemClick(chatItem: EkoMessage, position: Int, holder: View) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.popup_window_message_reaction, null)

        val popup = PopupWindow(
            view,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            false,
        )
        popup.isOutsideTouchable = true
        val holderLayout = holder.findViewById<ConstraintLayout>(R.id.mainItemChatLinearLayout)
        val xDimen = holderLayout.x.toInt()
        val yDimen = holderLayout.bottom

        popup.showAsDropDown(holder, xDimen * 2, -1 * yDimen)

        initialLoveButton(chatItem, view)
        initialSmileButton(chatItem, view)
        initialButton(chatItem, view)
        initialHeartEyeButton(chatItem, view)
        initialFireButton(chatItem, view)
    }

    override fun onItemLongClick(chatItem: EkoMessage, position: Int, holder: View) {
        onAlertDialog(chatItem, holder)
    }

    private fun initialLoveButton(chatItem: EkoMessage, view: View) {
        val loveBtn = view.findViewById<TextView>(R.id.sendLoveEmoji)
        loveBtn.setOnClickListener {
            chatItem.react()
                .addReaction(ChatReaction.LOVE)
                .subscribe()
            view.visibility = View.GONE
        }
    }

    private fun initialSmileButton(chatItem: EkoMessage, view: View) {
        val smileBtn = view.findViewById<TextView>(R.id.sendSmileEmoji)
        smileBtn.setOnClickListener {
            chatItem.react()
                .addReaction(ChatReaction.SMILE)
                .subscribe()
            view.visibility = View.GONE
        }
    }

    private fun initialButton(chatItem: EkoMessage, view: View) {
        val lolBtn = view.findViewById<TextView>(R.id.sendlolEmoji)
        lolBtn.setOnClickListener {
            chatItem.react()
                .addReaction(ChatReaction.LOL)
                .subscribe()
            view.visibility = View.GONE
        }
    }

    private fun initialHeartEyeButton(chatItem: EkoMessage, view: View) {
        val heartEyeBtn = view.findViewById<TextView>(R.id.sendHeartEyeEmoji)
        heartEyeBtn.setOnClickListener {
            chatItem.react()
                .addReaction(ChatReaction.HEARTEYE)
                .subscribe()
            view.visibility = View.GONE
        }
    }

    private fun initialFireButton(chatItem: EkoMessage, view: View) {
        val fireBtn = view.findViewById<TextView>(R.id.sendFiremoji)
        fireBtn.setOnClickListener {
            chatItem.react()
                .addReaction(ChatReaction.FIRE)
                .subscribe()
            view.visibility = View.GONE
        }
    }

    operator fun invoke(): Fragment {
        return this
    }

    private fun onAlertDialog(chatItem: EkoMessage, view: View) {
        val builder = AlertDialog.Builder(view.context, R.style.AlertDialogCustom)
        builder.setTitle((resources.getString(R.string.report_title)))
        builder.setMessage((resources.getString(R.string.report_description)))
        builder.setPositiveButton(
            resources.getString(R.string.ok)
        ) { dialog, id ->
            val flagger: EkoMessageFlagger = chatItem.report()
            flagger.flag()
                .doOnComplete { dialog.dismiss() }
                .subscribe()
        }
        builder.setNegativeButton(
            resources.getString(R.string.cancel)
        ) { dialog, id ->
            dialog.dismiss()
        }
        builder.show()
    }

}