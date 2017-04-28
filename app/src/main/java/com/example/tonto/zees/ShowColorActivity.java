package com.example.tonto.zees;

import android.content.Context;
import android.content.pm.ActivityInfo;

import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;


import static com.example.tonto.zees.ChooseColorActivity.*;

public class ShowColorActivity extends AppCompatActivity {

    private ImageView backGround;
    private Window window;
    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    Camera.Parameters params;
    private Thread thread;
    private Handler handler;
    MediaPlayer mp;
     MediaPlayer mp1 ;

    private Context context=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_show_color);
        window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.darkRain));
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        backGround = (ImageView) findViewById(R.id.iv_bg);
        VideoView mVideoView = (VideoView) findViewById(R.id.videoview);
        Bundle bundle = getIntent().getExtras();
        int color = bundle.getInt("COlOR TO SHOW");
        int mode = bundle.getInt("MODE TO SHOW");
        int lava_mode = bundle.getInt("LAVA MODE TO SHOW");
        if (mode == 1) {
            mVideoView.setVisibility(View.INVISIBLE);
            backGround.setVisibility(View.VISIBLE);
            Context context = backGround.getContext();
            int id = context.getResources().getIdentifier("color" + color + "_bg", "drawable", context.getPackageName());
            backGround.setImageResource(id);
        } else if (mode == 3) {
            backGround.setVisibility(View.INVISIBLE);
            mVideoView.setVisibility(View.VISIBLE);
            String uriPath = null;
            if (lava_mode == INFERNO)
                uriPath = "android.resource://com.example.tonto.zees/" + R.raw.inferno;
            else if (lava_mode == WATER)
                uriPath = "android.resource://com.example.tonto.zees/" + R.raw.water;
            else if (lava_mode == CAMP) {
                uriPath = "android.resource://com.example.tonto.zees/" + R.raw.fire;
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            if (lava_mode != THUNDER) {
                Uri uri = Uri.parse(uriPath);
                mVideoView.setVideoURI(uri);
                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                });
                mVideoView.requestFocus();
                mVideoView.start();
            } else {
                mp = MediaPlayer.create(this, R.raw.big_thunder);
                mp1= MediaPlayer.create(this, R.raw.thunder);
                backGround.setVisibility(View.VISIBLE);
                mVideoView.setVisibility(View.INVISIBLE);
                Context context = backGround.getContext();
                int id = context.getResources().getIdentifier("color" + 1 + "_bg", "drawable", context.getPackageName());
                backGround.setImageResource(id);
                getCamera();
                handler = new Handler();
                handler.post(loop);
            }


        } else if (mode == 2) {
            backGround.setVisibility(View.INVISIBLE);
            mVideoView.setVisibility(View.VISIBLE);
            String uriPath = "android.resource://com.example.tonto.zees/" + R.raw.mood;

            Uri uri = Uri.parse(uriPath);
            mVideoView.setVideoURI(uri);
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });
            mVideoView.requestFocus();
            mVideoView.start();
        }

    }

    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.d("Camera error ", e.getMessage());
            }
        }
    }

    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;
        }
    }

    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }


            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;

        }
    }

    @Override
    public void onBackPressed() {
        handler.removeCallbacks(loop);
        super.onBackPressed();

    }

    private Runnable loop = new Runnable() {
        int i = 0;

        @Override
        public void run() {

//                try {
//                    Thread.sleep(4000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            try {

                i++;
                Log.d("abc",i+"");
                if (i % 200 == 0) {
                        if(mp1.isPlaying())
                            mp1.pause();
                        mp.start();
                        turnOnFlash();
                } else if (i % 210 == 0)
                        turnOffFlash();
                    else if (i % 290 == 0) {
                        if(mp.isPlaying())
                        {
                            mp.pause();
                        }
                        mp1.start();
                        turnOnFlash();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        turnOffFlash();
                        mp1.start();
                        turnOnFlash();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        turnOffFlash();
                        mp1.start();
                        turnOnFlash();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        turnOffFlash();

                    }


                handler.postDelayed(this, 20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
