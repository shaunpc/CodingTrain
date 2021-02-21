package com.spc.codingtrain;

// MAZE GENERATION
//
// Recursive Backtracker from:
// https://en.wikipedia.org/wiki/Maze_generation_algorithm
//

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
import java.util.Random;

public class MazeGenActivity extends AppCompatActivity {

    private static final String TAG = "MazeGen";
    private String action = "START";
    Button btnAction;
    SeekBar seekbar;
    MyCanvasView myCanvasView;
    Cell current;
    Cell next;
    int maxX, maxY;
    int cellWidth;      // grabs from the sbCellSize...
    boolean started = false;
    int cols, rows;
    Cell cells[][];
    List<Cell> stack = new ArrayList<>();



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
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        rLayout.addView(btnAction, btnParams);

        // Next, add the SeekBar on the left of that "ACTION" button
        seekbar  = new SeekBar(this);
        seekbar.setId(R.id.seekbar_id);
        // sbCellSize.setMin(10);
        seekbar.setMax(100);
        seekbar.setProgress(50);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch( SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch( SeekBar seekBar) {
                started = false;    // force a restart, which will get the progress value...
                action = "START";
                actionButton();
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
        myCanvasView.setBackgroundColor(Color.LTGRAY);
        RelativeLayout.LayoutParams cParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ABOVE, seekbar.getId());
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
        public Paint paintText, paintFinished;
        private Handler handler;
        private static final int FRAME_RATE = 50; // 1000 = 1 per sec; 20 = 50 frames per sec

        MyCanvasView(Context context) {
            super(context);
            paintText = new Paint();
            paintText.setTextSize(25);
            paintText.setColor(Color.BLACK);
            paintFinished = new Paint();
            paintFinished.setTextSize(75);
            paintFinished.setColor(Color.BLUE);
            paintFinished.setTypeface(Typeface.DEFAULT_BOLD);


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
                cellWidth = seekbar.getProgress() + 30;
                cols = maxX / cellWidth;
                rows = maxY / cellWidth;
                int xOffset = (maxX - (cols * cellWidth))/2;  // centers overall board on canvas
                int yOffset = (maxY - (rows * cellWidth))/2;  // centers overall board on canvas
                cells = new Cell[cols][rows];
                for (int c = 0; c < cols; c++) {
                    for (int r = 0; r < rows; r++) {
                        cells[c][r] = new Cell(c, r , cellWidth, xOffset, yOffset);
                    }
                }

                // Make the initial cell the current cell and mark it as visited
                current = cells[0][0];
                current.setVisited();

                cells[0][0].walls[0] = false;           // ENTRY = top of top-left cell
                cells[cols-1][rows-1].walls[2] = false; // EXIT = bottom of bottom-right cell

                started = true;

            } else {
                // Perform ongoing updates

                // If the current cell has any neighbours which have not been visited
                //      Choose randomly one of the unvisited neighbours
                next = getNeighbour(current);
                if (next != null) {
                    //      Push the current cell to the stack
                    stack.add(current);
                    //      Remove the wall between the current cell and the chosen cell
                    removeWalls(current, next);
                    //      Make the chosen cell the current cell and mark it as visited
                    current = next;
                    next.setVisited();

                } else if (stack.size() > 0) {
                    // Else if stack is not empty
                    //      Pop a cell from the stack
                    //      Make it the current cell
                    current = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                }
            }
        }

        void removeWalls (Cell first, Cell second) {
            if (first.col - second.col == 1) {  // First is to RIGHT of second
                cells[first.col][first.row].walls[3] = false; // remove left
                cells[second.col][second.row].walls[1] = false; // remove right
            }
            if (first.col - second.col == -1) {  // First is to LEFT of second
                cells[first.col][first.row].walls[1] = false;   // remove right
                cells[second.col][second.row].walls[3] = false;  // remove left
            }
            if (first.row - second.row == 1) {   // First is BELOW the second
                cells[first.col][first.row].walls[0] = false;   // remove top
                cells[second.col][second.row].walls[2] = false; // remove bottom
            }
            if (first.row - second.row == -1) {     // First is ABOVE the second
                cells[first.col][first.row].walls[2] = false; // remove bottom
                cells[second.col][second.row].walls[0] = false;  // remove top
            }
        }


