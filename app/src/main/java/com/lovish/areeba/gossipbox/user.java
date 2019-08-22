package com.lovish.areeba.gossipbox;


import android.widget.RelativeLayout;

//
public class user {

    public String name;
    public String thumb_image;



    public user()
    {

    }

    public user(String name, String thumb_image) {
        this.name = name;
        this.thumb_image = thumb_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
