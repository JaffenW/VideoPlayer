package com.example.videoplayer.common;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Video{
    private int video_id;
    private String video_data;
    private  String video_title;
    private  int video_duration;
    private Bitmap thumbnails_data;

    public Video() {
    }

    public Video(int video_id, String video_data, String video_title, int video_duration, Bitmap thumbnails_data) {
        this.video_id = video_id;
        this.video_data = video_data;
        this.video_title = video_title;
        this.video_duration = video_duration;
        this.thumbnails_data = thumbnails_data;
    }

    public int getVideo_id() {
        return video_id;
    }

    public void setVideo_id(int video_id) {
        this.video_id = video_id;
    }

    public String getVideo_data() {
        return video_data;
    }

    public void setVideo_data(String video_data) {
        this.video_data = video_data;
    }

    public String getVideo_title() {
        return video_title;
    }

    public void setVideo_title(String video_title) {
        this.video_title = video_title;
    }

    public int getVideo_duration() {
        return video_duration;
    }

    public void setVideo_duration(int video_duration) {
        this.video_duration = video_duration;
    }

    public Bitmap getThumbnails_data() {
        return thumbnails_data;
    }

    public void setThumbnails_data(Bitmap thumbnails_data) {
        this.thumbnails_data = thumbnails_data;
    }
}
