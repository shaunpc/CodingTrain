package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

public class LangtonsAnt extends AppCompatActivity {

    private static final String TAG = "ANT";
    private String action = "START";
    Button btnAction;
    SeekBar seekbar;
    MyCanvasView myCanvasView;
    static final int UP = 0;
    static final int RIGHT = 1;
    static final int DOWN = 2;
    static final int LEFT = 3;
    int direction;
    int antCol;
    int antRow;
    int maxX, maxY;
    int cellWidth;
    boolean started = false;
    int cols, rows;
    int antMoves;
    Cell[][] cells;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        RelativeLayout rLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(rLayout, rlParams);
        rLayout.setBackgroundColor(Color.DKGRAY);

        ViewCompat.setOnApplyWindowInsetsListener(rLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    insets.left,
                    insets.top,
                    insets.right,
                    insets.bottom
            );

            return WindowInsetsCompat.CONSUMED;
        });

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
        seekbar.setMax(80);
        seekbar.setProgress(25);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                started = false;
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
        myCanvasView.setBackgroundColor(Color.BLUE);
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
        public Paint paintText;
        private Handler handler;
        private static final int FRAME_RATE = 5;

        MyCanvasView(Context context) {
            super(context);
            paintText = new Paint();
            paintText.setTextSize(25);
            paintText.setColor(Color.BLUE);
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
                cellWidth = seekbar.getProgress() + 5;
                cols = maxX / cellWidth;
                rows = maxY / cellWidth;
                cells = new Cell[cols][rows];
                for (int i = 0; i < cols; i++) {
                    for (int j = 0; j < rows; j++) {
                        cells[i][j] = new Cell((i * cellWidth), (j * cellWidth), cellWidth);
                    }
                }

                antCol = cols / 2;
                antRow = rows / 2;
                direction = UP;
                started = true;
            } else {
                switch (cells[antCol][antRow].getColour()) {
                    case Color.WHITE:
                        cells[antCol][antRow].flipColour();
                        direction++;
                        if (direction > LEFT) {
                            direction = UP;
                        }
                        break;
                    case Color.BLACK:
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

                antMoves++;
            }
        }


        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            if (!started) {
                return;
            }

            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    cells[i][j].show(canvas);
                }
            }

            String msg = "Cell Width:" + cellWidth;
            canvas.drawText(msg, 20, 25, paintText);
            msg = "Ant Moves:" + antMoves;
            canvas.drawText(msg, 20, 50, paintText);
            msg = "Ant Position: (" + antCol + "," + antRow + ")";
            canvas.drawText(msg, 20, 75, paintText);
            msg = "Ant Direction: " + direction;
            canvas.drawText(msg, 20, 100, paintText);

            cells[antCol][antRow].highlight(canvas);
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
        int x, y;
        int width;
        Paint paint;
        Paint paintEdge;
        Paint highlight;

        Cell(int x, int y, int width) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.paint = new Paint();
            this.paint.setColor(Color.WHITE);
            this.paint.setStyle(Paint.Style.FILL);
            this.paintEdge = new Paint();
            this.paintEdge.setColor(Color.CYAN);
            this.paintEdge.setStyle(Paint.Style.STROKE);
            this.paintEdge.setStrokeWidth(2);
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
            canvas.drawRect(this.x, this.y, this.x + this.width, this.y + this.width, this.paintEdge);
        }

        void highlight(Canvas canvas) {
            canvas.drawCircle((this.x + this.width / 2.0f), this.y + this.width / 2.0f, this.width / 2.0f, this.highlight);
        }
    }
}
