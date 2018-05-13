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
import android.widget.SeekBar;

import com.spc.library.PerlinNoiseGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PerlinNoise extends AppCompatActivity {

    private static final String TAG = "PERLIN";
    String action = "START";
    Button btnAction;
    SeekBar seekbar;
    MyCanvasView myCanvasView;
    int maxX, maxY;
    int cellWidth;      // grabs from the seekbar...
    boolean started = false;
    int cols, rows;
    Cell cells[][];
    PerlinNoiseGenerator png;
    float noiseIncrement = 0.1f;
    float timeIncrement = 0.01f;
    float xoff, yoff, zoff;
    List<Particle> particles = new ArrayList<>();
    Random r = new Random();


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

        // Next, add the SeekBar on the left of that "ACTION" button
        seekbar  = new SeekBar(this);
        seekbar.setId(R.id.seekbar_id);
        seekbar.setMax(100);
        seekbar.setProgress(50);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch( SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch( SeekBar seekBar) {
                started = false;    // force a restart, which will get the progress value...
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        });
        btnParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.LEFT_OF, btnAction.getId());
        btnParams.addRule(RelativeLayout.ALIGN_TOP,btnAction.getId());
        rLayout.addView(seekbar, btnParams);


        // Then, fill the rest with the MyCanvasView class
        myCanvasView = new MyCanvasView(this);
        myCanvasView.setId(R.id.canvas_view_id);
        myCanvasView.setBackgroundColor(Color.BLUE);
        RelativeLayout.LayoutParams cParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ABOVE, btnAction.getId());
        rLayout.addView(myCanvasView, cParams);

        setContentView(rLayout);
        Log.i(TAG, "OnCreate completed");

        png = new PerlinNoiseGenerator();   //with default seed

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
        private static final int FRAME_RATE = 50; // 1000 = 1 per sec; 20 = 50 frames per sec
        String msg;

        MyCanvasView(Context context) {
            super(context);
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

                // Create the basics
                maxX = myCanvasView.getWidth();
                maxY = myCanvasView.getHeight();
                cellWidth = seekbar.getProgress() + 100;
                cols = (int) Math.ceil(maxX / cellWidth ) ;
                rows = (int) Math.ceil(maxY / cellWidth ) ;
                cells = new Cell[cols][rows];
                Log.i(TAG,"Max=["+maxX+","+maxY+"] cellWidth="+cellWidth+" Cols:"+cols+" Rows:"+rows);

/*cellWidth = 310;
cols = 3;
rows = 4;*/

                // Create the flow field
                xoff = 0;
                for (int i = 0; i < cols; i++) {
                    yoff = 0;
                    for (int j = 0; j < rows; j++) {
                        int x = (i * cellWidth);    // X coord
                        int y = (j * cellWidth);    // Y coord
                        cells[i][j] = new Cell(x, y, cellWidth);
                        // noise3 actually returns between -0.866 and +0.866,  [noise2 range is +-0.707]
                        //              therefore need to mult appropriately
                        cells[i][j].setVector(png.noise3(xoff,yoff,zoff) * 180f/0.866f, 5);
                        cells[i][j].setColour((int) (png.noise3(xoff,yoff,zoff) * 128f/0.866f) + 128f/0.866f);
                        Log.i(TAG, "Cell ["+i+","+j+"] with "+cells[i][j].toString());
                        yoff += noiseIncrement;
                    }
                    xoff += noiseIncrement;
                }

                // Drop a few particle and give it initial nudge...
                particles = new ArrayList<>();
                for (int i = 0 ; i < 50; i++) {
                    Particle p = new Particle (r.nextInt(cols*cellWidth), r.nextInt(rows*cellWidth));
                    //p.applyForce(new Point2D((r.nextInt(50)-25)/cellWidth, (r.nextInt(50)-25)/cellWidth));
                    particles.add(p);
                }

                started = true;

            } else {
                // Perform ongoing updates - Update the flow field
/*
                zoff += timeIncrement;
                xoff = 0;
                for (int i = 0; i < cols; i++) {
                    yoff = 0;
                    for (int j = 0; j < rows; j++) {
                        cells[i][j].setVector(png.noise3(xoff,yoff,zoff) * 180f/0.866f, 5);
                        cells[i][j].setColour((int) (png.noise3(xoff,yoff,zoff) * 128f/0.866f) + 128f/0.866f);
                        //Log.i(TAG, "Cell ["+i+","+j+"] with "+cells[i][j].toString());
                        yoff += noiseIncrement;
                    }
                    xoff += noiseIncrement;
                }
*/

                // update all the particles
                for (Particle p : particles) {
                    int i = (int) Math.floor(p.pos.getX()/cellWidth);
                    int j = (int) Math.floor(p.pos.getY()/cellWidth);
                    if (i >= 0 && i < cols && j >= 0 && j < rows) {
                        p.setLocation(i,j);
                        p.applyForce(cells[i][j].force);
                    } else {
                        Log.e(TAG," Particle not mapped to cell : "+i+","+j+ "  :"+p.pos.toString());
                    }
                    p.update();
                    p.wrap(cols*cellWidth, rows*cellWidth); // wrap particles around screen if needed
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

            // display the cells first...
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    cells[i][j].show(canvas);
                    cells[i][j].showFlow(canvas);
                }
            }

            // show particles
            for (Particle p : particles) {
                p.show(canvas);
/*
                msg = "In cell ["+p.loc.getX()+","+p.loc.getY()+"] with force [" +
                        cells[(int)p.loc.getX()][(int)p.loc.getY()].force.getX()+","+
                        cells[(int)p.loc.getX()][(int)p.loc.getY()].force.getY()+"]";
                canvas.drawText(msg, 20, 100, paintText);
*/
            }

            // display info on top...
            msg = "Cell width="+cellWidth;
            canvas.drawText(msg, 20, 25, paintText);
            msg = "Cols="+cols+"/Rows="+rows;
            canvas.drawText(msg, 20, 50, paintText);
            msg = "Particles="+particles.size();
            canvas.drawText(msg, 20, 75, paintText);

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
        float angle;
        float colour;
        Point2D force;
        Paint paint;

        Cell(int x, int y, int width) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.paint = new Paint();
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setColor(Color.WHITE);   // default it
            this.paint.setTextSize(25);
            this.force = new Point2D();
        }

        void show(Canvas canvas) {
            canvas.drawRect(this.x, this.y, this.x + this.width, this.y + this.width, this.paint);
        }

        void showFlow(Canvas canvas) {
            canvas.save();
            canvas.translate(this.x + this.width/2,this.y+ this.width/2);
            canvas.rotate(this.angle);
            canvas.drawLine(-this.width/3,0,this.width/3, 0, this.paint);
            canvas.drawLine(this.width/3,0,(this.width/3)-5, -5, this.paint);
            canvas.drawLine(this.width/3,0,(this.width/3)-5, +5, this.paint);
            canvas.restore();
/*            canvas.drawText(String.valueOf(this.force.getX()),this.x,this.y+25,this.paint);
            canvas.drawText(String.valueOf(this.force.getY()),this.x,this.y+50,this.paint);
            canvas.drawText(String.valueOf(this.angle), this.x,this.y+75,this.paint);*/

        }

        void setVector (float angle, int length) {
            this.angle = angle;  // In degrees
            this.force.set(length * Math.cos(Math.toRadians(this.angle)),
                           length * Math.sin(Math.toRadians(this.angle)));
        }

        void setColour (float colour) {
            this.colour = colour;
            this.paint.setColor(Color.HSVToColor(new float[]{colour, 255,255}));
        }

        public String toString () {
            return ("Cell ["+this.x+","+this.y+"] with angle="+this.angle+", colour="+this.colour+", force="+this.force.toString());
        }
    }

    class Particle {
        Point2D pos;
        Point2D vel;
        Point2D acc;
        Paint paint;
        Point2D loc;

        Particle (int x, int y) {
            this.pos = new Point2D(x,y);
            this.vel = new Point2D();
            this.acc = new Point2D();
            this.loc = new Point2D();
            this.paint = new Paint();
            this.paint.setStyle(Paint.Style.FILL_AND_STROKE);
            this.paint.setColor(Color.WHITE);   // default it
        }

        void update () {
            this.vel.add(this.acc);
            this.pos.add(this.vel);
            this.acc.clear();
            this.vel.mult(0.8,0.8); // slow things down each time...
        }

        void wrap (double mx, double my) {
            //wrap around screen
            // Log.i(TAG, "Checking wrap (max="+mx+","+my+") : " + this.pos.toString());
            if (this.pos.getX() < 0) { this.pos.set (mx-1,this.pos.getY()); }
            if (this.pos.getY() < 0) { this.pos.set (this.pos.getX(), my-1); }
            if (this.pos.getX() > mx) { this.pos.set (0,this.pos.getY()); }
            if (this.pos.getY() > my) { this.pos.set (this.pos.getX(),0); }
            // Log.i(TAG, "     done, now: " + this.pos.toString());

        }

        void applyForce (Point2D force) {
            this.acc.add(force);
        }

        void setLocation (int lx, int ly) {
            this.loc.set(lx, ly);
        }

        void show (Canvas canvas) {
            canvas.drawCircle((float)this.pos.getX(),(float)this.pos.getY(),5,paint);
        }
    }

}