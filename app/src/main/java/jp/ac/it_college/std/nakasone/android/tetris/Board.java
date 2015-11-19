package jp.ac.it_college.std.nakasone.android.tetris;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 */
public class Board extends SurfaceView implements SurfaceHolder.Callback {
    public static final int FPS = 30;
    private SurfaceHolder holder;
    private DrawThread thread;
    private Bitmap blocks;
    private Tetromino fallingTetromino;
    private ArrayList<Tetromino> tetrominoList = new ArrayList<>();
    private long count = 0;

    public Board(Context context) {
        super(context);
        initialize(context);
    }

    public Board(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public Board(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        getHolder().addCallback(this);
        blocks = BitmapFactory.decodeResource(context.getResources(), R.raw.block);
        spawnTetromino();
    }

    public void spawnTetromino() {
        fallingTetromino = new Tetromino(this);
        fallingTetromino.setPosition(5, 23);
    }

    public boolean fallTetromino() {
        fallingTetromino.move(Tetromino.Orientation.Down);
        if (!isValidPosition()) {
            fallingTetromino.move(Tetromino.Orientation.Up);
            return false;
        }
        return true;
    }

    public boolean isValidPosition() {
        boolean overlapping = false;
        for (Tetromino fixedTetromino: tetrominoList) {
            if (fallingTetromino.intersect(fixedTetromino)) {
                overlapping = true;
                break;
            }
        }

        return !(overlapping || fallingTetromino.isOutOfBounds());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
        startThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopThread();
    }

    public void translateCanvasCoordinate(Canvas canvas, RectF rectF, int gx, int gy) {
        float side = canvas.getWidth() / 10.0f;
        gy = 20 - gy;
        rectF.set(side * gx, side * gy, side * (gx + 1), side * (gy + 1));
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) {
            return;
        }
        if (!Tetromino.Type.isBitmapInitialized()) {
            Tetromino.Type.setBlockBitmap(blocks);
        }
        updateGame();

        canvas.drawColor(Color.LTGRAY); // 画面クリア(単色塗りつぶし)

        for (Tetromino tetromino : tetrominoList) {
            tetromino.draw(canvas);
        }
        fallingTetromino.draw(canvas);
    }

    private void updateGame() {
        if (count++ % (FPS / 2) != 0) {
            return;
        }
        if (!fallTetromino()) {
            tetrominoList.add(fallingTetromino);
            spawnTetromino();
        }
    }

    private void startThread() {
        stopThread();

        thread = new DrawThread();
        thread.start();
    }

    private void stopThread() {
        if (thread != null) {
            thread.isFinished = true;
            thread = null;
        }
    }

    private class DrawThread extends Thread {
        private boolean isFinished;

        @Override
        public void run() {
            long prevTime = 0;
            while (!isFinished) {
                if (holder == null ||
                        System.currentTimeMillis() - prevTime < 1000 / FPS) {
                    try {
                        sleep(1000 / FPS / 3);
                    } catch (InterruptedException e) {
                        Log.w("DrawThread", e.getMessage(), e);
                    }
                    continue;
                }

                Canvas c = null;
                try {
                    c = holder.lockCanvas(null);
                    synchronized (holder) {
                        draw(c);
                    }
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c);
                    }
                }
                prevTime = System.currentTimeMillis();
            }
        }
    }
}
