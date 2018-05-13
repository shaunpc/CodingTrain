package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class SnakesAndLadders extends AppCompatActivity {

    private static final String TAG = "SNAKES";
    private String action = "START";
    Button btnAction;
    MyCanvasView myCanvasView;
    //state types
    public final int ROLL = 0;
    public final int MOVE = 1;
    public final int SLIDE = 2;
    public final int FINISHED = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // First, create the overall RelativeLayout view group
        RelativeLayout rLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(rLayout, rlParams);
        rLayout.setBackgroundColor(Color.DKGRAY);

        // Next, add the Button at the bottom of the screen
        btnAction = new Button(this);
        btnAction.setId(R.id.action_button_id);
        btnAction.setText(R.string.action_button_start);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButton();
            }
        });
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        rLayout.addView(btnAction, btnParams);

        // Then, fill the rest with the MyCanvasView class
        myCanvasView = new MyCanvasView(this);
        myCanvasView.setId(R.id.canvas_view_id);
        myCanvasView.setBackgroundColor(Color.BLUE);
        RelativeLayout.LayoutParams cParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ABOVE, btnAction.getId());
        rLayout.addView(myCanvasView, cParams);

        setContentView(rLayout);
        Log.i(TAG, "OnCreate completed");
    }

    void actionButton() {
        if (action.equals("START")) {
            btnAction.setText(R.string.action_button_done);
            action = "DONE";
            myCanvasView.startFrameUpdates();
        } else {
            myCanvasView.stopFrameUpdates();
            finish();
        }
    }

    class MyCanvasView extends View {
        private static final String TAG = "CANVASVIEW";
        public Paint paintCanvas, paintText;
        boolean started = false;
        private Handler handler;
        private static final int FRAME_RATE = 1000; // 1000 = 1 per sec; 20 = 50 frames per sec
        int columns = 10;    // Size of the playing board
        int rows = 10;
        int tileSize;
        int gameCount = 1;
        int state = ROLL;
        float averageRolls = 0;
        List<Tile> tiles = new ArrayList<>();
        Player player = new Player();


        MyCanvasView(Context context) {
            super(context);
            paintCanvas = new Paint();
            paintCanvas.setStyle(Paint.Style.FILL);
            paintCanvas.setColor(Color.WHITE);
            paintText = new Paint();
            paintText.setTextSize(25);
            paintText.setColor(Color.WHITE);

        }

        private Runnable updateFrame = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(updateFrame);
                // KEY FUNCTION CALL - but only when window is ready!
                if (myCanvasView != null) {
                    if (myCanvasView.getWidth() != 0) {
                        updateMyCanvas();
                        myCanvasView.invalidate();
                    }
                }
                handler.postDelayed(updateFrame, FRAME_RATE);
            }
        };

        void startFrameUpdates() {
            handler = new Handler();
            handler.postDelayed(updateFrame, 500);
        }

        void stopFrameUpdates() {
            handler.removeCallbacks(updateFrame);
        }

        //TEMPLATE - KEY FUNCTION - apply any changes to each construct
        private void updateMyCanvas() {

            if (!started) {   // Initiate key items the very first time

                tileSize = Math.min(myCanvasView.getWidth() / columns, myCanvasView.getHeight() / rows);
                tileSize = Math.min(tileSize, 100);
                Log.i(TAG, "Tile Size will be " + tileSize);

                int x = (myCanvasView.getWidth() - (columns * tileSize)) / 2;
                int y = myCanvasView.getHeight() - (myCanvasView.getHeight() - (rows * tileSize)) / 2;

                // create the board
                int dir = 1;
                int num = 0;
                for (int i = 0; i < columns; i++) {
                    for (int j = 0; j < rows; j++) {
                        tiles.add(new Tile(num, x, y, tileSize));
                        num++;
                        if (num % columns == 0) {
                            dir = dir * -1;  // change dir but keep x whereis for next square...
                            y = y - tileSize;
                        } else {
                            x = x + (tileSize * dir);
                        }
                    }
                }

                Random r = new Random();
                int fromTileIndex;
                int toTileIndex;
                int links = Math.round(((columns * rows) / (columns + rows)) * 0.8f);
                // add the snake starting points (number is somewhat dependent on board size)
                for (int i = 0; i < links; i++) {
                    fromTileIndex = r.nextInt(tiles.size() - columns - 1) + columns;
                    toTileIndex = r.nextInt(fromTileIndex-fromTileIndex%columns - 1) + 1;
                    Log.i(TAG, "Snake added from " + fromTileIndex + " to " + toTileIndex);
                    tiles.get(fromTileIndex).setSnadder (tiles.get(toTileIndex), true);
                }
                // add the ladder starting points (number is somewhat dependent on board size)
                // TODO - improve neatness of this random placement...
                //      - eg don't let cross, never sideways, endpoints can't be startpoints, etc
                for (int i = 0; i < links; i++) {
                    fromTileIndex = r.nextInt(tiles.size() - columns - 1) + 1;
                    toTileIndex = r.nextInt(tiles.size() - fromTileIndex - fromTileIndex%columns) + fromTileIndex;
                    Log.i(TAG, "Ladder added from " + fromTileIndex + " to " + toTileIndex);
                    tiles.get(fromTileIndex).setSnadder (tiles.get(toTileIndex), false);
                }
                started = true;

            } else {

                // Perform ongoing updates
                switch (state) {
                    case FINISHED:
                        averageRolls = ((averageRolls * gameCount) + player.rolls.size()) / (gameCount + 1);
                        gameCount++;
                        player.reset();
                        state = ROLL;
                        break;
                    case ROLL:
                        player.roll();
                        state = MOVE;
                        break;
                    case MOVE:
                        player.move();   // adjust if overshot
                        if (player.spot >= tiles.size() - 1) {
                            player.spot = Math.min(player.spot, tiles.size() - 1);
                            state = FINISHED;
                            break;
                        }
                        if (tiles.get(player.spot).snadder != null) {
                            state = SLIDE;
                            break;
                        }
                        state = ROLL;
                        break;
                    case SLIDE:
                        Log.i(TAG, "Player landed on snake/ladder at " + player.spot);
                        int endpoint = tiles.get(player.spot).snadder.index;
                        if (player.spot > endpoint) {
                            player.snakeHits++;
                        } else {
                            player.ladderHits++;
                        }
                        player.spot = tiles.get(player.spot).snadder.index;
                        state = ROLL;
                        break;
                    default:
                        break;
                }
            }
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Perform the actual drawing - effectively called by the 'invalidateCanvas' command
            if (!started) {
                return;
            }

            // display info
            String msg = "Game count:" + gameCount;
            canvas.drawText(msg, 20, 25, paintText);
            msg = "Average Rolls to win:" + averageRolls;
            canvas.drawText(msg, 20, 50, paintText);
            if (player.rolls != null) {
                msg = "Current Game:";
                canvas.drawText(msg, 20, 75, paintText);
                msg = "   Rolls (" + player.rolls.size() + "):" + player.allRolls();
                canvas.drawText(msg, 20, 100, paintText);
                msg = "   Snake/Ladder Hits: " + player.snakeHits + "/" + player.ladderHits;
                canvas.drawText(msg, 20, 125, paintText);
            }

            // display the board
            for (Tile t : tiles) {
                t.show(canvas);
            }

            // display the snakes & ladders
            for (Tile t : tiles) {
                if (t.snadder != null) {
                    t.showLink(canvas, false);
                }
            }

            // highlight move...
            if (state == MOVE) {
                int count = 1;
                while (count <= player.dice && (player.spot+count) < tiles.size()) {
                    tiles.get(player.spot+count).highlight(canvas);
                    count++;
                }
            }

            // highlight slide...
            if (state == SLIDE) {
                tiles.get(player.spot).showLink(canvas, true);
            }

            // display the player  (always last, so on top visually)
            player.show(canvas, tiles.get(player.spot));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int desiredWidth = 500;
            int desiredHeight = 500;

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            int width, height;

            // Measure Width
            if (widthMode == MeasureSpec.EXACTLY) {
                width = widthSize;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desiredWidth, widthSize);
            } else {
                width = desiredWidth;
            }

            // Measure Height
            if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desiredHeight, heightSize);
            } else {
                height = desiredHeight;
            }

            // MUST CALL THIS
            setMeasuredDimension(width, height);
        }

    }

    class Player {
        int spot;
        List<Integer> rolls;
        Integer dice;   // the last random dice number rolled
        Paint paintPlayer; // paint to use on this square
        Random r = new Random();
        int snakeHits, ladderHits;

        Player() {
            this.reset();
        }

        void reset() {
            this.spot = 0;  // everyone starts at position ZERO
            this.paintPlayer = new Paint();
            this.paintPlayer.setStyle(Paint.Style.FILL);
            this.paintPlayer.setColor(Color.BLUE);
            this.rolls = new ArrayList<>();
            this.dice = 0;
            this.snakeHits = 0;
            this.ladderHits = 0;
        }

        void roll() {
            this.dice = this.r.nextInt(6) + 1; // returns 1-6;
        }

        void move() {
            this.spot = this.spot + this.dice;
            this.rolls.add(this.dice);  // store in the dice roll history for this game
        }


        String allRolls() {
            StringBuilder msg = new StringBuilder(100);
            for (Integer i : this.rolls) {
                if (msg.length() == 0) {
                    msg.append(i);
                } else {
                    msg.append(",");
                    msg.append(i);
                }
            }
            return msg.toString();
        }

        void show(Canvas canvas, Tile tile) {
            canvas.drawOval(tile.getPlayerPos(), paintPlayer);
            // Log.i(TAG,"Player is on square "+tile.index);
        }
    }

    class Tile {
        int index;    // number of the tile
        int x, y;   // coords of bottom left corner of tile
        int sz;     // size of the square
        RectF fullRect;  // the full Rectangle of this tile
        RectF innerRect;  // the inner Rectangle of this tile
        Paint paintTile; // paint to use on this square
        Tile snadder;  // if non-zero then slide or climb amount
        Paint paintSnadder, paintHighlight; // paint to use when linking squares & highlighting
        Bitmap bitmap; // to store snadder image



        Tile(int index, int x, int y, int sz) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.sz = sz;
            this.innerRect = new RectF(x + (sz / 4), y - (3 * sz / 4),
                    x + (3 * sz / 4), y - (sz / 4));
            this.fullRect = new RectF(x , y - sz,x + sz, y );
            this.snadder = null;
            this.bitmap = null;

            this.paintTile = new Paint();
            this.paintTile.setStyle(Paint.Style.FILL);
            this.paintTile.setColor(Color.YELLOW);
            this.paintTile.setTextSize(this.sz / 3);
            if (this.index % 2 == 0) {
                this.paintTile.setAlpha(100);
            } else {
                this.paintTile.setAlpha(255);
            }

            this.paintSnadder = new Paint();
            this.paintSnadder.setStyle(Paint.Style.FILL_AND_STROKE);

            this.paintHighlight = new Paint();
            this.paintHighlight.setStyle(Paint.Style.FILL);
            this.paintHighlight.setColor(Color.GREEN);
            this.paintHighlight.setAlpha(100);
        }

        RectF getPlayerPos() {
            return this.innerRect;
        }

        Point2D getTextPos() {
            return new Point2D(this.x + this.sz / 3, this.y - this.sz / 3);
        }

        Point2D getCentre() {
            return new Point2D(this.x + this.sz / 2, this.y - this.sz / 2);
        }

        void setSnadder (Tile toTile, boolean snake) {
            this.snadder = toTile;
            if (snake) {
                this.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.snake);
            } else {
                this.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ladder);
            }
        }

        void showLink(Canvas canvas, boolean highlight) {
            this.paintSnadder.setStrokeWidth(6);
            if (highlight) { this.paintSnadder.setStrokeWidth(10);}

            if (this.snadder.index < this.index) {
                this.paintSnadder.setColor(Color.RED);
            }
            if (this.snadder.index > this.index) {
                paintSnadder.setColor(Color.GREEN);
            }
            canvas.drawLine((float) this.getCentre().getX(), (float) this.getCentre().getY(),
                    (float) this.snadder.getCentre().getX(), (float) this.snadder.getCentre().getY(),
                    this.paintSnadder);
            canvas.drawBitmap(this.bitmap, null, this.fullRect, null );
        }

        void show(Canvas canvas) {
            if (this.index % 2 == 0) {
                this.paintTile.setAlpha(100);
            } else {
                this.paintTile.setAlpha(255);
            }
            canvas.drawRect(this.fullRect, this.paintTile);
            if (this.snadder == null) { // place tile number if not at start of a slide
                canvas.drawText(Integer.toString(this.index), (float) this.getTextPos().getX(), (float) this.getTextPos().getY(), this.paintTile);
            }

        }

        void highlight(Canvas canvas) {
            canvas.drawRect(this.getPlayerPos(), this.paintHighlight);
        }
    }
}