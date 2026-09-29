package com.spc.codingtrain;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        rLayout.setBackgroundColor(Color.BLACK);

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
        myCanvasView.setBackgroundColor(Color.BLACK);
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
        Paint starPaint;
        Paint fadePaint;
        TextPaint mTextPaint;
        boolean started = false;
        boolean isRunning = false;

        private long lastFrameTimeNanos = 0;
        private final Camera camera = new Camera();
        private final Matrix transformMatrix = new Matrix();

        int maxX, maxY;
        float posY;   // scroll position (Y offset for text layout)
        StaticLayout mTextLayout;
        int layoutWidth;

        // Starfield
        class Star {
            float x, y, radius;
            int alpha;
        }
        private final List<Star> stars = new ArrayList<>();

        private final Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (!isRunning) return;

                if (lastFrameTimeNanos > 0) {
                    float deltaSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000.0f;
                    // Cap delta to prevent jump after pause
                    if (deltaSeconds > 0.1f) deltaSeconds = 0.016f;

                    updateScroll(deltaSeconds);
                    invalidate();
                }
                lastFrameTimeNanos = frameTimeNanos;
                Choreographer.getInstance().postFrameCallback(this);
            }
        };

        MyCanvasView(Context context) {
            super(context);

            mTextPaint = new TextPaint();
            float density = getResources().getDisplayMetrics().density;
            mTextPaint.setTextSize(20 * density);
            mTextPaint.setColor(0xFFE5B13A); // Star Wars Gold
            mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mTextPaint.setAntiAlias(true);

            starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            starPaint.setColor(Color.WHITE);

            fadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        void startFrameUpdates() {
            isRunning = true;
            lastFrameTimeNanos = 0;
            Choreographer.getInstance().postFrameCallback(frameCallback);
        }

        void stopFrameUpdates() {
            isRunning = false;
            Choreographer.getInstance().removeFrameCallback(frameCallback);
        }

        private void initLayoutAndStars() {
            maxX = getWidth();
            maxY = getHeight();

            if (maxX == 0 || maxY == 0) return;

            // Generate starfield
            stars.clear();
            Random random = new Random(42); // Fixed seed for consistent star map
            for (int i = 0; i < 150; i++) {
                Star star = new Star();
                star.x = random.nextFloat() * maxX;
                star.y = random.nextFloat() * maxY;
                star.radius = 1.0f + random.nextFloat() * 2.5f;
                star.alpha = 100 + random.nextInt(155);
                stars.add(star);
            }

            // Top horizon fade gradient (fade text out as it reaches top horizon)
            LinearGradient fadeShader = new LinearGradient(
                    0, 0, 0, maxY * 0.30f,
                    Color.BLACK, Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
            );
            fadePaint.setShader(fadeShader);

            // Load text layout
            loadEpisodeText();
        }

        private void loadEpisodeText() {
            crawlText = readFileAsString(sourceFiles[current]);
            layoutWidth = (int) (maxX * 0.75f); // 75% width for nice 3D trapezoid proportion

            mTextLayout = StaticLayout.Builder.obtain(crawlText, 0, crawlText.length(), mTextPaint, layoutWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0.0f, 1.2f)
                    .setIncludePad(false)
                    .build();

            posY = maxY; // Start at the bottom of the screen
            started = true;
        }

        private void updateScroll(float deltaSeconds) {
            if (!started) return;

            float density = getResources().getDisplayMetrics().density;
            float speed = 35.0f * density; // Pixels per second
            posY -= speed * deltaSeconds;

            // Once text has scrolled off top horizon completely, load next episode
            if (mTextLayout != null && posY + mTextLayout.getHeight() < -200) {
                startNextFile();
            }
        }

        String readFileAsString(int resID) {
            try (InputStream inputStream = getResources().openRawResource(resID);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error reading raw resource file", e);
                return "";
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            initLayoutAndStars();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            // 1. Draw space background
            canvas.drawColor(Color.BLACK);

            // 2. Draw starfield
            for (Star star : stars) {
                starPaint.setAlpha(star.alpha);
                canvas.drawCircle(star.x, star.y, star.radius, starPaint);
            }

            // 3. Draw 3D scrolling text
            if (started && mTextLayout != null) {
                float density = getResources().getDisplayMetrics().density;
                float centerX = maxX / 2.0f;
                float centerY = maxY * 0.5f;

                camera.save();
                // Adjust camera location for screen density to avoid extreme Z clipping
                camera.setLocation(0, 0, -8.0f * density);
                camera.rotateX(60.0f); // 60 degree backwards pitch for Star Wars crawl angle
                camera.getMatrix(transformMatrix);
                camera.restore();

                // Center perspective rotation around canvas center pivot
                transformMatrix.preTranslate(-centerX, -centerY);
                transformMatrix.postTranslate(centerX, centerY);

                canvas.save();
                canvas.concat(transformMatrix);

                // Draw text layout centered horizontally at current posY scroll offset
                float layoutLeft = (maxX - layoutWidth) / 2.0f;
                canvas.translate(layoutLeft, posY);
                mTextLayout.draw(canvas);

                canvas.restore();

                // 4. Draw horizon fade mask over top 30% of screen
                canvas.drawRect(0, 0, maxX, maxY * 0.30f, fadePaint);
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
                if (!isRunning) {
                    actionButton();
                } else {
                    startNextFile();
                }
            }
            return true;
        }

        void startNextFile() {
            current++;
            if (current >= sourceFiles.length) {
                current = 0;
            }
            loadEpisodeText();
            invalidate();
        }
    }
}
