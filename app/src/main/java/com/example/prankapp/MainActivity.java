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
    private final Handler handler = new Handler(Looper.getMainThreadLooper());

    // Фоновый поток, который каждую секунду держит громкость на максимуме и скрывает панели
    private final Runnable boosterRunnable = new Runnable() {
        @Override
        public void run() {
            maxVolumeLoop();
            hideSystemUI();
            handler.postDelayed(this, 1000); // Повторять каждую 1 секунду
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

        // Инициализируем аудио менеджер
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Сразу делаем звук на максимум и скрываем интерфейс
        maxVolumeLoop();
        hideSystemUI();

        // Запускаем бесконечный плейлист
        playNextSound();

        // Обработчик секретной невидимой кнопки в левом нижнем углу
        Button secretExit = findViewById(R.id.btnSecretExit);
        if (secretExit != null) {
            secretExit.setOnClickListener(v -> stopPrankAndExit());
        }

        // Блокируем кнопку "Назад"
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Ничего не делаем
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Включаем жесткий контроль громкости и полног экрана при возврате в приложение
        handler.post(boosterRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Останавливаем цикл, когда приложение свернуто (чтобы не тратить ресурсы)
        handler.removeCallbacks(boosterRunnable);
    }

    // Функция последовательного воспроизведения звуков по кругу
    private void playNextSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        if (isFinishing() || isDestroyed()) {
            return;
        }

        mediaPlayer = MediaPlayer.create(this, soundList[currentSoundIndex]);

        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> {
                currentSoundIndex = (currentSoundIndex + 1) % soundList.length;
                playNextSound();
            });

            mediaPlayer.start();
        }
    }

    // Принудительное выставление максимальной громкости
    private void maxVolumeLoop() {
        if (audioManager != null) {
            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
        }
    }

    // Блокируем качельку громкости
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            maxVolumeLoop();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Жесткий полноэкранный режим
    private void hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                // TRANSIENT_BARS_BY_SWIPE скрывает панели даже если пользователь попытался свайпнуть
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    // Выход при нажатии на секретную невидимую кнопку
    private void stopPrankAndExit() {
        handler.removeCallbacks(boosterRunnable);
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
        finishAffinity();
    }
}
