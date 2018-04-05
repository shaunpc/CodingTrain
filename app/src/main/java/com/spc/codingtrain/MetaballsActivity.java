package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Bitmap;
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


public class MetaballsActivity extends AppCompatActivity {

    private static final String TAG = "METABALLS";
    private String action = "START";
    Button btnAction;
    MyCanvasView myCanvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // First, create the overall RelativeLayout view group
        Log.i(TAG, "Creating relative layout");
        RelativeLayout rLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(rLayout, rlParams);
        rLayout.setBackgroundColor(Color.DKGRAY);

        // Next, add the Button at the bottom of the screen
        Log.i(TAG, "Creating action button");
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
        Log.i(TAG, "Creating canvas view");
        myCanvasView = new MyCanvasView(this);
        myCanvasView.setId(R.id.canvas_view_id);
        // myCanvasView.setBackgroundColor(Color.BLUE);
        RelativeLayout.LayoutParams cParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ABOVE, btnAction.getId());
        rLayout.addView(myCanvasView, cParams);

        Log.i(TAG, "Setting content view");
        setContentView(rLayout);

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
        List<Blob> blobs = new ArrayList<>();
        int myWidth = 0, myHeight = 0;
        Bitmap tmpBitmap;
        Random random = new Random();
        int intArray[], counter;
        private Handler handler;
        private static final int FRAME_RATE = 20; // 50 frames per second

        MyCanvasView(Context context) {
            super(context);
            paintCanvas = new Paint();
            paintCanvas.setStyle(Paint.Style.FILL);
            paintCanvas.setColor(Color.BLACK);
            paintText = new Paint();
            paintText.setTextSize(25);
            paintText.setColor(Color.WHITE);
        }

        private Runnable updateFrame = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(updateFrame);
                // KEY FUNCTION CALL
                updateCanvas();
                myCanvasView.invalidate();
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

        //KEY FUNCTION - apply any changes to each construct
        private void updateCanvas() {
            if (blobs.size() == 0 ) {
                // Add the first two blobs
                blobs.add(new Blob(random.nextInt(myWidth),
                        random.nextInt(myHeight)));
            } else {
                // Update each blob - might not do this when fully working...
                for (Blob b : blobs) { b.update();}
            }

            if (intArray == null) {
                //Create the intArray to hold the bitmap pixel colour details
                intArray = new int[myWidth * myHeight];
            }

            // update the pixel representations of colour every pixel based on function...
            //Log.i(TAG,"Starting array update of all pixels... "+myWidth+"x"+myHeight+" ("+myHeight*myWidth+"!)");
            counter = 0;
            int distance;
            for (int y = 0 ; y < myHeight; y++) {  //myHeight
                for (int x = 0; x < myWidth; x++){  //myWidth
                    distance = 0;
                    for (Blob b : blobs) {
                        distance += b.pos.distance(x,y);
                    }
                    distance = distance/blobs.size();
                    if (distance < (myWidth/8)) {
                        intArray[counter] = Color.WHITE;
                    } else if (distance <(myWidth/8)*2) {
                        intArray[counter] = Color.CYAN;
                    } else if (distance <(myWidth/8)*3) {
                        intArray[counter] = Color.GREEN;
                    } else if (distance <(myWidth/8)*4) {
                        intArray[counter] = Color.YELLOW;
                    } else if (distance <(myWidth/8)*5) {
                        intArray[counter] = Color.MAGENTA;
                    } else if (distance <(myWidth/8)*6) {
                        intArray[counter] = Color.RED;
                    } else if (distance <(myWidth/8)*7) {
                        intArray[counter] = Color.BLUE;
                    } else {
                        intArray[counter] = Color.BLACK;
                    }
                    //Log.i(TAG, ""+x+","+y+" distance="+distance+ "/shifted="+shifted_distance+"/intArray="+intArray[counter] );
                    //Log.i(TAG, ""+x+","+y+" distance="+distance+ "/intArray="+intArray[counter] );
                    counter++;
                }
            }


            //Log.i(TAG,"Creating tmpBITMAP from intArray");
            tmpBitmap = Bitmap.createBitmap(intArray, myWidth,myHeight,Bitmap.Config.ARGB_8888);
            //Log.i(TAG,"tmpBitmap dimensions are "+tmpBitmap.getWidth()+"x"+tmpBitmap.getHeight());

            //Log.i(TAG,"setting bitmap as background on button!?");
            //btnAction.setBackground(new BitmapDrawable(getResources(),tmpBitmap));

        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            String msg;

            //clear the canvas - not required as subsuquently draws whole frame bitmap...
            //canvas.drawPaint(paintCanvas);

            if (tmpBitmap != null) {
                // we have a bitmap, so let's display it...
                // Log.i(TAG,"Starting draw of bitmap");
                canvas.drawBitmap(tmpBitmap, 0, 0, paintText);
            }

            if (blobs.size() > 0 ) {
                // paint all the blobs that exist
                for (Blob b : blobs) {
                    b.show(canvas);
                }
            }
            msg = "Blobs:" + blobs.size();
            canvas.drawText(msg, 20, 20, paintText);
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            Log.i(TAG,"Checking motion event for DOWN to draw new blob");
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (handler == null) {  // touched screen before "START" button
                    actionButton();     // then mimic the action button being pressed...
                }
                // add new blob at pressed location to the list for processing
                this.blobs.add(new Blob(Math.round(event.getX()),Math.round(event.getY())));
            }
            return true;
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
            if (widthMode == MeasureSpec.EXACTLY) { width = widthSize;
            } else if (widthMode == MeasureSpec.AT_MOST) { width = Math.min(desiredWidth, widthSize);
            } else { width = desiredWidth;}

            // Measure Height
            if (heightMode == MeasureSpec.EXACTLY) { height = heightSize;
            } else if (heightMode == MeasureSpec.AT_MOST) { height = Math.min(desiredHeight, heightSize);
            } else { height = desiredHeight;}

            // MUST CALL THIS
            this.myWidth = width;
            this.myHeight = height;
            setMeasuredDimension(width, height);
        }
    }

    class Blob {
        Point2D pos;
        Point2D vel;
        int radius;
        Paint paint;
        Random random = new Random();

        Blob (int x, int y) {
            int VELOCITY_BASE = 20;
            this.pos = new Point2D(x,y);
            this.vel = new Point2D(random.nextInt(VELOCITY_BASE * 2) - VELOCITY_BASE,
                            random.nextInt(VELOCITY_BASE * 2) - VELOCITY_BASE);
            this.radius = random.nextInt(30) + 20;

            this.paint = new Paint();
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setColor(Color.BLACK);
        }

        void update(){
            this.pos.add(this.vel);
            // bounce off the sides of the window
            if (!this.pos.inRangeX(0,myCanvasView.myWidth)) {this.vel.mult(-1,1);}
            if (!this.pos.inRangeY(0,myCanvasView.myHeight)) {this.vel.mult(1,-1);}
        }

        void show(Canvas canvas) {
            canvas.drawCircle(this.pos.getX(), this.pos.getY(), this.radius, this.paint);
        }
    }
}