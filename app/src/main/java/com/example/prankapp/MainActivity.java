package com.example.prankapp;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    // Список твоих звуков из папки res/raw/ (без расширения .mp3)
    private final int[] soundList = {
        R.raw.sound1,
        R.raw.sound2,
        R.raw.sound3
    };
    private int currentSoundIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Скрываем системные панели
        hideSystemUI();

        // Настраиваем звук
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolumeLoop();

        // Запускаем бесконечный плейлист
        playNextSound();

        // Обработчик секретной невидимой кнопки в левом нижнем углу
        Button secretExit = findViewById(R.id.btnSecretExit);
        secretExit.setOnClickListener(v -> stopPrankAndExit());
    }

    // Функция последовательного воспроизведения звуков по кругу
    private void playNextSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        // Создаем плеер для текущего звука из массива
        mediaPlayer = MediaPlayer.create(this, soundList[currentSoundIndex]);

        if (mediaPlayer != null) {
            // Когда текущий звук заканчивается — запускаем следующий
            mediaPlayer.setOnCompletionListener(mp -> {
                currentSoundIndex = (currentSoundIndex + 1) % soundList.length; // Переход к след. треку или в начало
                playNextSound();
            });

            mediaPlayer.start();
        }
    }

    // Функция постоянного выставления максимальной громкости
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

    // Блокируем кнопку "Назад"
    @Override
    public void onBackPressed() {
        // Ничего не делаем
    }

    private void hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_STR_BARS_BY_SWIPE);
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

    // Выход при нажатии на секретную кнопку
    private void stopPrankAndExit() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        finishAffinity();
    }
}
