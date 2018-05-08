package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.sqrt;


public class MitosisActivity extends AppCompatActivity {

    private static final String TAG = "MITOSIS";
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
        Paint paintCanvas, paintText;
        boolean started = false;
        private Handler handler;
        private static final int FRAME_RATE = 20; // 50 frames per second
        List<Cell> cells = new ArrayList<>();
        String msg;

        MyCanvasView(Context context) {
            super(context);
            paintCanvas = new Paint();
            paintCanvas.setStyle(Paint.Style.STROKE);
            paintCanvas.setColor(Color.BLACK);
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

            Random r = new Random();
            int maxX = getWidth();
            int maxY = getHeight();

            if (!started) {
                // create two cells to get things started
                cells.add(new Cell (r.nextInt(maxX), r.nextInt(maxY), r.nextInt(40)+40));
                cells.add(new Cell (r.nextInt(maxX), r.nextInt(maxY), r.nextInt(40)+40));
                started = true;
            } else {
                // perform updates on all cells
                for (Cell c : cells) {
                    c.update();
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Draw the two lines and the pendulums themselves...
            if (started) {
                // now clear background
                canvas.drawPaint(paintCanvas);

                // show all cells
                for (Cell c : cells) {
                    c.show(canvas);
                }

                // display the info
                msg = "Cells:" + cells.size();
                canvas.drawText(msg, 20, 25, paintText);
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

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (handler == null) {  // touched screen before "START" button
                    actionButton();     // then mimic the action button being pressed...
                }
                // review all cells to see if touch event occurred inside a cell
                for (Cell c : cells) {
                    if (c.inside(event.getX(), event.getY())) {
                        cells.add(c.mitosis());
                        break;
                    };
                }
            } // DOWN

            return true;
        }
    }

    class Cell {
        float x;
        float y;
        float r;
        float pulsate;
        MyColor colour;
        Paint paintCell = new Paint();

        Cell (float x, float y, float r) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.paintCell.setStrokeWidth(5);
            this.colour = new MyColor();  // get a random colour
            this.paintCell.setStyle(Paint.Style.FILL_AND_STROKE);
            this.paintCell.setARGB(155, colour.r, colour.g, colour.b);
        }

        void show(Canvas canvas) {
            canvas.drawCircle(this.x, this.y, this.r+this.pulsate, this.paintCell);
        }

        void update () {
            Random r = new Random();
            this.x = this.x + (r.nextInt(11)-5);
            this.y = this.y + (r.nextInt(11)-5);
            this.pulsate = this.r + (r.nextInt(5)-2);
            if (this.r < 100) {
                this.r = this.r *1.001f;
            }
        }

        Cell mitosis () {
            Log.i (TAG, "Performing mitosis at " + this.x + ","+ this.y);

            float newX = this.x - this.r * 0.5f; // set new cell slightly to left of current
            this.x = this.x + this.r * 0.5f;   // move this one slightly right
            this.r = (float) sqrt(2)/2 * this.r;   //set new radius that preserves overall area
            Cell newCell = new Cell(newX,this.y,this.r);
            newCell.setColour(this.colour);   //preserve the colour...
            return newCell;
        }

        void setColour (MyColor colour) {
            this.paintCell.setARGB(155, colour.r, colour.g, colour.b);
        }

        boolean inside (float x, float y) {  // return true if coords passed are inside the cell
            // otherwise find the distance between the (x,y)'s and check against the two radius
            float distance =  (float) sqrt(Math.pow(this.x - x, 2)+Math.pow(this.y - y, 2));
            return (distance < this.r);
        }

    }
}
