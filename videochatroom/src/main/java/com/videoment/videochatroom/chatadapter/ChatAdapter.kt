package com.videoment.videochatroom.chatadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ekoapp.ekosdk.message.EkoMessage
import com.videoment.videochatroom.R

class ChatAdapter(
    private val listener: ListListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EkoMessage>() {

        override fun areItemsTheSame(oldItem: EkoMessage, newItem: EkoMessage): Boolean {
            return oldItem.getMessageId() == newItem.getMessageId()
        }

        override fun areContentsTheSame(oldItem: EkoMessage, newItem: EkoMessage): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_list, parent, false)
        )
    }

    fun submitList(list: List<EkoMessage>) {
        differ.submitList(list)
    }

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = differ.currentList[position]
        if (message != null) {
            (holder as ChatViewHolder).bind(message)
            addOnClickListener(message, holder, position)
        }
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