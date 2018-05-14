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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.spc.library.MyColor;
import com.spc.library.Point2D;

public class SmartRocketsActivity extends AppCompatActivity {

    private static final String TAG = "ROCKETS";
    private String action = "START";
    Button btnAction;
    MyCanvasView myCanvasView;

    // Key population variables
    public Population population;
    public int generation = 1;
    public int lifespan_count = 0;
    public static final int LIFESPAN_MAX = 200;
    public RectF target;
    public Point2D targetCenter;
    public static final int TARGET_SIZE = 25;
    List<RectF> barriers = new ArrayList<>();
    public static final int BARRIER_SIZE = 25;
    public static final int POPULATION_SIZE = 20;

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
        private static final String TAG = "CANVASVIEW";
        String msg;
        Paint paintCanvas, paintText, paintTargetInner, paintTargetOuter, paintBarrier;
        int myWidth = 0, myHeight = 0;
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
            paintTargetOuter = new Paint();
            paintTargetOuter.setStyle(Paint.Style.STROKE);
            paintTargetOuter.setStrokeWidth(3);
            paintTargetOuter.setColor(Color.RED);
            paintTargetInner = new Paint();
            paintTargetInner.setStyle(Paint.Style.FILL);
            paintTargetInner.setColor(Color.WHITE);
            paintBarrier = new Paint();
            paintBarrier.setStyle(Paint.Style.FILL);
            paintBarrier.setColor(Color.RED);

        }

        private Runnable updateFrame = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(updateFrame);
                // KEY FUNCTION CALL
                updateSmartRockets();
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
        private void updateSmartRockets() {
            if (population == null) {
                population = new Population(POPULATION_SIZE); // Create the initial population
                lifespan_count = 0;  // reset lifespan counter for this population
                targetCenter = new Point2D(myWidth / 2, myHeight / 20);
                target = new RectF( // set the target area rectangle
                        myWidth / 2 - TARGET_SIZE * 2,
                        myHeight / 20 - TARGET_SIZE,
                        myWidth / 2 + TARGET_SIZE * 2,
                        myHeight / 20 + TARGET_SIZE);
            } else {
                if (lifespan_count == LIFESPAN_MAX) {
                    population.evaluate();  // Create the mating pool (based on those closest to target)
                    population.selection(); // create the new rockets with 'fit' DNA
                    lifespan_count = 0;  // reset lifespan counter for this population
                    generation++;
                } else {
                    population.update();
                    lifespan_count++;
                }
            }
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawPaint(paintCanvas);

            // show our population if we have one
            if (population != null) {
                population.show(canvas);

                // draw the target
                canvas.drawOval(target, paintTargetInner);
                canvas.drawOval(target, paintTargetOuter);
            }

            // show any barriers
            for (RectF b : barriers) {
                canvas.drawRoundRect(b, 5, 5, paintBarrier);
            }

            // display the info
            msg = "Generation:" + generation + " / Age:" + lifespan_count;
            canvas.drawText(msg, 20, 25, paintText);
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
                barriers.add(new RectF( // set the barrier area rectangle
                        event.getX() - BARRIER_SIZE * 2,
                        event.getY() - BARRIER_SIZE,
                        event.getX() + BARRIER_SIZE * 2,
                        event.getY() + BARRIER_SIZE));
            }
            return true;
        }
    }

    class Population {
        List<Rocket> rockets = new ArrayList<>();
        List<Rocket> matingPool = new ArrayList<>();
        double maxFitness = 0;
        double minFitness = 0;
        int mutatedRocketsCount;
        Paint paintText, paintSummary;
        String msg;
        int perCent = 0;

        Population(int size) {
            paintText = new Paint();        // for the detailed stats text
            paintText.setTextSize(25);
            paintText.setColor(Color.WHITE);
            paintSummary = new Paint();     // for the big summary % text
            paintSummary.setTextSize(100);
            paintSummary.setColor(Color.LTGRAY);
            paintSummary.setAlpha(128);
            // create the population and position roughly halfway along bottom of screen.
            Random r = new Random();
            for (int i = 0; i < size; i++) {
                int offset = r.nextInt(30) - 15;
                this.rockets.add(new Rocket((myCanvasView.myWidth / 2) + offset, myCanvasView.myHeight));
            }

        }

        public void show(Canvas canvas) {
            int hitCount = 0;
            int oobCount = 0;
            int barCount = 0;
            for (Rocket r : this.rockets) {
                r.show(canvas);
                if (r.hitTarget) {
                    hitCount++;
                }
                if (r.outOfBounds) {
                    oobCount++;
                }
                if (r.hitBarrier) {
                    barCount++;
                }
            }
            msg = "Rockets:" + this.rockets.size();
            if (barriers.size() > 0) {
                msg = msg + "/Barriers:" + barriers.size();
            }
            canvas.drawText(msg, 20, 50, paintText);

            int actCount = this.rockets.size() - hitCount - oobCount - barCount;
            msg = "Act=" + actCount + "/Hit=" + hitCount + "/Out=" + oobCount;
            if (barriers.size() > 0) {
                msg = msg + "/Bar=" + barCount;
            }
            canvas.drawText(msg, 20, 75, paintText);

            msg = "Fitness: Max=" + ((double) Math.round(this.maxFitness * 1000000) / 1000000);
            msg = msg+ "/Min="+ ((double) Math.round(this.minFitness * 1000000 / 1000000));
            canvas.drawText(msg, 20, 100, paintText);

            msg = "Mating Pool Size="+this.matingPool.size();
            canvas.drawText(msg, 20, 125, paintText);

            msg = "Generation Mutation Rate="+(this.mutatedRocketsCount * 100)/this.rockets.size()+"%";
            canvas.drawText(msg, 20, 150, paintText);

            if (actCount == 0) {  // only change it at end of generation
                perCent = (hitCount * 100) / this.rockets.size();
            }
            msg = "" + perCent + "%";
            canvas.drawText(msg, 20, myCanvasView.myHeight - 20, paintSummary);
        }

        public void evaluate() {
            this.maxFitness = 0;
            this.minFitness = 0;
            for (Rocket r : this.rockets) {
                r.calcFitness();
                if (r.fitness > this.maxFitness) {
                    this.maxFitness = r.fitness;
                }
                if (r.fitness < this.minFitness) {
                    this.minFitness = r.fitness;
                }
            }
            // Normalise the fitness numbers, then reward for hitting target, and punish for missing
            for (Rocket r : this.rockets) {
                r.fitness /= this.maxFitness;
                if (r.hitTarget) {r.fitness *=1.5;}
                if (r.hitBarrier) {r.fitness *=0.8;}
                if (r.outOfBounds) {r.fitness *=0.9;}
                //Log.i(TAG,""+r.toString()+"/Fitness="+r.fitness+"/tar="+r.hitTarget+"/bar="+r.hitBarrier+"/oob="+r.outOfBounds);
            }
            // Now create the mating pool - adding more of the fittest as potential parent
            this.matingPool = new ArrayList<>();
            for (Rocket r : this.rockets) {
                for (int i = 0; i < r.fitness * 100; i++) {
                    this.matingPool.add(r);
                }
                //Log.i(TAG, ""+r.toString()+"/Fit="+r.fitness+"/Pool now "+this.matingPool.size());
            }
        }

        public void selection() {
            Random random = new Random();
            List<Rocket> newRockets = new ArrayList<>();
            this.mutatedRocketsCount = 0 ;
            for (Rocket r : this.rockets) {
                DNA parentA = this.matingPool.get(random.nextInt(this.matingPool.size())).dna;
                DNA parentB = this.matingPool.get(random.nextInt(this.matingPool.size())).dna;
                DNA child = parentA.crossOver(parentB);
                int mutate_rate = 5; // Mutate the resultant child DNA ~5% of time
                if (r.hitBarrier) {mutate_rate = 10;}  // unless it had hit a barrier, then force 10% mutation
                if (random.nextInt(100) < mutate_rate) {
                    child.mutation();
                    this.mutatedRocketsCount++;
                }
                newRockets.add(new Rocket(myCanvasView.myWidth / 2, myCanvasView.myHeight, child));
            }
            // Log.i(TAG, "Gen:"+generation+" /Mutated "+count+" children out of "+this.rockets.size());
            this.rockets = newRockets;
        }

        public void update() {
            for (Rocket r : this.rockets) {
                r.applyForce(r.dna.genes.get(lifespan_count));
                r.update();                // Update each rocket in the current population
            }
        }
    }

    // The DNA class basically contains the genes for a rocket's path - stored as a list of random
    // 2D points representing the velocity to be applied at each age step of the lifespan.
    class DNA {
        List<Point2D> genes = new ArrayList<>();
        Random random = new Random();
        final int DNA_SIZE = 5;     // Sets magnitude of velocity (9 looked too 'jumpy')

        // Constructor to create new gene set - random genes added
        DNA(int lifespan) {
            for (int i = 0; i < lifespan; i++) {
                genes.add(new Point2D(random.nextInt(DNA_SIZE) - DNA_SIZE/2,
                        random.nextInt(DNA_SIZE) - DNA_SIZE/2));
            }
        }

        // Constructor with ready-made genes
        DNA(List<Point2D> genes) {
            this.genes = genes;
        }

        // Cross-over this DNA gene string, by randomly picking from either parent.
        public DNA crossOver(DNA otherParent) {
            List<Point2D> newGenes = new ArrayList<>();
            // int count = 0;
            for (int i = 0; i < this.genes.size(); i++) {
                if (random.nextInt(2) == 0) {  // returns 0 or 1
                    newGenes.add(this.genes.get(i));  // if 0
                } else {
                    newGenes.add(otherParent.genes.get(i));  // if 1
                //    count++;
                }
            }
            //Log.i(TAG, "Crossed over "+count+" genes from otherParent - out of "+this.genes.size());
            return new DNA(newGenes);
        }

        // Mutate this DNA gene string, by creating new random genes ~5% of the time
        public void mutation () {
            // int count = 0;
            for (int i = 0; i < this.genes.size(); i++) {
                if (random.nextInt(100) < 5) { // returns 0-99, only mutate < 5
                    this.genes.set(i, new Point2D(random.nextInt(DNA_SIZE) - DNA_SIZE/2,
                            random.nextInt(DNA_SIZE) - DNA_SIZE/2));
                //    count++;
                }
            }
            //Log.i(TAG, "Mutated "+count+" genes out of "+this.genes.size());
        }
    }

    class Rocket {
        Point2D pos;
        Point2D vel;
        Point2D acc;
        DNA dna;
        int height = 25;
        int width = 8;
        double fitness = 0;
        boolean outOfBounds;
        boolean hitBarrier;
        boolean hitTarget;
        Paint paint = new Paint();
        MyColor colour = new MyColor();

        Rocket(int x, int y) {
            this.pos = new Point2D(x, y);
            this.vel = new Point2D(0, 0);
            this.acc = new Point2D(0, 0);
            this.paint.setStyle(Paint.Style.FILL);
            this.paint.setARGB(255, colour.r, colour.g, colour.b);
            this.dna = new DNA(LIFESPAN_MAX);
            this.outOfBounds = false;
            this.hitBarrier = false;
            this.hitTarget = false;
        }

        Rocket(int x, int y, DNA dna) {
            this.pos = new Point2D(x, y);
            this.vel = new Point2D(0, 0);
            this.acc = new Point2D(0, 0);
            this.paint.setStyle(Paint.Style.FILL);
            this.paint.setARGB(255, colour.r, colour.g, colour.b);
            this.dna = dna;
            this.outOfBounds = false;
            this.hitBarrier = false;
            this.hitTarget = false;
        }

        void applyForce(Point2D f) {
            this.acc.add(f);
        }

        void update() {
            // only update if still valid to move it...
            if (!(this.outOfBounds || this.hitTarget || this.hitBarrier)) {
                this.vel.add(this.acc);
                this.pos.add(this.vel);
                this.acc.clear();
            }

            // Check if out-of-bounds
            this.outOfBounds = !(this.pos.inRangeX(0, myCanvasView.myWidth) &&
                    this.pos.inRangeY(0, myCanvasView.myHeight));

            // Check if hit target
            this.hitTarget = this.pos.inRangeX(target.left, target.right) &&
                    this.pos.inRangeY(target.top, target.bottom);

            // Check if hit any barriers
            for (RectF b : barriers) {
                this.hitBarrier = this.hitBarrier || this.pos.inRangeX(b.left, b.right) &&
                        this.pos.inRangeY(b.top, b.bottom);
            }
        }

        void calcFitness() {
            // determines fitness by distance from target center point
            // inverted as those ending closest have the highest fitness level
            this.fitness = 1 / this.pos.distance(targetCenter);
        }

        void show(Canvas canvas) {
            /*canvas.save();
            Log.i(TAG,"Rotate: POS: "+this.pos.getX()+","+this.pos.getY()+" angle="+this.pos.angle() );
            Log.i(TAG,"     Rotate: VEL: "+this.vel.getX()+","+this.vel.getY()+" angle="+this.vel.angle() );
            canvas.rotate(this.vel.angle()); //take the angle of the velocity!
            */
            canvas.drawRect((float) this.pos.getX() - width, (float) this.pos.getY() - height,
                    (float) this.pos.getX() + width, (float) this.pos.getY() + height, this.paint);
            /*canvas.restore();*/
        }
    }
}