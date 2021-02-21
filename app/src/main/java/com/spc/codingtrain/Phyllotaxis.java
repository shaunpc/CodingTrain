package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

public class Phyllotaxis extends AppCompatActivity {

    private static final String TAG = "PHYLLO";
    String action = "START";
    Button btnAction;
    SeekBar sbConstant, sbMagicAngle;
    MyCanvasView myCanvasView;
    int maxX, maxY;
    boolean started = false;

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

        // Next, add the Constant SeekBar on the left of that "ACTION" button
        sbConstant = new SeekBar(this);
        sbConstant.setId(R.id.seekbar_constant_id);
        sbConstant.setMax(50);
        sbConstant.setProgress(20);
        sbConstant.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                started = false;    // force a restart, which will get the progress value...
                action = "RESET";
                actionButton(); // in case we've finished and stopped updates
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });
        btnParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.LEFT_OF, btnAction.getId());
        btnParams.addRule(RelativeLayout.ALIGN_TOP, btnAction.getId());
        rLayout.addView(sbConstant, btnParams);

        // Next, add the Magic Angle SeekBar on the left of that "ACTION" button
        sbMagicAngle = new SeekBar(this);
        sbMagicAngle.setId(R.id.seekbar_magicangle_id);
        sbMagicAngle.setMax(3598);
        sbMagicAngle.setProgress(1374);
        sbMagicAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                started = false;    // force a restart, which will get the progress value...
                action = "RESET";
                actionButton(); // in case we've finished and stopped updates
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });
        btnParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.RIGHT_OF, btnAction.getId());
        btnParams.addRule(RelativeLayout.ALIGN_TOP, btnAction.getId());
        rLayout.addView(sbMagicAngle, btnParams);

        // Then, fill the rest with the MyCanvasView class
        myCanvasView = new MyCanvasView(this);
        myCanvasView.setId(R.id.canvas_view_id);
        myCanvasView.setBackgroundColor(Color.BLUE);
        RelativeLayout.LayoutParams cParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ABOVE, sbConstant.getId());
        cParams.addRule(RelativeLayout.ABOVE, sbMagicAngle.getId());
        cParams.addRule(RelativeLayout.ABOVE, btnAction.getId());
        rLayout.addView(myCanvasView, cParams);

        setContentView(rLayout);
        Log.i(TAG, "OnCreate completed");
    }

    void actionButton() {
        if (action.equals("START") || action.equals("RESET")) {
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
        private Handler handler;
        private static final int FRAME_RATE = 5; // 1000 = 1 per sec; 20 = 50 frames per sec
        List<Leaf> leaves = new ArrayList<>();
        int pConstant;
        float pMagicAngle;
        String msg;

        MyCanvasView(Context context) {
            super(context);
            paintText = new Paint();
            paintText.setTextSize(25);
            paintText.setColor(Color.WHITE);
        }

        private final Runnable updateFrame = new Runnable() {
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

                maxX = myCanvasView.getWidth();
                maxY = myCanvasView.getHeight();
                pConstant = sbConstant.getProgress();
                pMagicAngle = (sbMagicAngle.getProgress()+1) / 10f;
                leaves = new ArrayList<>();  // clear/create array list
                leaves.add(new Leaf(0, pConstant, pMagicAngle));

                started = true;

            } else {
                // Perform ongoing updates - just add another leaf
                leaves.add(new Leaf(leaves.size(), pConstant, pMagicAngle));

            }
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Perform the actual drawing - effectively called by the 'invalidateCanvas' command
            int offScreenCount;  // so we know when to finish...

            if (!started) {
                msg = "Constant: " + sbConstant.getProgress();
                canvas.drawText(msg, 20, 25, paintText);
                msg = "Magic angle:" + (sbMagicAngle.getProgress()+1) / 10f;
                canvas.drawText(msg, 20, 50, paintText);
                return;
            }

            // display info on top...
            msg = "Constant: " + pConstant;
            canvas.drawText(msg, 20, 25, paintText);
            msg = "Magic angle:" + pMagicAngle;
            canvas.drawText(msg, 20, 50, paintText);
            msg = "Leaf Count:" + leaves.size();
            canvas.drawText(msg, 20, 75, paintText);

            // move the (0,0) point to the center of the canvas..
            canvas.translate((float) maxX / 2, (float) maxY / 2);

            // display the leaves
            if (leaves != null) {
                offScreenCount = 0;
                for (Leaf l : leaves) {
                    l.show(canvas);
                    if (l.offScreen(maxX, maxY)) {
                        offScreenCount++;
                    } else {
                        offScreenCount = 0;
                    }
                }
                if (offScreenCount > 360) {
                    msg = "FINISHED";
                    paintText.setTextSize(paintText.getTextSize() * 3);
                    paintText.setColor(Color.YELLOW);
                    paintText.setTypeface(Typeface.DEFAULT_BOLD);
                    canvas.drawText(msg, -maxX * 0.8f, maxY * 0.4f, paintText);
                    stopFrameUpdates();
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

    static class Leaf {
        int n;
        float x, y;   // screen coords of center
        float distance;
        float angle;
        float radius;
        Paint paint;

        Leaf(int n, int c, float angle) {
            this.n = n;
            this.distance = (float) (c * Math.sqrt(n));
            this.angle = (float) Math.toRadians(n * angle);
            this.x = (float) (this.distance * Math.cos(this.angle));
            this.y = (float) (this.distance * Math.sin(this.angle));
            this.radius = 4;
            this.paint = new Paint();
            //this.paint.setARGB(255, (int) ((n*angle) % 128) + 128, (int) ((n*angle) % 256), 0);
            this.paint.setColor(Color.HSVToColor(new float[]{(n*angle)%256,255,255}));
            //this.paint.setColor(Color.WHITE);
            this.paint.setStyle(Paint.Style.FILL);
        }

        void show(Canvas canvas) {
            canvas.drawCircle(this.x, this.y, this.radius, this.paint);
        }

        boolean offScreen(int limitX, int limitY) {
            return (this.x < -limitX / 2.0 || this.x > limitX / 2.0 || this.y < -limitY / 2.0 || this.y > limitY / 2.0);
        }
    }

}