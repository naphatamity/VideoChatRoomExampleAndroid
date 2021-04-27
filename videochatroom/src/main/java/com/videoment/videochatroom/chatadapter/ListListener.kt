package com.videoment.videochatroom.chatadapter

import android.view.View
import com.ekoapp.ekosdk.message.EkoMessage

interface ListListener {
    fun  onItemClick(chatItem: EkoMessage, position: Int, holder: View)
    fun  onItemLongClick(chatItem: EkoMessage, position: Int, holder: View)
}