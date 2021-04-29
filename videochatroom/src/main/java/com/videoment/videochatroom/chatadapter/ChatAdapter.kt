package com.videoment.videochatroom.chatadapter

import butterknife.Setter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.adapter.EkoMessageAdapter
import com.ekoapp.ekosdk.message.EkoMessage
import com.videoment.videochatroom.R

class ChatAdapter(
    private val listener: ListListener
) : EkoMessageAdapter<RecyclerView.ViewHolder>(object :
    DiffUtil.ItemCallback<EkoMessage>() {

    override fun areItemsTheSame(oldItem: EkoMessage, newItem: EkoMessage): Boolean {
        return oldItem.getMessageId() == newItem.getMessageId()
    }

    override fun areContentsTheSame(oldItem: EkoMessage, newItem: EkoMessage): Boolean {
        return oldItem.getState() == newItem.getState()
                && oldItem.getData() == newItem.getData()
                && oldItem.getFlagCount() == newItem.getFlagCount()
                && oldItem.getReactionCount() == newItem.getReactionCount()
                && oldItem.getUser() == newItem.getUser()
    }
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        val visibility = Setter { view: View, value: Int?, index: Int -> view.visibility = value!! }
        if (message == null) {
            renderLoadingItem(holder as ChatViewHolder, visibility, position)
        } else {
            (holder as ChatViewHolder).bind(message)
            addOnClickListener(message, holder, position)
        }
    }

    private fun renderLoadingItem(
        holder: ChatViewHolder,
        visibility: Setter<View, Int?>,
        position: Int
    ) {
        holder.itemView.findViewById<TextView>(R.id.usernameMessageAppTextView).text =
            String.format("loading")
        holder.itemView.findViewById<TextView>(R.id.chatTextMessageAppTextView).text =
            String.format("loading")
    }

    private fun addOnClickListener(
        message: EkoMessage,
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (!message.isDeleted()) {
            holder.itemView
                .setOnClickListener {
                    listener.onItemClick(
                        message,
                        holder.adapterPosition,
                        holder.itemView
                    )
                }

            holder.itemView.setOnLongClickListener {
                listener.onItemLongClick(
                    message,
                    holder.adapterPosition,
                    holder.itemView
                )
                false
            }
        }
    }
}