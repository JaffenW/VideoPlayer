package com.example.videoplayer.common;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.videoplayer.R;
import com.example.videoplayer.play.VideoActivity;
import java.io.File;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{
    private Context context;
    private int source;
    private List<Video> videos;

    public RecyclerAdapter(Context context, int source, List<Video> videos) {
        this.context = context;
        this.source = source;
        this.videos = videos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        View view;
        view = LayoutInflater.from(context).inflate(source,parent,false);
        viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Video video = videos.get(position);
        if (video.getThumbnails_data()!=null)
            holder.thumbnail.setImageBitmap(video.getThumbnails_data());
         else
             Log.d("recycler","图片不存在");
        holder.title.setText(video.getVideo_title());
        holder.duration.setText(stringForTime(video.getVideo_duration()));
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//点击图片进入播放界面
                Intent intent = new Intent(context, VideoActivity.class);
                intent.putExtra("titles",getTitle());
                intent.putExtra("datas",getData());
                intent.putExtra("position",position);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView thumbnail;
        TextView title;
        TextView duration;
        Button delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.imageView);
            title = itemView.findViewById(R.id.title);
            duration = itemView.findViewById(R.id.duration);
        }
    }

    public String[] getTitle(){//返回所有的标题数组
        String[] titles = new String[videos.size()];
        for (int i= 0;i<videos.size();i++){
            titles[i] = videos.get(i).getVideo_title();
        }
        return titles;
    }

    public String[] getData(){//返回所有的播放路径数组
        String[] datas = new String[videos.size()];
        for (int i= 0;i<videos.size();i++){
            datas[i] = videos.get(i).getVideo_data();
        }
        return datas;
    }

    private String stringForTime(int duration){//将int类型的时间转换成HH：MM：SS格式
        String time = null;
        int dur = duration/1000;//将时间由微秒转换成秒
        if(dur<60){//少于60秒
            time = "00:00:"+dur;
        }else if(dur<60*10){//少于10分钟
            time = "00:0"+dur/60+":"+dur%60;
        }else if (dur<60*60){//少于60分钟
            time = "00:"+dur/60+":"+dur%60;
        }else{//大于一个小时
            time = "0"+dur/3600+":"+dur%3600/60+":"+dur%3600%60;
        }
        return time;
    }

}
