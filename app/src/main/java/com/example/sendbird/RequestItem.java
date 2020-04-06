package com.example.sendbird;

public class RequestItem {
    private String id, fromId, name, avatar, type;
    RequestItem(){}
    RequestItem(String id, String fromId, String userName, String avatar, String type){
        this.id = id;
        this.fromId = fromId;
        this.name = userName;
        this.avatar = avatar;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setName(String userName) {
        this.name = userName;
    }
}
