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
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class FireworksActivity extends AppCompatActivity {

    private static final String TAG = "FIREWORKS";
    private String action = "START";
    Button btnAction;
    MyCanvasView myCanvasView;
    private static final int GRAVITY = 2;

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
        myCanvasView.setBackgroundColor(Color.BLUE);
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
        private static final String TAG = "FIREWORKS/CANVASVIEW";
        Paint paintCanvas, paintText;
        List<Firework> fireworks = new ArrayList<>();
        int myWidth = 0, myHeight = 0;
        Random random = new Random();
        private Handler handler;
        private static final int FRAME_RATE = 20 ; // 50 frames per second

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
                updateFireworks();
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
        private void updateFireworks () {
            if (fireworks == null) {
                // Add the first firework
                fireworks.add(new Firework(random.nextInt(myCanvasView.myWidth),
                        myCanvasView.myHeight - random.nextInt(50)));
            } else {
                // Update each firework in the current array
                for (Firework f : fireworks) {
                    f.update();
                }
                // Add another firework 10% of the time...
                if (random.nextInt(100) < 10) {
                    fireworks.add(new Firework(random.nextInt(myCanvasView.myWidth),
                            myCanvasView.myHeight - random.nextInt(50)));
                }
            }
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawPaint(paintCanvas);
            if (fireworks != null) {
                for (Firework f : fireworks) {
                    f.show(canvas);
                }
                String msg = "Fireworks:"+fireworks.size();
                canvas.drawText(msg,20,20,paintText);
                // remove any fireworks from the list that are now dead...
                for (Iterator<Firework> iterator = this.fireworks.iterator(); iterator.hasNext(); ) {
                    Firework f = iterator.next();
                    if(f.isDead()){
                        iterator.remove();
                    }
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
            Log.i(TAG, "OnMeasure: Canvas dimension W:" + width + " H:" + height);
            this.myWidth = width;
            this.myHeight = height;
            setMeasuredDimension(width, height);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (handler == null) {  // touched screen before "START" button
                    actionButton();     // then mimic the action button being pressed...
                }
                Firework f = new Firework(Math.round(event.getX()),Math.round(event.getY()));
                f.exploded = true;      // create new firework from touch point
                f.explode();            // explode it immediately
                this.fireworks.add(f);       // add it to the list for processing
            }
            return true;
        }
    }

    class Particle {
        int posX, posY;
        int velX, velY;
        int accX, accY;
        int radius;
        boolean firework;
        int lifespan = 255;   // for explosion particles this will fade to zero...
        Paint paint;
        MyColor colour;
        Random random = new Random();

        Particle(int x, int y, MyColor colour, boolean firework) {
            this.posX = x;
            this.posY = y;
            this.colour = colour;
            this.firework = firework;
            if (this.firework) {  // straight-ish up...
                this.velX = random.nextInt(10) - 5;
                this.velY = - 30 -  random.nextInt(50);  // TODO - explode in top third of screen?
            } else { // explosion particle, so any direction
                int size = 10 + random.nextInt(20);
                this.velX = random.nextInt(2*size) - size;
                this.velY = random.nextInt(2*size) - size;
            }
            this.accX = 0;
            this.accY = 0;
            this.radius = 4;
            this.paint = new Paint();
            this.paint.setStyle(Paint.Style.FILL);
            this.paint.setARGB(255, colour.r, colour.g, colour.b);
        }

        void applyForce(int x, int y) {
            this.accX += x;
            this.accY += y;
        }

        void update() {
            this.velX += this.accX;
            this.velY += this.accY;
            this.posX += this.velX;
            this.posY += this.velY;
            this.accX = 0;
            this.accY = 0;
            if (!this.firework) {  // slow down explosion particles after initial burst
                this.velX *= 0.85;
                this.velY *= 0.85;
                this.lifespan -= 5 + random.nextInt(5);
                if (this.lifespan < 0) { this.lifespan = 0;}
                this.paint.setAlpha(this.lifespan);
            }
        }

        boolean isDead () {
            // returns true if an explosion particle and has faded away
            return (!this.firework && this.lifespan == 0 );
        }

        void show(Canvas canvas) {
            canvas.drawCircle(this.posX, this.posY, this.radius, this.paint);
        }
    }

    class Firework {
        Particle firework;
        Boolean exploded;
        List<Particle> particles;
        MyColor colour;

        Firework (int x, int y) {
             this.colour = new MyColor();
             this.firework = new Particle(x, y, colour, true);
             this.exploded = false;
             this.particles = new ArrayList<>();

        }

        void update() {
            if (!this.exploded) {
                this.firework.applyForce(0, GRAVITY);
                this.firework.update();
                // Check if reach highest point... if so, create the explosion...
                if (this.firework.velY >=0) {
                    this.exploded = true;
                    this.explode();
                }
            } else {
                for (Particle p : this.particles) {
                    p.applyForce(0, GRAVITY);
                    p.update();
                }
                for (Iterator<Particle> iterator = this.particles.iterator(); iterator.hasNext(); ) {
                    Particle p = iterator.next();
                    if(p.isDead()){
                        iterator.remove();
                    }
                }
            }
        }

        void explode () {
            for (int i=0; i<100; i++) {
                this.particles.add(new Particle(this.firework.posX,this.firework.posY, this.colour.shift(),false));
            }
        }

        boolean isDead () {
            // returns true if the firework has exploded and has no particles still visible
            return (this.exploded && this.particles.size()==0);
        }

        void show(Canvas canvas) {
            if (!this.exploded) {
                this.firework.show(canvas);
            } else {  // show the explosion particles
                for (Particle p: this.particles) {
                    p.show(canvas);
                }
            }
        }
    }

    class MyColor {
        int r,g,b;
        Random random = new Random();

        MyColor (int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        MyColor () {
            int COLOUR_BASE = 50;
            // give a random colour
            this.r = random.nextInt(255 - COLOUR_BASE) + COLOUR_BASE;
            this.g = random.nextInt(255 - COLOUR_BASE) + COLOUR_BASE;
            this.b = random.nextInt(255 - COLOUR_BASE) + COLOUR_BASE;
        }

        MyColor shift () {
            int COLOUR_SHIFT = 20;
            int newR = this.r + random.nextInt(COLOUR_SHIFT * 2) - COLOUR_SHIFT;
            int newG = this.g + random.nextInt(COLOUR_SHIFT * 2) - COLOUR_SHIFT;
            int newB = this.b + random.nextInt(COLOUR_SHIFT * 2) - COLOUR_SHIFT;
            if (newR>=0 && newR<=255) {newR = this.r;}
            if (newG>=0 && newG<=255) {newG = this.g;}
            if (newB>=0 && newB<=255) {newB = this.b;}
            return new MyColor(newR, newG, newB);

        }

    }
}