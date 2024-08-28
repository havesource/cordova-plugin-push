package com.adobe.phonegap.push

import java.util.ArrayList
import java.util.HashMap

object MessagesStore {

    private val messageMap = HashMap<Int, ArrayList<String?>>()

    /**
     * Set Notification
     * If message is empty or null, the message list is cleared.
     *
     * @param notId
     * @param message
     */
    fun set(notId: Int, message: String?) {
        var messageList = messageMap[notId]

        if (messageList == null) {
            messageList = ArrayList()
            messageMap[notId] = messageList
        }

        if (message.isNullOrEmpty()) {
            messageList.clear()
        } else {
            messageList.add(message)
        }
    }

    /**
     * Get Notification
     * If no message found by @notId, returns empty message list.
     *
     * @param notId
     */
    fun get(notId: Int): ArrayList<String?> {
        return messageMap.getOrDefault(notId, ArrayList<String?>())
    }
}