        Cell getNeighbour (Cell core) {
            Random r = new Random();
            List<Cell> neighbours = new ArrayList<>();
            // Add above
            if (core.col > 0 && !cells[core.col-1][core.row].visited) {
                neighbours.add(cells[core.col-1][core.row]);
            }
            // Add below
            if (core.col < cols-1 && !cells[core.col+1][core.row].visited) {
                neighbours.add(cells[core.col+1][core.row]);
            }
            // Add left
            if (core.row > 0 && !cells[core.col][core.row-1].visited) {
                neighbours.add(cells[core.col][core.row-1]);
            }
            // Add right
            if (core.row < rows-1 && !cells[core.col][core.row+1].visited) {
                neighbours.add(cells[core.col][core.row+1]);
            }

            if (neighbours.size() > 0) {
                return neighbours.get(r.nextInt(neighbours.size()));
            } else {
                return null;
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
            Boolean allVisited = true;
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    cells[i][j].show(canvas);
                    allVisited = allVisited && cells[i][j].visited;
                }
            }

            // highlight current
            current.highlight(canvas);

            // display info on top...
            String msg = "Cell Width:" + cellWidth;
            canvas.drawText(msg, 20, 25, paintText);
            msg = "Stack:" + stack.size();
            canvas.drawText(msg, 20, 50, paintText);

            if (allVisited && stack.size()==0) {
                msg = "# FINISHED #";
                canvas.drawText(msg, maxX/4, maxY/2, paintFinished);
                stopFrameUpdates();
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

    class Cell {
        int col, row;
        int x, y;   // screen coords of top-left corner
        int width;
        // TOP, RIGHT, BOTTOM, LEFT
        boolean[] walls = {true, true, true, true};
        boolean visited = false;
        Paint paint = new Paint();
        Paint paintEdge = new Paint();
        Paint highlight = new Paint();

        Cell(int col, int row, int width, int xOffset, int yOffset) {
            this.col = col;
            this.row = row;
            this.x = xOffset + (col * width);
            this.y = yOffset + (row * width);
            this.width = width;
            this.paint.setColor(Color.WHITE);
            this.paint.setStyle(Paint.Style.FILL);
            this.paintEdge.setColor(Color.RED);
            this.paintEdge.setStyle(Paint.Style.STROKE);
            this.paintEdge.setStrokeWidth(3);
            this.highlight.setColor(Color.GREEN);
            this.highlight.setStyle(Paint.Style.FILL);
        }

        void setVisited () {this.visited = true;}

        void show(Canvas canvas) {

            if (visited) {
                canvas.drawRect(this.x, this.y, this.x + this.width, this.y + this.width, this.paint);
            }
            if (walls[0]) {  // TOP
                canvas.drawLine(this.x, this.y, this.x + this.width, this.y, this.paintEdge);
            }
            if (walls[1]) {  // RIGHT
                canvas.drawLine(this.x + this.width, this.y, this.x + this.width, this.y + this.width, this.paintEdge);
            }
            if (walls[2]) {  // BOTTOM
                canvas.drawLine(this.x, this.y + this.width, this.x + this.width, this.y + this.width, this.paintEdge);
            }
            if (walls[3]) {  // LEFT
                canvas.drawLine(this.x, this.y , this.x, this.y + this.width, this.paintEdge);
            }

        }

        void highlight(Canvas canvas) {
            canvas.drawCircle((this.x + this.width / 2), this.y + this.width / 2, this.width *0.4f, this.highlight);
        }
    }

}