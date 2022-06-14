package com.hemantpatel.snakple.engine;

import android.util.Log;

import com.hemantpatel.snakple.classes.Coordinate;
import com.hemantpatel.snakple.enums.Direction;
import com.hemantpatel.snakple.enums.GameState;
import com.hemantpatel.snakple.enums.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {
    public static final int GameWidth = 40;
    public static final int GameHeight = 40;

    private final List<Coordinate> walls = new ArrayList<>();
    private List<Coordinate> snake = new ArrayList<>();
    private Coordinate apple = new Coordinate(GameHeight / 2, GameHeight / 2);

    private final Random random = new Random();

    public int score = 0;

    public Direction snakeDirection = Direction.East;
    public Direction appleDirection = Direction.East;
    private GameState currentGameState = GameState.Running;

    public Coordinate getSnakeHead() {
        return snake.get(0);
    }

    public Coordinate getApple() {
        return apple;
    }

    public GameEngine() {

    }

    public void initGame() {
        AddSnake();
        AddWalls();
        AddApples();

        score++;
    }

    public void UpdateSnakeDirection(Direction newDirection) {
        if (Math.abs(newDirection.ordinal() - snakeDirection.ordinal()) % 2 == 1) {
            snakeDirection = newDirection;
        }
    }

    public void UpdateAppleDirection(Direction newDirection) {
        if (Math.abs(newDirection.ordinal() - appleDirection.ordinal()) % 2 == 1) {
            appleDirection = newDirection;
        }
    }

    public void updateSnakePosition() {
        //update the snake
        switch (snakeDirection) {
            case North:
                UpdateSnake(0, -1);
                break;
            case East:
                UpdateSnake(1, 0);
                break;
            case South:
                UpdateSnake(0, 1);
                break;
            case West:
                UpdateSnake(-1, 0);
                break;
        }
        // also check for collision
        checkCollision();
    }

    public void updateApplePosition() {
        // update the apple
        switch (appleDirection) {
            case North:
                UpdateApple(0, -1);
                break;
            case East:
                UpdateApple(1, 0);
                break;
            case South:
                UpdateApple(0, 1);
                break;
            case West:
                UpdateApple(-1, 0);
                break;
        }
    }

    public void checkCollision() {
        // check wall collision
        for (Coordinate w : walls) {
            if (apple.equals(w)) {
                currentGameState = GameState.Lost;
                return;
            }
        }

        // check snake collision
        for (int i = 0; i < snake.size(); i++) {
            if (apple.equals(snake.get(i))) {
                currentGameState = GameState.Lost;
                return;
            }
        }
    }

    public TileType[][] getMap() {
        TileType[][] map = new TileType[GameWidth][GameHeight];

        for (int x = 0; x < GameWidth; x++) {
            for (int y = 0; y < GameHeight; y++) {
                map[x][y] = TileType.Nothing;
            }
        }

        for (Coordinate wall : walls) {
            try {
                map[wall.getX()][wall.getY()] = TileType.Wall;
            } catch (Exception e) {
                Log.e("error_snake", e.toString());
                //Toast.makeText(this, ""+e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        for (Coordinate s : snake) {
            map[s.getX()][s.getY()] = TileType.SnakeBody;
        }

        map[apple.getX()][apple.getY()] = TileType.Apple;
        map[snake.get(0).getX()][snake.get(0).getY()] = TileType.SnakeHead;

        return map;
    }

    private void UpdateSnake(int x, int y) {
        for (int i = snake.size() - 1; i > 0; i--) {
            snake.get(i).setX(snake.get(i - 1).getX());
            snake.get(i).setY(snake.get(i - 1).getY());
        }

        snake.get(0).setX(snake.get(0).getX() + x);
        snake.get(0).setY(snake.get(0).getY() + y);
    }

    private void UpdateApple(int x, int y) {
        apple.setX(apple.getX() + x);
        apple.setY(apple.getY() + y);
    }

    private void AddSnake() {
        snake.clear();
        snake.add(new Coordinate(7, 7));
        snake.add(new Coordinate(6, 7));
        snake.add(new Coordinate(5, 7));
        snake.add(new Coordinate(4, 7));
        snake.add(new Coordinate(3, 7));
        snake.add(new Coordinate(2, 7));
    }

    private void AddWalls() {
        //Top and bottom walls
        for (int x = 0; x < GameWidth; x++) {
            walls.add(new Coordinate(x, 0));
            walls.add(new Coordinate(x, GameHeight - 1));
        }

        //Left and Right walls
        for (int y = 0; y < GameHeight; y++) {
            walls.add(new Coordinate(0, y));
            walls.add(new Coordinate(GameHeight - 1, y));
        }
    }

    private void AddApples() {
        Coordinate coordinate = null;

        boolean added = false;
        while (!added) {
            // use 5 and 10 for padding from walls
            int x = 5 + random.nextInt(GameWidth - 10);
            int y = 5 + random.nextInt(GameHeight - 10);

            coordinate = new Coordinate(x, y);
            boolean collision = false;
            for (Coordinate s : snake) {
                if (s.equals(coordinate)) {
                    collision = true;
                    break;
                }
            }

            added = !collision;
        }
        apple = coordinate;
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }
}
