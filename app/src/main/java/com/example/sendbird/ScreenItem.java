package com.example.sendbird;

public class ScreenItem {
    String title, description;
    int imageRsc;

    public ScreenItem(String title, String description, int imageRsc) {
        this.title = title;
        this.description = description;
        this.imageRsc = imageRsc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImageRsc() {
        return imageRsc;
    }

    public void setImageRsc(int imageRsc) {
        this.imageRsc = imageRsc;
    }
}
