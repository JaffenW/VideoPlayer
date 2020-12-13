package com.example.videoplayer.play;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.Nullable;
import com.example.videoplayer.R;
import com.example.videoplayer.common.MainActivity;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoActivity extends Activity implements View.OnClickListener{
    private VideoView videoView;//播放视图
    private AudioManager am;//音频管理器
    private int currentVoice;//当前音量
    private int maxVoice;//最大音量
    private boolean isMute;//是否静音
    private String[] titles;//标题集合
    private String[] datas;//播放路径集合
    private int position;//当前的位置
    private View controller;//控制面板
    private boolean isshowMediaController;//是否展示控制面板
    private GestureDetector detector;
    private boolean isEndAndNext;//是否自动播放下一个视频
    private TextView tvName;//视频标题
    private TextView tvSystemTime;//系统时间
    private Button btnVoice;//声音按键
    private SeekBar seekbarVoice;//声音进度条
    private Button deleteVideo;//删除视频
    private TextView tvCurrentTime;//当前播放时间
    private SeekBar seekbarVideo;//播放进度条
    private TextView tvDuration;//视频总时长
    private Button btnExit;//退出按钮
    private Button btnVideoPre;//播放前一个视频按钮
    private Button btnStartPause;//停止播放按钮
    private Button btnVideoNext;//播放下一个视频按钮
    private Button btnVideoIsNext;//是否自动播放下一个视频按钮

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //1.得到当前的视频播放进程
                    int currentPosition = videoView.getCurrentPosition();//0

                    //2.SeekBar.setProgress(当前进度);
                    seekbarVideo.setProgress(currentPosition);

                    //3.更新文本播放进度
                    tvCurrentTime.setText(stringForTime(currentPosition));

                    //设置系统时间
                    tvSystemTime.setText(getSystemTime());

                    //4.每秒更新一次
                    handler.removeMessages(1);
                    handler.sendEmptyMessageDelayed(1, 1000);
                    break;
                case 2:
                    hideMediaController();
                    break;

            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plays);
        findViews();
        setVoice();
    }

    private void findViews() {//进行控件绑定
        videoView = findViewById(R.id.mVideoView);
        tvName = (TextView)findViewById( R.id.tv_name );
        tvSystemTime = (TextView)findViewById( R.id.tv_system_time );
        btnVoice = (Button)findViewById( R.id.btn_voice );
        seekbarVoice = (SeekBar)findViewById( R.id.seekbar_voice );
        deleteVideo = (Button)findViewById( R.id.btn_delete );
        tvCurrentTime = (TextView)findViewById( R.id.tv_current_time );
        seekbarVideo = (SeekBar)findViewById( R.id.seekbar_video );
        tvDuration = (TextView)findViewById( R.id.tv_duration );
        btnExit = (Button)findViewById( R.id.btn_exit );
        btnVideoPre = (Button)findViewById( R.id.btn_video_pre );
        btnStartPause = findViewById(R.id.btn_video_start_pause);
        btnVideoNext = (Button)findViewById( R.id.btn_video_next );
        btnVideoIsNext = (Button)findViewById( R.id.btn_video_is_next );
        controller = findViewById(R.id.contorller);
        isEndAndNext = true;

        btnVoice.setOnClickListener( this);
        deleteVideo.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this );
        btnVideoIsNext.setOnClickListener(this);

        //获取Intent传过来的信息
        Intent intent = getIntent();
        position = intent.getIntExtra("position",0);
        titles = intent.getStringArrayExtra("titles");
        datas = intent.getStringArrayExtra("datas");
        tvName.setText(titles[position]);
        Uri uri = Uri.parse(datas[position]);
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(new MyPreparedListener());//设置准备监听器
        videoView.setOnCompletionListener(new MyCompleteListener());//设置播放完成监听器
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());//设置播放进度条变化监听器
        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());//设置声音进度条变化监听器
        detector = new GestureDetector(this,new MySimpleOnGestureListener());//设置手势监听器

    }

    //处理按键的点击事件
    @Override
    public void onClick(View v) {
        if ( v == btnVoice ) {//设置静音
            isMute = !isMute;
            updateVoice(currentVoice, isMute);
        } else if ( v == deleteVideo ) {//删除视频
            setDeleteVideo();
        } else if ( v == btnExit ) {//退出播放
            exit();
        } else if ( v == btnVideoPre ) {//播放上一个视频
            preVideo();
        } else if ( v == btnVideoNext ) {//播放下一个视频
            nextVideo();
        } else if ( v == btnVideoIsNext ) {//切换是播放单个视频还是循环播放
            setEndIsNext();
        } else if ( v == btnStartPause ){//播放暂停
            startAndPause();
        }
    }


    /**
     * 声音相关的设置
     */
    //使系统音量与进度条音量绑定
    private void setVoice(){
        //得到音量
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = am.getStreamVolume(AudioManager.STREAM_MUSIC);//获取手机当前音量
        maxVoice = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获取手机最大音量
        seekbarVoice.setMax(maxVoice);//最大音量和SeekBar关联
        seekbarVoice.setProgress(currentVoice);//设置当前进度-当前音量
    }

    //设置静音按键
    private void updateVoice(int progress, boolean isMute) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            currentVoice = progress;
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
        }
    }

    //监听声音控制的物理按键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updateVoice(currentVoice, false);
            showMediaController();
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            updateVoice(currentVoice, false);
            showMediaController();
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //声音进度条的监听器
    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        //该参数同上述SeekBar
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress > 0) {
                    isMute = false;
                } else {
                    isMute = true;
                }
                updateVoice(progress, isMute);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { handler.removeMessages(2);}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { handler.sendEmptyMessageDelayed(2,4000); }
    }

    /*
    删除视频
     */
    private void setDeleteVideo(){
        File file = new File(datas[position]);
        if (file.exists()&&file.isFile()){
            nextVideo();
            if (file.delete()){
                Toast.makeText(VideoActivity.this,"删除成功",Toast.LENGTH_LONG).show();
                updateFileFromDatabase(VideoActivity.this,file);
            }else {
                Toast.makeText(VideoActivity.this,"删除失败",Toast.LENGTH_LONG).show();
            }
        }else
            Log.d("delete","文件不存在");
    }

    /**
     * 下面是设置上一个视频和下一个视频的点击事件
     */
    //上一个视频的点击事件
    private void preVideo(){
        if (position != 0){
            position--;
            String uri = datas[position];
            videoView.setVideoPath(uri);
            tvName.setText(titles[position]);
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
        }else{
            position = datas.length-1;
            String uri = datas[position];
            videoView.setVideoPath(uri);
            tvName.setText(titles[position]);
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
        }
    }

    //下一个视频的点击事件
    private void nextVideo(){
        if (position != datas.length-1){
            position++;
            String uri = datas[position];
            videoView.setVideoPath(uri);
            tvName.setText(titles[position]);
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
        }else{
            position = 0;
            String uri = datas[position];
            videoView.setVideoPath(uri);
            tvName.setText(titles[position]);
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
        }

    }

    /**
     * 下面是开始和暂停按钮的点击事件
     */
    //开始和暂停
    private void startAndPause() {
        if (videoView.isPlaying()) {//视频在播放-设置暂停
            videoView.pause();
            btnStartPause.setBackgroundResource(R.mipmap.ic_status_bar_play_light);
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
        } else {//视频在暂停-设置播放
            videoView.start();
            btnStartPause.setBackgroundResource(R.mipmap.ic_status_bar_pause_light);
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
        }
    }

    //设置是否自动播放下一个视频
    private void setEndIsNext(){
        if (isEndAndNext){
            isEndAndNext = false;
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
            btnVideoIsNext.setBackgroundResource(R.mipmap.ic_play_btn_one);
        }else {
            isEndAndNext = true;
            handler.removeMessages(2);
            handler.sendEmptyMessageDelayed(2,4000);
            btnVideoIsNext.setBackgroundResource(R.mipmap.ic_play_btn_shuffle);
        }
    }

    /**
     * 下面使对系统时间的设置
     */
    //获取系统时间
    public String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date());
    }

    //将整型的时间转换成hh：mm：ss形式
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

    /**
     * 下面是对控制面板显示和隐藏的设置
     */
    //隐藏控制面板
    private void hideMediaController() {
        controller.setVisibility(View.GONE);
        isshowMediaController = false;    //该参数用于表示面板是否显示，默认隐藏
    }

   //显示控制面板
    private void showMediaController() {
        controller.setVisibility(View.VISIBLE);
        isshowMediaController = true;
    }

    //手势监听器
    class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        //长按监听
        @Override
        public void onLongPress(MotionEvent e) {
            if (isshowMediaController) {
                //隐藏
                hideMediaController();
                //把隐藏消息移除
                handler.removeMessages(2);
            } else {
                //显示
                showMediaController();
                //过4秒，自动发消息隐藏控制面板消息
                handler.sendEmptyMessageDelayed(2, 4000);
            }

        }

        //点击两次监听
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            //setFullScreenAndDefault();
            return super.onDoubleTapEvent(e);
        }

        //点击一次监听
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            startAndPause();
            return super.onSingleTapConfirmed(e);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
               detector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    /*

            下面是一些监听器内部实现类

             */
    //播放准备监听器
    private class MyPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            //视频准备好了，进行播放
            mediaPlayer.start();
            //视频的总时长，并关联SeekBar的总长度
            int duration = videoView.getDuration();
            //设置SeekBar进度的总长
            seekbarVideo.setMax(duration);
            //设置视频的总时间
            tvDuration.setText(stringForTime(duration));
            //发消息
            handler.sendEmptyMessage(1);

            //默认是隐藏控制面板
            hideMediaController();
        }
    }

    //播放进度条的监听器
    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 当手指滑动的时候，会引起SeekBar进度变化，会回调这个方法
         * @param seekBar
         * @param progress
         * @param fromUser 如果是用户引起的true,不是用户引起的false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoView.seekTo(progress);
            }
        }

        /**
         * 当手指触碰的时候回调这个方法
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(2);
        }
        /**
         * 当手指离开的时候回调这个方法
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(2,4000);
        }
    }

    //播放结束的接听器
    class MyCompleteListener implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (isEndAndNext){//如果是循环播放
                if (position != datas.length-1){//如果没有播放到下一个视频
                    position++;
                    String uri = datas[position];
                    videoView.setVideoPath(uri);
                    tvName.setText(titles[position]);
                }else{//播放完最后一个视频则返回第一个视频
                    position = 0;
                    String uri = datas[position];
                    videoView.setVideoPath(uri);
                    tvName.setText(titles[position]);
                }
            }else {
                finish();
            }
        }
    }

    //此方法来自网络搜索,用于删除视频之后更新媒体库
    public static void updateFileFromDatabase(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String[] paths = new String[]{Environment.getExternalStorageDirectory().toString()};
            MediaScannerConnection.scanFile(context, paths, null, null);
            MediaScannerConnection.scanFile(context, new String[]{
                            file.getAbsolutePath()},
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    //返回主界面，通过finish（）同样可以返回主界面，但是删除视频后并不会更新视频列表
    public void exit(){
        Intent intent = new Intent(VideoActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
