package com.example.sendbird;

public class ChatItem {
    private String id, from, type, message, name, avatar, date;
    private boolean isClicked;

    public boolean isClicked() {
        return isClicked;
    }

    public void toggleClicked() {
        isClicked = !isClicked;
    }

    public ChatItem(String id, String from, String name, String avatar, String message, String type, String date) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.from = from;
        this.name = name;
        this.avatar = avatar;
        this.date =date;
    }
    public ChatItem(){}
    public ChatItem(String id, String from, String message, String type, String date){
        this.id = id;
        this.type = type;
        this.message = message;
        this.from = from;
        this.date =date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
