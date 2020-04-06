package com.example.sendbird;

public class ContactItem {

    public String uid,name, avatar;

    public ContactItem()
    {

    }

    public ContactItem(String uid, String name, String avatar) {
        this.name = name;
        this.avatar = avatar;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
}
