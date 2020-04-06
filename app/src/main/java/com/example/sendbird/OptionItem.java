package com.example.sendbird;

public class OptionItem {
    int img_option;
    String title_option;

    OptionItem(){

    }
    public OptionItem(int img_option, String title_option) {
        this.img_option = img_option;
        this.title_option = title_option;
    }

    public int getImg_option() {
        return img_option;
    }

    public void setImg_option(int img_option) {
        this.img_option = img_option;
    }

    public String getTitle_option() {
        return title_option;
    }

    public void setTitle_option(String title_option) {
        this.title_option = title_option;
    }
}
