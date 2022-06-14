package com.hemantpatel.snakple;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hemantpatel.snakple.classes.Coordinate;
import com.hemantpatel.snakple.engine.GameEngine;
import com.hemantpatel.snakple.enums.Direction;
import com.hemantpatel.snakple.enums.GameState;
import com.hemantpatel.snakple.views.GameView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    TextView scoreTV;

    private GameEngine gameEngine;
    private GameView gameView;

    private final Handler snakeHandler = new Handler();
    private final Handler appleHandler = new Handler();
    private Runnable snakeRunnable;
    private Runnable appleRunnable;
    private final long snakeUpdateDelay = 150;
    private final long appleUpdateDelay = 153;

    private int currentScore = 0;
    private int time = 0;
    private int sec_for_obstacle = 0;

    private float prevX = 0, prevY = 0;

    public MainActivity() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Game Over 💔");
        builder.setCancelable(false);
        builder.setMessage("Your Score : " + currentScore / 1000 + "\nDo you want to start New game??");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
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
        snakeHandler.removeCallbacks(snakeRunnable);
        appleHandler.removeCallbacks(appleRunnable);
    }
}
