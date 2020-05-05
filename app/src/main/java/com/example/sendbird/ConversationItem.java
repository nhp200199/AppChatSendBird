package com.example.sendbird;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationItem implements Parcelable {
    private  String uid;
    String channelId, name, avatar, time, lastMessage;
    ConversationItem(){}

    public ConversationItem(String uid, String channelId, String name, String avatar, String time, String message) {
        this.channelId = channelId;
        this.name = name;
        this.avatar = avatar;
        this.time = time;
        this.lastMessage = message;
        this.uid = uid;
    }

    private ConversationItem(Parcel source) {
        channelId = source.readString();
        name = source.readString();
        avatar = source.readString();
        lastMessage = source.readString();
        uid = source.readString();

    }

    public String getDate() {
        return time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public ConversationItem(String channelId, String name, String avatar, String time, String lastMessage) {
        this.channelId = channelId;
        this.name = name;
        this.avatar = avatar;
        this.time = time;
        this.lastMessage = lastMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(channelId);
        dest.writeString(name);
        dest.writeString(lastMessage);
        dest.writeString(time);
        dest.writeString(uid);
    }

    public static final Parcelable.Creator<ConversationItem> CREATOR =
            new Parcelable.Creator<ConversationItem>(){

                @Override
                public ConversationItem createFromParcel(Parcel source) {
                    return new ConversationItem(source);
                }

                @Override
                public ConversationItem[] newArray(int size) {
                    return new ConversationItem[size];
                }
            };
}
