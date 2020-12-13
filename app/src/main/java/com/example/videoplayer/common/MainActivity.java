package com.example.videoplayer.common;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import com.example.videoplayer.R;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        setRecyclerView();

    }

    //检查是否授权，没有授权就请求授权
    public void checkPermission(){
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    //授权回调函数的处理
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this,"访问本地视频的权限终于授权成功了！",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this,"访问本地视频的权限还是授权失败。。。",Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void setRecyclerView(){
        recyclerView = findViewById(R.id.recyclerView);
        RecyclerAdapter adapter = new RecyclerAdapter(MainActivity.this,R.layout.main_item,getMyData());
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
    }

    public List<Video> getMyData(){
        List<Video> myData = new ArrayList<>();
        Map map = new HashMap();
        int i = 0;
        int video_id;//视频Id
        String video_data;//视频播放路径
        String video_title;//视频的标题
        int video_duration;//视频播放时长
        Bitmap thumbnail = null;//视频简略图

        Video video;
        Uri usi = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] localVideoColumns = new String[]{
                MediaStore.Video.Media._ID, // 视频id
                MediaStore.Video.Media.DATA, // 视频路径
                MediaStore.Video.Media.TITLE, // 视频标题
                MediaStore.Video.Media.DURATION, // 视频时长
        };
        String[] thumbColumns = {
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID
        };

        Cursor cursor = getContentResolver().query(usi,localVideoColumns,null,null,null);
        cursor.moveToFirst();
        do{
            video_id = cursor.getInt(0);//视频id
            video_data = cursor.getString(1);//视频路径
            video_title = cursor.getString(2);//视频标题
            video_duration = cursor.getInt(3);//视频时长
            Cursor thumbnailCursor = getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,thumbColumns,
                    MediaStore.Video.Thumbnails.VIDEO_ID + "=" + video_id, null, null);
            if (thumbnailCursor.moveToFirst()){//如果媒体库存有视频的简略图，则获取系统简略图，若没有，则调用getVideoPhoto方法生成简略图
                String thumbnails_data = thumbnailCursor.getString(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                thumbnail = BitmapFactory.decodeFile(thumbnails_data);
            }else{
                thumbnail = getVideoPhoto(video_data);
            }
            thumbnailCursor.close();//将thumbnailCursor资源回收

            video = new Video(video_id,video_data,video_title,video_duration,thumbnail);
            myData.add(video);
        }while (cursor.moveToNext());
        cursor.close();//将cursor资源回收
        return myData;
    }

    private Bitmap getVideoPhoto(String videoPath) {//获取视频的简略图
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        if(new File(videoPath).exists()){
            media.setDataSource(videoPath);
        }else {
            Log.d("delete","文件不存在");
        }

        Bitmap bitmap = media.getFrameAtTime();
        return bitmap;
    }

}
