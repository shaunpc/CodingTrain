package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
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
    int cellWidth;
    boolean started = false;
    int cols, rows;
    Cell[][] cells;
    List<Cell> stack = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        RelativeLayout rLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(rLayout, rlParams);
        rLayout.setBackgroundColor(Color.DKGRAY);

        btnAction = new Button(this);
        btnAction.setId(R.id.action_button_id);
        btnAction.setText(R.string.action_button_start);
        btnAction.setOnClickListener(v -> actionButton());
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        rLayout.addView(btnAction, btnParams);

        seekbar = new SeekBar(this);
        seekbar.setId(R.id.seekbar_id);
        seekbar.setMax(100);
        seekbar.setProgress(50);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                started = false;
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
        btnParams.addRule(RelativeLayout.ALIGN_TOP, btnAction.getId());
        rLayout.addView(seekbar, btnParams);

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
        if ("START".equals(action)) {
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
        private static final int FRAME_RATE = 50;

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

        private final Runnable updateFrame = new Runnable() {
            @Override
            public void run() {
                if (handler != null) {
                    handler.removeCallbacks(updateFrame);
                    if (myCanvasView != null && myCanvasView.getWidth() != 0) {
                        updateMyCanvas();
                        myCanvasView.invalidate();
                    }
                    handler.postDelayed(updateFrame, FRAME_RATE);
                }
            }
        };

        void startFrameUpdates() {
            handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(updateFrame, 500);
        }

        void stopFrameUpdates() {
            if (handler != null) {
                handler.removeCallbacks(updateFrame);
            }
        }

        private void updateMyCanvas() {
            if (!started) {
                maxX = myCanvasView.getWidth();
                maxY = myCanvasView.getHeight();
                cellWidth = seekbar.getProgress() + 30;
                cols = maxX / cellWidth;
                rows = maxY / cellWidth;
                int xOffset = (maxX - (cols * cellWidth)) / 2;
                int yOffset = (maxY - (rows * cellWidth)) / 2;
                cells = new Cell[cols][rows];
                for (int c = 0; c < cols; c++) {
                    for (int r = 0; r < rows; r++) {
                        cells[c][r] = new Cell(c, r, cellWidth, xOffset, yOffset);
                    }
                }

                current = cells[0][0];
                current.setVisited();

                cells[0][0].walls[0] = false;
                cells[cols - 1][rows - 1].walls[2] = false;

                started = true;
            } else {
                next = getNeighbour(current);
                if (next != null) {
                    stack.add(current);
                    removeWalls(current, next);
                    current = next;
                    next.setVisited();
                } else if (!stack.isEmpty()) {
                    current = stack.get(stack.size() - 1);
                    stack.remove(stack.size() - 1);
                }
            }
        }

        void removeWalls(Cell first, Cell second) {
            if (first.col - second.col == 1) {
                cells[first.col][first.row].walls[3] = false;
                cells[second.col][second.row].walls[1] = false;
            }
            if (first.col - second.col == -1) {
                cells[first.col][first.row].walls[1] = false;
                cells[second.col][second.row].walls[3] = false;
            }
            if (first.row - second.row == 1) {
                cells[first.col][first.row].walls[0] = false;
                cells[second.col][second.row].walls[2] = false;
            }
            if (first.row - second.row == -1) {
                cells[first.col][first.row].walls[2] = false;
                cells[second.col][second.row].walls[0] = false;
            }
        }


        Cell getNeighbour(Cell core) {
            Random r = new Random();
            List<Cell> neighbours = new ArrayList<>();
            if (core.col > 0 && !cells[core.col - 1][core.row].visited) {
                neighbours.add(cells[core.col - 1][core.row]);
            }
            if (core.col < cols - 1 && !cells[core.col + 1][core.row].visited) {
                neighbours.add(cells[core.col + 1][core.row]);
            }
            if (core.row > 0 && !cells[core.col][core.row - 1].visited) {
                neighbours.add(cells[core.col][core.row - 1]);
            }
            if (core.row < rows - 1 && !cells[core.col][core.row + 1].visited) {
                neighbours.add(cells[core.col][core.row + 1]);
            }

            if (!neighbours.isEmpty()) {
                return neighbours.get(r.nextInt(neighbours.size()));
            } else {
                return null;
            }
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            if (!started) {
                return;
            }

            boolean allVisited = true;
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    cells[i][j].show(canvas);
                    allVisited = allVisited && cells[i][j].visited;
                }
            }

            current.highlight(canvas);

            String msg = "Cell Width:" + cellWidth;
            canvas.drawText(msg, 20, 25, paintText);
            msg = "Stack:" + stack.size();
            canvas.drawText(msg, 20, 50, paintText);

            if (allVisited && stack.isEmpty()) {
                msg = "# FINISHED #";
                canvas.drawText(msg, maxX / 4.0f, maxY / 2.0f, paintFinished);
                stopFrameUpdates();
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int desiredWidth = 500;
            int desiredHeight = 500;

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            int width, height;

            if (widthMode == MeasureSpec.EXACTLY) {
                width = widthSize;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desiredWidth, widthSize);
            } else {
                width = desiredWidth;
            }

            if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desiredHeight, heightSize);
            } else {
                height = desiredHeight;
            }

            setMeasuredDimension(width, height);
        }
    }

    static class Cell {
        int col, row;
        int x, y;
        int width;
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

        void setVisited() {
            this.visited = true;
        }

        void show(Canvas canvas) {
            if (visited) {
                canvas.drawRect(this.x, this.y, this.x + this.width, this.y + this.width, this.paint);
            }
            if (walls[0]) {
                canvas.drawLine(this.x, this.y, this.x + this.width, this.y, this.paintEdge);
            }
            if (walls[1]) {
                canvas.drawLine(this.x + this.width, this.y, this.x + this.width, this.y + this.width, this.paintEdge);
            }
            if (walls[2]) {
                canvas.drawLine(this.x, this.y + this.width, this.x + this.width, this.y + this.width, this.paintEdge);
            }
            if (walls[3]) {
                canvas.drawLine(this.x, this.y, this.x, this.y + this.width, this.paintEdge);
            }
        }

        void highlight(Canvas canvas) {
            canvas.drawCircle((this.x + this.width / 2.0f), this.y + this.width / 2.0f, this.width * 0.4f, this.highlight);
        }
    }
}
