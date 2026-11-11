package com.example.prankapp;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isExiting = false;

    private final Runnable boosterRunnable = new Runnable() {
        @Override
        public void run() {
            if (isExiting || isFinishing() || isDestroyed()) {
                return;
            }

            maxVolumeLoop();
            hideSystemUI();

            handler.postDelayed(this, 1000);
        }
    };

    // Все 16 звуков строго по порядку
    private final int[] soundList = {
            R.raw.sound1,
            R.raw.sound2,
            R.raw.sound3,
            R.raw.sound4,
            R.raw.sound5,
            R.raw.sound6,
            R.raw.sound7,
            R.raw.sound8,
            R.raw.sound9,
            R.raw.sound10,
            R.raw.sound11,
            R.raw.sound12,
            R.raw.sound13,
            R.raw.sound14,
            R.raw.sound15,
            R.raw.sound16
    };

    private int currentSoundIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Сразу максимальная громкость
        maxVolumeLoop();

        // Сразу полноэкранный режим
        hideSystemUI();

        // Запуск закрепления приложения
        try {
            startLockTask();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Запускаем плейлист
        playNextSound();

        // Секретная кнопка выхода
        Button secretExit = findViewById(R.id.btnSecretExit);

        if (secretExit != null) {
            secretExit.setOnClickListener(v -> stopPrankAndExit());
        }

        // Блокировка кнопки "Назад"
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Не закрываем приложение
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        handler.removeCallbacks(boosterRunnable);
        handler.post(boosterRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(boosterRunnable);
    }

    // Последовательное воспроизведение звуков по кругу
    private void playNextSound() {
        releaseMediaPlayer();

        if (isFinishing() || isDestroyed() || isExiting) {
            return;
        }

        mediaPlayer = MediaPlayer.create(this, soundList[currentSoundIndex]);

        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> {
                currentSoundIndex =
                        (currentSoundIndex + 1) % soundList.length;

                playNextSound();
            });

            mediaPlayer.start();
        }
    }

    // Освобождение MediaPlayer
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setOnCompletionListener(null);

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }

                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mediaPlayer = null;
        }
    }

    // Максимальная громкость
    private void maxVolumeLoop() {
        if (audioManager != null) {
            int maxVol = audioManager.getStreamMaxVolume(
                    AudioManager.STREAM_MUSIC
            );

            audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    maxVol,
                    0
            );
        }
    }

    // Перехват кнопок громкости
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

            maxVolumeLoop();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    // Полноэкранный режим
    private void hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.R) {

            getWindow().setDecorFitsSystemWindows(false);

            WindowInsetsController controller =
                    getWindow().getInsetsController();

            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());

                controller.setSystemBarsBehavior(
                        WindowInsetsController
                                .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }

        } else {
            View decorView = getWindow().getDecorView();

            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && !isExiting) {
            hideSystemUI();
        }
    }

    // Секретный выход
    private void stopPrankAndExit() {
        if (isExiting) {
            return;
        }

        isExiting = true;

        handler.removeCallbacks(boosterRunnable);
        releaseMediaPlayer();

        try {
            stopLockTask();
        } catch (Exception e) {
            e.printStackTrace();
        }

        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(boosterRunnable);
        releaseMediaPlayer();

        super.onDestroy();
    }
}
