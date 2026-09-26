package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TextCrawl extends AppCompatActivity {

    private static final String TAG = "TEXTCRAWL";
    private String action = "START";
    Button btnAction;
    MyCanvasView myCanvasView;
    int[] sourceFiles;
    int current;
    String crawlText;

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
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        rLayout.addView(btnAction, btnParams);

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

        sourceFiles = new int[] {R.raw.episode_i, R.raw.episode_ii, R.raw.episode_iii,
                                R.raw.episode_iv, R.raw.episode_v, R.raw.episode_vi,
                                R.raw.episode_vii, R.raw.episode_viii};
        current = 0; // Episode I

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
        Paint paintCanvas, paintText;
        TextPaint mTextPaint;
        boolean started = false;
        private Handler handler;
        private static final int FRAME_RATE = 25; // 50 frames per second
        Camera camera = new Camera();
        int maxX, maxY;
        int posY;   // where start the text draw
        StaticLayout mTextLayout;


        MyCanvasView(Context context) {
            super(context);
            paintCanvas = new Paint();
            paintCanvas.setStyle(Paint.Style.STROKE);
            paintCanvas.setColor(Color.BLACK);
            mTextPaint = new TextPaint();
            mTextPaint.setTextSize(16 * getResources().getDisplayMetrics().density);
            mTextPaint.setARGB(255, 229, 177,58);
            mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
            paintText = new Paint();
            paintText.setTextSize(25);
            paintText.setColor(Color.WHITE);

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
            maxX = getWidth();
            maxY = getHeight();

            if (!started) {
                crawlText = readFileAsString(sourceFiles[current]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mTextLayout = StaticLayout.Builder.obtain(crawlText, 0, crawlText.length(), mTextPaint, maxX)
                            .setAlignment(Layout.Alignment.ALIGN_CENTER)
                            .setLineSpacing(0.0f, 1.0f)
                            .setIncludePad(false)
                            .build();
                } else {
                    mTextLayout = new StaticLayout(crawlText, mTextPaint, maxX,
                            Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
                }
                posY = maxY;
                started = true;
            } else {
                if (posY + mTextLayout.getHeight() < 0 ) {
                    startNextFile();
                }
            }
        }

        String readFileAsString (int resID) {
            InputStream inputStream = getResources().openRawResource(resID);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            try {
                int i = inputStream.read();
                while (i != -1) {
                    byteArrayOutputStream.write(i);
                    i = inputStream.read();
                }
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error reading raw resource file", e);
            }
            return (byteArrayOutputStream.toString());
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            if (started) {
                canvas.drawPaint(paintCanvas);

                camera.save();
                canvas.save();
                camera.translate((float) (maxX / 2.0), maxY, 0);
                camera.setLocation(0, 0, 200);
                camera.rotate(45, 0, 0);
                camera.applyToCanvas(canvas);
                canvas.translate((float) (-maxX / 2.0), posY);
                mTextLayout.draw(canvas);
                canvas.restore();
                camera.restore();
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

        @Override
        public boolean performClick() {
            return super.performClick();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                performClick();
                if (handler == null) {
                    actionButton();
                }
                startNextFile();
            }

            return true;
        }

        void startNextFile() {
            current++;
            if (current > sourceFiles.length-1) {
                current = 0;
            }
            started = false;
        }
    }
}
