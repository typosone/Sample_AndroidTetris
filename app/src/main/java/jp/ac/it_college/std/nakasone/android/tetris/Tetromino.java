package jp.ac.it_college.std.nakasone.android.tetris;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 */
public class Tetromino {
    private static Paint paint = new Paint();
    private Coordinate base;
    private Type type;
    private Orientation orientation;
    private Coordinate[] blockBoardCoordinates = Coordinate.asArray(0, 0, 0, 0, 0, 0, 0, 0);
    private RectF dst = new RectF();

    public Tetromino() {
        base = new Coordinate(0, 0);
        type = Type.nextType();
        orientation = Orientation.Right;
        calcBlockBoardCoordinates();
    }

    private void calcBlockBoardCoordinates() {
        Coordinate[] localCoordinates = type.getLocalBlockCoordinates(orientation);
        for (int i = 0; i < localCoordinates.length; i++) {
            Coordinate local = localCoordinates[i];
            blockBoardCoordinates[i].x = local.x + base.x;
            blockBoardCoordinates[i].y = local.y + base.y;
        }
    }

    public void setPosition(int x, int y) {
        base.x = x;
        base.y = y;
        calcBlockBoardCoordinates();
    }

    public void draw(Canvas canvas) {
        float side = canvas.getWidth() / 10.0f;

        for (Coordinate point : blockBoardCoordinates) {
            dst.set(point.x, (20 - point.y), point.x + side, (20 - point.y - side));
            canvas.drawBitmap(Type.blockBitmap, type.getRect(), dst, paint);
        }
    }

    public enum Orientation {
        Right, Down, Left, Up,;
    }

    public enum Type {
        I(1), O(2), S(3), Z(4), J(5), L(6), T(7),;

        private static final Map<Type, Map<Orientation, Coordinate[]>> LOCAL_BLOCK_COORDINATES;
        private static final int SHUFFLE_COUNT = 100;
        private static Bitmap blockBitmap = null;
        private static LinkedList<Type> queue = new LinkedList<>();
        private static Random random = new Random();
        private static Map<Type, Rect> blockRect = new HashMap<>();

