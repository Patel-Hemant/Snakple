package com.hemantpatel.snakple.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.hemantpatel.snakple.R;
import com.hemantpatel.snakple.enums.TileType;

public class GameView extends View {
    private final Paint mPaint = new Paint();
    private TileType[][] snakeViewMap;
    int m, n;
    float tileSizeX;
    float tileSizeY;
    float circleSize;
    Bitmap apple;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        apple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple_3);
    }

    public void setSnakeViewMap(TileType[][] map) {
        this.snakeViewMap = map;
        m = map.length;
        n = map[0].length;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (snakeViewMap != null) {
            tileSizeX = (float) getWidth() / snakeViewMap.length;
            tileSizeY = (float) getWidth() / snakeViewMap.length;
            circleSize = Math.min(tileSizeX, tileSizeY) / 2;
            //tileSizeY = (float) getHeight() / snakeViewMap.length;

            for (int x = 1; x < n - 1; x++) {
                for (int y = 1; y < m - 1; y++) {
                    switch (snakeViewMap[x][y]) {
                        case SnakeHead:
                            mPaint.setColor(Color.BLACK);
                            canvas.drawCircle(x * tileSizeX + tileSizeX / 2f + circleSize / 2, y * tileSizeY + tileSizeY / 2f + circleSize / 2, circleSize, mPaint);
                            break;
                        case SnakeBody:
                            mPaint.setColor(Color.GRAY);
                            canvas.drawCircle(x * tileSizeX + tileSizeX / 2f + circleSize / 2, y * tileSizeY + tileSizeY / 2f + circleSize / 2, circleSize, mPaint);
                            break;
                        case Apple:
                            mPaint.setColor(Color.RED);
                            canvas.drawCircle(x * tileSizeX + tileSizeX / 2f + circleSize / 2, y * tileSizeY + tileSizeY / 2f + circleSize / 2, circleSize, mPaint);
                            drawApple(canvas, x * tileSizeX, y * tileSizeY);
                            break;
                    }
                }
            }
            drawWall(canvas);
        }
    }

    protected void drawWall(Canvas canvas) {
        mPaint.setColor(Color.rgb(179, 82, 4));
        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        canvas.drawRect(0, 0, n * tileSizeX, tileSizeY, mPaint);
        canvas.drawRect(0, tileSizeY * (m - 1), n * tileSizeX, tileSizeY * m, mPaint);
        canvas.drawRect(0, 0, tileSizeX, n * tileSizeY, mPaint);
        canvas.drawRect((n - 1) * tileSizeX, 0, m * tileSizeX, n * tileSizeY, mPaint);

        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL);
    }

    protected void drawApple(Canvas canvas, float x, float y) {
        Rect rect = new Rect(0, 0, (int) (2 * tileSizeX), (int) (2 * tileSizeX));
        rect.offset((int) (x + 5 - tileSizeX / 2), (int) (y - tileSizeY / 2));

        canvas.drawBitmap(apple, null, rect, null);
    }
}