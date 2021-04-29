package com.videoment.videochatroom.chatadapter

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.internal.api.http.EkoOkHttp
import com.ekoapp.ekosdk.message.EkoMessage
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.videoment.videochatroom.ChatReaction
import com.videoment.videochatroom.R

class ChatViewHolder(itemsView: View) : RecyclerView.ViewHolder(itemsView) {

    fun bind(item: EkoMessage) {
        itemView.apply {
            addProfilePicture(this, item)
            addMessageToView(this, item)
            addMessageReaction(this, item)
        }
    }

    private fun addProfilePicture(view: View, item: EkoMessage) {
        val imgUrl = item.getUser()?.getAvatar()?.getUrl()
        if (imgUrl != null) {
            val builder = Picasso.Builder(view.context)
            val ekoOkHttpClient = EkoOkHttp.newBuilder().build()
            val picasso = builder.downloader(OkHttp3Downloader(ekoOkHttpClient)).build()

            picasso.load(imgUrl)
                .placeholder(R.drawable.amity_ic_avatar_placeholder)
                .error(R.drawable.ic_default_profile)
                .into(view.findViewById<ImageView>(R.id.profileImageView));
        }
    }


    private fun addMessageToView(view: View, item: EkoMessage) {

        val name = item.getUser()?.getDisplayName()
        view.findViewById<TextView>(R.id.usernameMessageAppTextView).text = name

        when (val description = item.getData()) {
            is EkoMessage.Data.TEXT -> {
                if (!item.isDeleted()) {
                    view.findViewById<TextView>(R.id.chatTextMessageAppTextView).text =
                        description.getText()
                } else {
                    view.findViewById<TextView>(R.id.chatTextMessageAppTextView).text =
                        view.resources.getString(R.string.message_deleted)
                }
            }
        }
    }

    private fun addMessageReaction(view: View, item: EkoMessage) {
        val textCount = view.findViewById<TextView>(R.id.countReactionTextView)
        val sendLoveEmoji = view.findViewById<TextView>(R.id.sendLoveEmoji)
        val sendSmileEmoji = view.findViewById<TextView>(R.id.sendSmileEmoji)
        val sendlolEmoji = view.findViewById<TextView>(R.id.sendlolEmoji)
        val sendHeartEyeEmoji = view.findViewById<TextView>(R.id.sendHeartEyeEmoji)
        val sendFiremoji = view.findViewById<TextView>(R.id.sendFiremoji)

        if (item.getReactionCount() > 0) {
            textCount.visibility = View.VISIBLE
            textCount.text = item.getReactionCount().toString()
        } else {
            textCount.visibility = View.GONE
        }

        if (item.getReactionMap().getCount(ChatReaction.LOVE) > 0) {
            sendLoveEmoji.visibility = View.VISIBLE
        } else {
            sendLoveEmoji.visibility = View.GONE
        }
        if (item.getReactionMap().getCount(ChatReaction.SMILE) > 0) {
            sendSmileEmoji.visibility = View.VISIBLE
        } else {
            sendSmileEmoji.visibility = View.GONE
        }
        if (item.getReactionMap().getCount(ChatReaction.LOL) > 0) {
            sendlolEmoji.visibility = View.VISIBLE
        } else {
            sendlolEmoji.visibility = View.GONE
        }
        if (item.getReactionMap().getCount(ChatReaction.HEARTEYE) > 0) {
            sendHeartEyeEmoji.visibility =
                View.VISIBLE
        } else {
            sendHeartEyeEmoji.visibility = View.GONE
        }
        if (item.getReactionMap().getCount(ChatReaction.FIRE) > 0) {
            sendFiremoji.visibility = View.VISIBLE
        } else {
            sendFiremoji.visibility = View.GONE
        }
        view.findViewById<LinearLayout>(R.id.reactionLayout)
            .setBackgroundResource(R.drawable.round_corner_white)
    }
}