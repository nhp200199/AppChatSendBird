package com.example.sendbird;

import android.widget.ImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationItem {
    String channelId, name, avatar, time, lastMessage;
    ConversationItem(){}
    public String getDate() {
        return time;
    }

    public void setDate(String date) {
        this.time = date;
    }
    public ConversationItem(String name, String avatar) {
        this.name = name;
        this.avatar = avatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getTime() {
        return time;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public ConversationItem(String name, String avatar, String date, String content) {
        this.name = name;
        this.avatar = avatar;
        this.time = date;
        this.lastMessage = content;
    }

    public ConversationItem(String channelId, String name, String avatar, String time, String lastMessage) {
        this.channelId = channelId;
        this.name = name;
        this.avatar = avatar;
        this.time = time;
        this.lastMessage = lastMessage;
    }
}
