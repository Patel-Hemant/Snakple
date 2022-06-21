package com.hemantpatel.snakple;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor mEditor;
    Switch sound, music, vibrate;
    boolean sound_data;
    boolean music_data;
    boolean vibrate_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Setting");

        sharedPreferences = getSharedPreferences("SettingData", MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor mEditor = sharedPreferences.edit();

        sound = findViewById(R.id.switch_sound);
        music = findViewById(R.id.switch_music);
        vibrate = findViewById(R.id.switch_vibration);

        getData();
        sound.setChecked(sound_data);
        music.setChecked(music_data);
        vibrate.setChecked(vibrate_data);

        sound.setOnClickListener(v -> saveData());
        music.setOnClickListener(v -> saveData());
        vibrate.setOnClickListener(v -> saveData());
    }

    private void saveData() {
        sound_data = sound.isChecked();
        music_data = music.isChecked();
        vibrate_data = vibrate.isChecked();

        mEditor = sharedPreferences.edit();
        mEditor.putBoolean("SOUND_KEY", sound_data);
        mEditor.putBoolean("MUSIC_KEY", music_data);
        mEditor.putBoolean("VIBRATE_KEY", vibrate_data);
        mEditor.commit();
    }

    private void getData() {
        sound_data = sharedPreferences.getBoolean("SOUND_KEY", true);
        music_data = sharedPreferences.getBoolean("MUSIC_KEY", true);
        vibrate_data = sharedPreferences.getBoolean("VIBRATE_KEY", true);
    }
}