        static {
            LOCAL_BLOCK_COORDINATES = new HashMap<>();
            HashMap<Orientation, Coordinate[]> coordinates;

            coordinates = new HashMap<>();
            coordinates.put(Orientation.Right, Coordinate.asArray(0, 1, 1, 1, 2, 1, 3, 1));
            coordinates.put(Orientation.Down, Coordinate.asArray(1, 0, 1, 1, 1, 2, 1, 3));
            coordinates.put(Orientation.Left, Coordinate.asArray(0, 2, 1, 2, 2, 2, 3, 2));
            coordinates.put(Orientation.Up, Coordinate.asArray(2, 0, 2, 1, 2, 2, 2, 3));
            LOCAL_BLOCK_COORDINATES.put(I, coordinates);

            coordinates = new HashMap<>();
            coordinates.put(Orientation.Right, Coordinate.asArray(0, 0, 0, 1, 1, 0, 1, 1));
            coordinates.put(Orientation.Down, Coordinate.asArray(0, 0, 0, 1, 1, 0, 1, 1));
            coordinates.put(Orientation.Left, Coordinate.asArray(0, 0, 0, 1, 1, 0, 1, 1));
            coordinates.put(Orientation.Up, Coordinate.asArray(0, 0, 0, 1, 1, 0, 1, 1));
            LOCAL_BLOCK_COORDINATES.put(O, coordinates);

            coordinates = new HashMap<>();
            coordinates.put(Orientation.Right, Coordinate.asArray(1, 0, 1, 1, 2, 1, 2, 2));
            coordinates.put(Orientation.Down, Coordinate.asArray(2, 0, 1, 0, 1, 1, 0, 1));
            coordinates.put(Orientation.Left, Coordinate.asArray(1, 0, 1, 1, 2, 1, 2, 2));
            coordinates.put(Orientation.Up, Coordinate.asArray(2, 0, 1, 0, 1, 1, 0, 1));
            LOCAL_BLOCK_COORDINATES.put(S, coordinates);

            coordinates = new HashMap<>();
            coordinates.put(Orientation.Right, Coordinate.asArray(2, 0, 2, 1, 1, 1, 1, 2));
            coordinates.put(Orientation.Down, Coordinate.asArray(2, 2, 1, 2, 1, 1, 0, 1));
            coordinates.put(Orientation.Left, Coordinate.asArray(2, 0, 2, 1, 1, 1, 1, 2));
            coordinates.put(Orientation.Up, Coordinate.asArray(2, 2, 1, 2, 1, 1, 0, 1));
            LOCAL_BLOCK_COORDINATES.put(Z, coordinates);

            coordinates = new HashMap<>();
            coordinates.put(Orientation.Right, Coordinate.asArray(2, 0, 1, 0, 1, 1, 1, 2));
            coordinates.put(Orientation.Down, Coordinate.asArray(2, 2, 2, 1, 1, 1, 0, 1));
            coordinates.put(Orientation.Left, Coordinate.asArray(1, 0, 1, 1, 1, 2, 0, 2));
            coordinates.put(Orientation.Up, Coordinate.asArray(0, 0, 0, 1, 1, 1, 2, 1));
            LOCAL_BLOCK_COORDINATES.put(J, coordinates);

            coordinates = new HashMap<>();
            coordinates.put(Orientation.Right, Coordinate.asArray(0, 0, 1, 0, 1, 1, 1, 2));
            coordinates.put(Orientation.Down, Coordinate.asArray(2, 0, 2, 1, 1, 1, 0, 1));
            coordinates.put(Orientation.Left, Coordinate.asArray(1, 0, 1, 1, 1, 2, 2, 2));
            coordinates.put(Orientation.Up, Coordinate.asArray(2, 1, 1, 1, 0, 1, 0, 2));
            LOCAL_BLOCK_COORDINATES.put(L, coordinates);

            coordinates = new HashMap<>();
            coordinates.put(Orientation.Right, Coordinate.asArray(2, 1, 1, 1, 0, 1, 1, 2));
            coordinates.put(Orientation.Down, Coordinate.asArray(1, 0, 1, 1, 1, 2, 0, 1));
            coordinates.put(Orientation.Left, Coordinate.asArray(2, 1, 1, 1, 0, 1, 1, 0));
            coordinates.put(Orientation.Up, Coordinate.asArray(1, 0, 1, 1, 1, 2, 2, 1));
            LOCAL_BLOCK_COORDINATES.put(T, coordinates);
        }

        private final int id;

        Type(int id) {
            this.id = id;
        }

        public static Type nextType() {
            if (queue.size() == 0) {
                generateQueue();
            }
            return queue.remove();
        }

        public static void generateQueue() {
            queue.addAll(Arrays.asList(I, O, S, Z, J, L, T));
            for (int i = 0; i < SHUFFLE_COUNT; i++) {
                int src = random.nextInt(queue.size());
                int dst = random.nextInt(queue.size());
                Type tmp = queue.get(src);
                queue.set(src, queue.get(dst));
                queue.set(dst, tmp);
            }
        }

        public static boolean isBitmapInitialized() {
            return blockBitmap != null;
        }

        public static void setBlockBitmap(Bitmap bitmap) {
            blockBitmap = bitmap;
            int side = blockBitmap.getWidth();
            blockRect.put(I, new Rect(0, 0 * side, side, (0 + 1) * side));
            blockRect.put(O, new Rect(0, 1 * side, side, (1 + 1) * side));
            blockRect.put(S, new Rect(0, 2 * side, side, (2 + 1) * side));
            blockRect.put(Z, new Rect(0, 3 * side, side, (3 + 1) * side));
            blockRect.put(J, new Rect(0, 4 * side, side, (4 + 1) * side));
            blockRect.put(L, new Rect(0, 5 * side, side, (5 + 1) * side));
            blockRect.put(T, new Rect(0, 6 * side, side, (6 + 1) * side));
        }

        public int getId() {
            return id;
        }

        public Rect getRect() {
            return blockRect.get(this);
        }

        public Coordinate[] getLocalBlockCoordinates(Orientation orientation) {
            return LOCAL_BLOCK_COORDINATES.get(this).get(orientation);
        }
    }
}
