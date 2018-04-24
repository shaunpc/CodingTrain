package com.spc.codingtrain;

import android.content.Context;
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
        Log.i(TAG,"OnCreate completed");
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
        private static final int FRAME_RATE = 500 ; // 1000 = 1 per sec; 20 = 50 frames per sec
        int columns = 10;    // Size of the playing board
        int rows = 10;
        int tileSize;
        int gameCount = 1;
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
                    if (myCanvasView.getWidth()!=0) {
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
        private void updateMyCanvas () {

            if (!started) {   // Initiate key items the very first time

                tileSize = Math.min(myCanvasView.getWidth()/columns, myCanvasView.getHeight()/rows);
                tileSize = Math.min(tileSize, 100);
                Log.i(TAG, "Tile Size will be "+tileSize);

                int x = (myCanvasView.getWidth() - (columns * tileSize))/2;
                int y = myCanvasView.getHeight() - (myCanvasView.getHeight() - (rows * tileSize))/2;

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
                started=true;
            } else {
                // Perform ongoing updates
                if (player.spot >= tiles.size()) {
                    player.spot = tiles.size() - 1;
                    averageRolls = ((averageRolls * gameCount)+player.rolls.size())/(gameCount+1);
                    gameCount++;
                    player.reset();

                } else {
                    player.roll();
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

            // display info
            msg = "Average Rolls to win:" + averageRolls ;
            canvas.drawText(msg, 20, 50, paintText);

            // display info
            if (player.rolls != null) {
                msg = "Current Game Rolls ("+player.rolls.size()+"):" + player.allRolls() ;
                canvas.drawText(msg, 20, 75, paintText);
            }


            for (Tile t : tiles) {
                t.show (canvas);
                if (t.index == player.spot) {
                    player.show(canvas, t);
                }
            }
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

        Paint paintPlayer; // paint to use on this square

        Player () {
            this.reset();
        }

        void reset () {
            this.spot = 0;  // everyone starts at position ZERO
            this.paintPlayer = new Paint();
            this.paintPlayer.setStyle(Paint.Style.FILL);
            this.paintPlayer.setColor(Color.RED);
            this.rolls = new ArrayList<>();
        }

        void roll () {
            Random r = new Random();
            Integer dice = r.nextInt(6) + 1; // returns 1-6
            this.spot = this.spot + dice;
            Log.i(TAG,"Player rolled a "+dice);
            this.rolls.add(dice);
        }


        String allRolls () {
            StringBuilder msg = new StringBuilder (100);
            for (Integer i: this.rolls) {
                if (msg.length() == 0) {
                    msg.append(i);
                } else {
                    msg.append(",");
                    msg.append(i);
                }
            }
            return msg.toString();
        }

        void show (Canvas canvas, Tile tile) {
            canvas.drawOval(tile.getPlayerPos(), paintPlayer);
            Log.i(TAG,"Player is on square "+tile.index);
        }
    }

    class Tile {
        int index;    // number of the tile
        int x, y;   // coords of bottom left corner of tile
        int sz;     // size of the square
        Paint paintTile; // paint to use on this square
        Tile nextTile;  // next tile after this... TODO

        Tile (int index, int x, int y, int sz) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.sz = sz;
            this.nextTile = null;

            this.paintTile = new Paint();
            this.paintTile.setStyle(Paint.Style.FILL);
            this.paintTile.setColor(Color.GREEN);
            this.paintTile.setTextSize(this.sz/3);
            if (this.index % 2 == 0) {
                this.paintTile.setAlpha(100);
            } else {
                this.paintTile.setAlpha(255);
            }

            // Log.i(TAG,"Tile "+this.num + " at ("+x+","+y+")");
        }

        RectF getPlayerPos () {
            return new RectF(this.x + (this.sz/4),this.y- (3*this.sz/4),
                    this.x+ (3*this.sz/4),this.y - (this.sz/4));
        }

        Point2D getTextPos () {
            return new Point2D(this.x+this.sz/3, this.y-this.sz/3);
        }

        void show (Canvas canvas) {

            canvas.drawRect(this.x, this.y, this.x + this.sz, this.y - this.sz, this.paintTile);
            canvas.drawText(Integer.toString(this.index),this.getTextPos().getX(),this.getTextPos().getY(), paintTile);
        }
    }
}