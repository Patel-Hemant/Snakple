package com.hemantpatel.snakple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hemantpatel.snakple.classes.Coordinate;
import com.hemantpatel.snakple.engine.GameEngine;
import com.hemantpatel.snakple.enums.Direction;
import com.hemantpatel.snakple.enums.GameState;
import com.hemantpatel.snakple.views.GameView;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private MediaPlayer mPlayer;
    private Uri snake_bull, snake_running, snake_running2, game_bg_music, hit_wall, fire_burn;
    SharedPreferences sharedPreferences;
    boolean sound_data;
    boolean music_data;
    boolean vibrate_data;

    private TextView scoreTV;

    private GameEngine gameEngine;
    private GameView gameView;

    private final Handler snakeHandler = new Handler();
    private final Handler appleHandler = new Handler();
    private Runnable snakeRunnable;
    private Runnable appleRunnable;
    private final long snakeUpdateDelay = 180;
    private final long appleUpdateDelay = 183;

    private int currentScore = 0;
    private int time = 0;
    private int sec_for_obstacle = 0;

    private float prevX = 0, prevY = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        sharedPreferences = getSharedPreferences("SettingData", MODE_PRIVATE);
        sound_data = sharedPreferences.getBoolean("SOUND_KEY", true);
        music_data = sharedPreferences.getBoolean("MUSIC_KEY", true);
        vibrate_data = sharedPreferences.getBoolean("VIBRATE_KEY", true);

        mPlayer = new MediaPlayer();
        mPlayer.setVolume(0.8f, 0.8f);

        snake_bull = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.snake_bull);
        snake_running = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.snake_running);
        snake_running2 = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.snake_running2);
        game_bg_music = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.game_bg_music);
        hit_wall = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.hit_wall);
        fire_burn = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.fire_burn);

        playSound(game_bg_music, true, music_data);

        scoreTV = findViewById(R.id.score_view);

        gameEngine = new GameEngine();
        gameEngine.initGame();

        gameView = (GameView) findViewById(R.id.gameView);
        gameView.setOnTouchListener(this);

        snakeRunnable = new Runnable() {
            @Override
            public void run() {
                gameEngine.updateSnakePosition();
                gameView.setSnakeViewMap(gameEngine.getMap());
                gameView.invalidate();

                // Turn the Snake for catch the apple
                turnSnake();

                // update the score
                time += snakeUpdateDelay;
                if (time >= 1000) {
                    sec_for_obstacle++;
                    if (sec_for_obstacle >= 5) {
                        gameEngine.addObstacle();
                        sec_for_obstacle = 0;
                    }

                    currentScore += 1000;
                    String data = (currentScore < 9000) ? "Current Score : 0" : "Current Score : ";
                    data += currentScore / 1000;
                    scoreTV.setText(data);
                    time = 0;
                }

                if (gameEngine.getCurrentGameState() == GameState.Running) {
                    snakeHandler.postDelayed(this, snakeUpdateDelay);
                } else if (gameEngine.getCurrentGameState() == GameState.Lost) {
                    OnGameLost();
                }
            }
        };
        appleRunnable = new Runnable() {
            @Override
            public void run() {
                gameEngine.updateApplePosition();
                if (gameEngine.getCurrentGameState() == GameState.Running) {
                    appleHandler.postDelayed(this, appleUpdateDelay);
                } else if (gameEngine.getCurrentGameState() == GameState.Lost) {
                    OnGameLost();
                }
            }
        };

        startSnakeUpdateHandler();
        startAppleUpdateHandler();
    }

    private void startSnakeUpdateHandler() {
        snakeHandler.postDelayed(snakeRunnable, snakeUpdateDelay);
    }

    private void startAppleUpdateHandler() {
        appleHandler.postDelayed(appleRunnable, appleUpdateDelay);
    }

    private void OnGameLost() {
        if (vibrate_data) vibrate();
        String death_note = "";
        switch (gameEngine.gameOverState) {
            case ByWallCrashed:
                death_note = "Crashed on Wall! 🧱";
                playSound(hit_wall, false, sound_data);
                break;
            case EatenBySnake:
                death_note = "Eaten by Snake! 🐍";
                playSound(snake_bull, false, sound_data);
                break;
            case BurnedByFire:
                death_note = "Burned in Fire! 🔥";
                playSound(fire_burn, false, sound_data);
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Game Over 💔");
        builder.setCancelable(false);
        builder.setMessage("Your Score : " + currentScore / 1000 + "\n" + death_note + "\nDo you want to start New game??");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // For restart the activity
                // Intent intent = getIntent();
                // finish();
                // startActivity(intent);

                Intent intent = getIntent();
                overridePendingTransition(0, 0);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);

                // For restart the app
                // Intent i = getBaseContext().getPackageManager()
                //         .getLaunchIntentForPackage(getBaseContext().getPackageName());
                // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // startActivity(i);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void playSound(Uri uri, boolean isLoop, boolean volumeOn) {
        float volume = (volumeOn) ? 0.8f : 0;
        mPlayer.reset();
        mPlayer.setLooping(isLoop);
        mPlayer.setVolume(volume, volume);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(getApplicationContext(), uri);
            mPlayer.prepare();
            mPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 1000 milliseconds after 500 ms
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    v.vibrate(1000);
                }
            }
        }, 1000);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevX = event.getX();
                prevY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float newX = event.getX();
                float newY = event.getY();

                //calculate where we swiped
                if (Math.abs(newX - prevX) > Math.abs(newY - prevY)) {
                    //Left-Right direction
                    if (newX > prevX) {
                        //Right
                        turnRightApple();
                    } else {
                        //Left
                        turnLeftApple();
                    }
                } else {
                    //Up-Down direction
                    if (newY > prevY) {
                        //Down
                        turnDownApple();
                    } else {
                        //Up
                        turnTopApple();
                    }
                }
                break;
        }
        return true;
    }

    // Turn function for snake
    public void turnLeftSnake() {
        gameEngine.UpdateSnakeDirection(Direction.West);
    }

    public void turnDownSnake() {
        gameEngine.UpdateSnakeDirection(Direction.South);
    }

    public void turnRightSnake() {
        gameEngine.UpdateSnakeDirection(Direction.East);
    }

    public void turnTopSnake() {
        gameEngine.UpdateSnakeDirection(Direction.North);
    }

    public void turnSnake() {
        Coordinate apple = gameEngine.getApple();
        Coordinate snake = gameEngine.getSnakeHead();

        // First check the box for apple
        // and move according it
        if (snake.getX() > apple.getX() && snake.getY() > apple.getY()) {
//            Toast.makeText(MainActivity.this, "1", Toast.LENGTH_SHORT).show();
            if (gameEngine.snakeDirection.equals(Direction.East)) {
                turnTopSnake();
//                turnLeftSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.West)) {
                turnTopSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.North)) {
                turnLeftSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.South)) {
                turnLeftSnake();
//                turnTopSnake();
            }
        } else if (snake.getX() < apple.getX() && snake.getY() > apple.getY()) {
//            Toast.makeText(MainActivity.this, "2", Toast.LENGTH_SHORT).show();
            if (gameEngine.snakeDirection.equals(Direction.East)) {
                turnTopSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.West)) {
                turnTopSnake();
//                turnRightSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.North)) {
                turnRightSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.South)) {
                turnRightSnake();
//                turnTopSnake();
            }
        } else if (snake.getX() < apple.getX() && snake.getY() < apple.getY()) {
//            Toast.makeText(MainActivity.this, "3", Toast.LENGTH_SHORT).show();
            if (gameEngine.snakeDirection.equals(Direction.East)) {
                turnDownSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.West)) {
                turnDownSnake();
//                turnRightSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.North)) {
                turnRightSnake();
//                turnDownSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.South)) {
                turnRightSnake();
            }
        } else if (snake.getX() > apple.getX() && snake.getY() < apple.getY()) {
//            Toast.makeText(MainActivity.this, "4", Toast.LENGTH_SHORT).show();
            if (gameEngine.snakeDirection.equals(Direction.East)) {
                turnDownSnake();
//                turnLeftSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.West)) {
                turnDownSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.North)) {
                turnLeftSnake();
//                turnDownSnake();
            } else if (gameEngine.snakeDirection.equals(Direction.South)) {
                turnLeftSnake();
            }
        } else if (snake.getX() == apple.getX() && snake.getY() < apple.getY()) {
            turnDownSnake();
        } else if (snake.getX() == apple.getX() && snake.getY() > apple.getY()) {
            turnTopSnake();
        } else if (snake.getX() > apple.getX() && snake.getY() == apple.getY()) {
            turnLeftSnake();
        } else if (snake.getX() < apple.getX() && snake.getY() == apple.getY()) {
            turnRightSnake();
        }

        // update snake direction for update the image of snake's head
        if (gameView != null && gameEngine != null)
            gameView.setSnakeDirection(gameEngine.snakeDirection);
    }

    // Turn function for apple
    public void turnLeftApple() {
        gameEngine.UpdateAppleDirection(Direction.West);
    }

    public void turnDownApple() {
        gameEngine.UpdateAppleDirection(Direction.South);
    }

    public void turnRightApple() {
        gameEngine.UpdateAppleDirection(Direction.East);
    }

    public void turnTopApple() {
        gameEngine.UpdateAppleDirection(Direction.North);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.reset();
        snakeHandler.removeCallbacks(snakeRunnable);
        appleHandler.removeCallbacks(appleRunnable);
    }
}
