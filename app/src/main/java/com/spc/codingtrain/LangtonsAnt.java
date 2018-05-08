package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

public class LangtonsAnt extends AppCompatActivity {

    private static final String TAG = "ANT";
    private String action = "START";
    Button btnAction;
    MyCanvasView myCanvasView;
    static final int UP = 0;
    static final int RIGHT = 1;
    static final int DOWN = 2;
    static final int LEFT = 3;
    int direction;
    int antCol;
    int antRow;
    int maxX, maxY;
    int cols, rows;
    int antMoves;
    Cell cells[][];

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
        public Paint paintText;
        boolean started = false;
        private Handler handler;
        private static final int FRAME_RATE = 5; // 1000 = 1 per sec; 20 = 50 frames per sec

        MyCanvasView(Context context) {
            super(context);
            paintText = new Paint();
            paintText.setTextSize(25);
            paintText.setColor(Color.BLUE);

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

                // Create the bitmap to be used...
                maxX = myCanvasView.getWidth();
                maxY = myCanvasView.getHeight();
                int cellWidth = Math.min(maxX,maxY)/100;
                cols = maxX / cellWidth;
                rows = maxY / cellWidth;
                cells = new Cell[cols][rows];
                for (int i = 0; i < cols; i++) {
                    for (int j = 0; j < rows; j++) {
                        cells[i][j] = new Cell((i * cellWidth), (j * cellWidth), cellWidth);
                    }
                }

                // initialise ant position and direction
                antCol = cols / 2;
                antRow = rows / 2;
                direction = UP;

                started = true;

            } else {
                // Perform ongoing updates
                //Log.i(TAG,"Ant at ("+ antCol +","+ antRow +") direction " + direction);
                switch (cells[antCol][antRow].getColour()) {
                    case Color.WHITE:
                        //Log.i(TAG, "   on WHITE, so flipping colour and turning right...");
                        cells[antCol][antRow].flipColour();
                        direction++;
                        if (direction > LEFT) {
                            direction = UP;
                        }
                        break;
                    case Color.BLACK:
                        //Log.i(TAG, "   on BLACK, so flipping colour and turning left...");
                        cells[antCol][antRow].flipColour();
                        direction--;
                        if (direction < UP) {
                            direction = LEFT;
                        }
                        break;
                    default:
                        Log.i(TAG, "** FOUND UNEXPECTED COLOUR " + cells[antCol][antRow].getColour());
                        finish();
                }
                // Move forward..
                switch (direction) {
                    case UP:
                        antRow--;
                        if (antRow < 0) {
                            antRow = rows - 1;
                        }
                        break;
                    case RIGHT:
                        antCol++;
                        if (antCol > cols - 1) {
                            antCol = 0;
                        }
                        break;
                    case DOWN:
                        antRow++;
                        if (antRow > rows - 1) {
                            antRow = 0;
                        }
                        break;
                    case LEFT:
                        antCol--;
                        if (antCol < 0) {
                            antCol = cols - 1;
                        }
                        break;
                    default:
                        Log.i(TAG, "** FOUND UNEXPECTED DIRECTION " + direction);
                        finish();
                }

                // count the moves...
                antMoves++;

            }
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Perform the actual drawing - effectively called by the 'invalidateCanvas' command
            if (!started) {
                return;
            }

            // display the cells first...
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    cells[i][j].show(canvas);
                }
            }

            // display info on top...
            String msg = "Ant Moves:" + antMoves;
            canvas.drawText(msg, 20, 25, paintText);
            msg = "Ant Position: (" + antCol + "," + antRow + ")";
            canvas.drawText(msg, 20, 50, paintText);
            msg = "Ant Direction: " + direction;
            canvas.drawText(msg, 20, 75, paintText);

            // highlight the ant
            cells[antCol][antRow].highlight(canvas);
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

    class Cell {
        int x, y;   // screen coords of top-left corner
        int width;
        Paint paint;
        Paint highlight;

        Cell(int x, int y, int width) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.paint = new Paint();
            this.paint.setColor(Color.WHITE);
            this.paint.setStyle(Paint.Style.FILL);
            this.highlight = new Paint();
            this.highlight.setColor(Color.RED);
            this.highlight.setStyle(Paint.Style.FILL);
        }

        int getColour() {
            return this.paint.getColor();
        }

        void flipColour() {
            if (this.paint.getColor() == Color.WHITE) {
                this.paint.setColor(Color.BLACK);
            } else {
                this.paint.setColor(Color.WHITE);
            }
        }

        void show(Canvas canvas) {
            canvas.drawRect(this.x, this.y, this.x + this.width, this.y + this.width, this.paint);
        }

        void highlight(Canvas canvas) {
            canvas.drawCircle((this.x + this.width / 2), this.y + this.width / 2, this.width / 2, this.highlight);
        }
    }

}