package com.example.sendbird;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactItem implements Parcelable {

    public String id,name, avatar, mChannel;

    public ContactItem()
    {
    }

    public ContactItem(String uid, String name, String avatar) {
        this.name = name;
        this.avatar = avatar;
        this.id = uid;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return id;
    }

    public void setUid(String uid) {
        this.id = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getChannel() {
        return mChannel;
    }

    public void setChannel(String channel) {
        mChannel = channel;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(avatar);
    }

    public static final Parcelable.Creator<ContactItem>CREATOR = new Parcelable.Creator<ContactItem>(){

        @Override
        public ContactItem createFromParcel(Parcel source) {
            String id = source.readString();
            String name = source.readString();
            String avatar = source.readString();

            return new ContactItem(id, name, avatar);
        }

        @Override
        public ContactItem[] newArray(int size) {
            return new ContactItem[size];
        }
    };
}
