package com.hemantpatel.snakple;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    public void openActivity(View view) {
        switch (view.getId()) {
            case R.id.play_btn:
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                break;
            case R.id.setting_btn:
                startActivity(new Intent(HomeActivity.this, SettingActivity.class));
                break;
            case R.id.exit_btn:
                finish();
                break;
        }

    }
}