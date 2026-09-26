package com.spc.codingtrain;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.spc.library.MyColor;
import com.spc.library.Point2D;

public class CirclePackingActivity extends AppCompatActivity {

    private static final String TAG = "CIRCLEPACK";
    private String action = "START";
    Button btnAction, btnImage;
    EditText enterText;
    MyCanvasView myCanvasView;
    Outline template;
    private static final int FILL_MODE_FULL = 0;        // full screen
    private static final int FILL_MODE_TEXT = 1;        // use entered text outline
    private static final int FILL_MODE_IMAGE = 2;       // use image colours
    public static final int PICK_IMAGE = 1; // ensure we listen for the right action intent result
    int fill_mode = FILL_MODE_FULL;


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
        btnAction.setOnClickListener(v -> actionButton());
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        rLayout.addView(btnAction, btnParams);

        // Next, add the "Enter Text" Button on the left of that "ACTION" button
        enterText = new EditText(this);
        enterText.setId(R.id.entertext_button_id);
        enterText.setHint(R.string.enter_text_button);
        enterText.setTextColor(Color.WHITE);
        enterText.setHintTextColor(Color.YELLOW);
        enterText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        enterText.setLines(1);
        enterText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                processEnteredText(enterText.getText().toString());
                return true;
            }
            return false;
        });
        btnParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.LEFT_OF, btnAction.getId());
        rLayout.addView(enterText, btnParams);

        // Next, add the getImage Button at the right/bottom of the screen
        btnImage = new Button(this);
        btnImage.setId(R.id.image_button_id);
        btnImage.setText(R.string.image_button);
        btnImage.setOnClickListener(v -> imageButton());
        btnParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.RIGHT_OF, btnAction.getId());
        rLayout.addView(btnImage, btnParams);

        // Then, fill the rest with the MyCanvasView class
        myCanvasView = new MyCanvasView(this);
        myCanvasView.setId(R.id.canvas_view_id);
        myCanvasView.setBackgroundColor(Color.BLUE);
        RelativeLayout.LayoutParams cParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        cParams.addRule(RelativeLayout.ABOVE, btnAction.getId());
        cParams.addRule(RelativeLayout.ABOVE, enterText.getId());
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

    void imageButton () {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            Log.i(TAG, "Activity ResultCode=" + resultCode);
            if (data != null && data.getData() != null) {
                template = new Outline(data.getData());
                Log.i(TAG, "imageUri is " + data.getData());
                fill_mode = FILL_MODE_IMAGE; // instruct onDraw to only create circles based on colours
                myCanvasView.setBackgroundColor(Color.BLUE);  // clears the screen
                if (!myCanvasView.circles.isEmpty()) {      // reset the circles list
                    myCanvasView.circles = new ArrayList<>();
                }
            }
        }
    }

    void processEnteredText (String enteredText) {
        template = new Outline(enteredText);
        Log.i(TAG,"Ideal font size for "+enteredText+" is "+ template.paintET.getTextSize());
        fill_mode = FILL_MODE_TEXT; // instruct onDraw to only create circles witing the template
        myCanvasView.setBackgroundColor(Color.BLUE);  // clears the screen
        if (!myCanvasView.circles.isEmpty()) {      // reset the circles list
            myCanvasView.circles = new ArrayList<>();
        }
    }

    class Outline {
        String textOutline;
        float x, y;
        double textSize;
        Paint paintET;
        Bitmap bitmap;
        Canvas canvas;
        Rect bounds = new Rect();
        List<Point2D> points = new ArrayList<>();
        String imageUri;

        Outline (String textOutline) {  // created with some entered text to use as outline

            Log.i(TAG,"Creating template from String:" + textOutline + " (len="+textOutline.length()+")");
            if (textOutline.contentEquals("'") || textOutline.contentEquals("\"")) {
                textOutline = "error";
            }
            this.textOutline = textOutline;

            this.paintET = new Paint();
            this.paintET.setColor(Color.WHITE);
            this.paintET.setTypeface(Typeface.DEFAULT_BOLD);

            // figure out best text size, to fill screen as much as possible...
            double maxX = myCanvasView.getWidth();
            double maxY = myCanvasView.getHeight();
            for (int i = 10 ; i < maxY*0.9; i=i+10) {
                this.paintET.setTextSize(i);
                this.paintET.getTextBounds(this.textOutline, 0, this.textOutline.length(), this.bounds);
                Log.i(TAG, "Size:"+ i + " W:"+ this.bounds.width()+ " H:"+ this.bounds.height() + " Max:"+maxX+","+maxY);
                if (this.bounds.width() > maxX * 0.9|| this.bounds.height() > maxY * 0.9) {
                    break;
                }
            }
            this.textSize = this.paintET.getTextSize();
            this.x = (float) (maxX - this.bounds.width())/2;
            this.y = (float) (maxY - ((maxY - this.bounds.height())/2)) - this.bounds.bottom ;
            Log.i(TAG,"Bounds:"+this.bounds.toString()+"/W:"+this.bounds.width()+"/H:"+this.bounds.height());
            Log.i(TAG,"Draw text at (X="+this.x+",Y="+this.y);

            // now create a blank bitmap, and a canvas to draw on it, then "show" it...
            this.bitmap = Bitmap.createBitmap((int) maxX,(int) maxY, Bitmap.Config.ARGB_8888);
            this.canvas = new Canvas (this.bitmap);
            this.canvas.drawText(this.textOutline, this.x,this.y, this.paintET);

            // now record which  (x,y) represent the white text areas...
            for (int x = 0; x < maxX; x++) {
                for (int y = 0; y < maxY; y++) {
                    if (this.bitmap.getPixel(x,y) == Color.WHITE) {
                        this.points.add(new Point2D(x,y));
                    }
                }
            }

            Log.i(TAG,"Found "+this.points.size()+ " pixels coloured WHITE, from "+(maxX*maxY));

        }

        Outline (Uri imageUri) { // created with a URI of image to use as colour guide...
            this.imageUri = imageUri.getPath();
            Log.i(TAG,"URI="+imageUri+ " / PATH="+this.imageUri);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.Source source = ImageDecoder.createSource(getBaseContext().getContentResolver(), imageUri);
                    this.bitmap = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true);
                } else {
                    InputStream is = getBaseContext().getContentResolver().openInputStream(imageUri);
                    this.bitmap = BitmapFactory.decodeStream(is);
                    if (is != null) is.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error decoding image URI", e);
            }
            if (this.bitmap != null) {
                Log.i(TAG, "Decoded bitmap size is " + this.bitmap.getWidth() + " by " + this.bitmap.getHeight());
                double maxX = myCanvasView.getWidth();
                double maxY = myCanvasView.getHeight();
                this.bitmap = resize(this.bitmap, (int) maxX, (int) maxY);
                Log.i(TAG, "Re-sized bitmap size is " + this.bitmap.getWidth() + " by " + this.bitmap.getHeight());

                this.bounds = new Rect((int) ((maxX - this.bitmap.getWidth()) / 2),
                        (int) ((maxY - this.bitmap.getHeight()) / 2),
                        (int) (maxX - ((maxX - this.bitmap.getWidth()) / 2)),
                        (int) (maxY - ((maxY - this.bitmap.getHeight()) / 2)));
            }
        }


        void show (Canvas canvas) {
            if (fill_mode == FILL_MODE_TEXT && !this.textOutline.isEmpty()) {    // draw text if we have some
                canvas.drawText(this.textOutline, this.x,this.y, this.paintET);
                canvas.drawRect(this.bounds, this.paintET);
            }

            if (fill_mode == FILL_MODE_IMAGE && this.bitmap != null) {
                canvas.drawBitmap(this.bitmap,null, this.bounds,null);
            }
        }

        private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
            if (maxHeight > 0 && maxWidth > 0) {
                int width = image.getWidth();
                int height = image.getHeight();
                float ratioBitmap = (float) width / (float) height;
                float ratioMax = (float) maxWidth / (float) maxHeight;

                int finalWidth = maxWidth;
                int finalHeight = maxHeight;
                if (ratioMax > ratioBitmap) {
                    finalWidth = (int) ((float)maxHeight * ratioBitmap);
                } else {
                    finalHeight = (int) ((float)maxWidth / ratioBitmap);
                }
                image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
                return image;
            } else {
                return image;
            }
        }

    }

    class MyCanvasView extends View {
        Paint paintCanvas, paintText;
        boolean started = false;
        private Handler handler;
        private static final int FRAME_RATE = 20; // 50 frames per second
        List<Circle> circles = new ArrayList<>();
        String msg;

        MyCanvasView(Context context) {
            super(context);
            paintCanvas = new Paint();
            paintCanvas.setStyle(Paint.Style.STROKE);
            paintCanvas.setColor(Color.BLACK);
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
            Random r = new Random();
            int maxX = getWidth();
            int maxY = getHeight();

            if (!started) {
                started = true;
            } else {
                switch (fill_mode) {
                    case FILL_MODE_FULL:
                        createNewCircle(r.nextInt(maxX), r.nextInt(maxY));
                        break;

                    case FILL_MODE_TEXT:
                        int index;
                        for (int i=0; i < template.textOutline.length(); i++) {
                            index = r.nextInt(template.points.size());
                            createNewCircle((int) template.points.get(index).getX(),
                                    (int) template.points.get(index).getY());
                        }
                        break;

                    case FILL_MODE_IMAGE:
                        int iX, iY, iC;
                        for (int i=0; i < template.bounds.width()/50; i++) {
                            iX = r.nextInt(template.bounds.width());
                            iY = r.nextInt(template.bounds.height());
                            iC = template.bitmap.getPixel(iX, iY);
                            createNewCircle(iX + template.bounds.left, iY + template.bounds.top, iC);
                        }
                        break;
                }

                for (Circle c : circles) {
                    if (c.growing) {
                        c.grow();
                        if (c.edges()) {
                            c.growing = false;
                        }
                        if (!checkNoOverlap(c)) {
                            c.growing = false;
                        }
                    }
                }
            }
        }


        void createNewCircle (int x, int y) {
            Circle newCircle = new Circle(x,y,1);
            if (checkNoOverlap(newCircle)) {
                circles.add(newCircle);
            }
        }

        void createNewCircle (int x, int y, int col) {
            Circle newCircle = new Circle(x,y,1, col);
            if (checkNoOverlap(newCircle)) {
                circles.add(newCircle);
            }
        }

        boolean checkNoOverlap (Circle checkCircle) {
            boolean noOverlap = true;
            for (Circle other : circles) {
                if (checkCircle.overlapping(other)) {
                    noOverlap = false;
                    break;
                }
            }
            return noOverlap;
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            if (started) {
                canvas.drawPaint(paintCanvas);

                for (Circle c : circles) {
                    c.show(canvas);
                }

                msg = "Circles:" + circles.size();
                canvas.drawText(msg, 20, 25, paintText);

                if (template != null) {
                    if (fill_mode == FILL_MODE_TEXT) {
                        msg = "TEXT:" + template.textOutline + " (" + template.textOutline.length()+") ";
                        msg = msg + "size="+template.textSize;
                    } else {
                        msg = "IMAGE:" + template.imageUri;
                    }
                    canvas.drawText(msg, 20, 50, paintText);
                    msg = "bounds="+template.bounds.toShortString()+" W="+template.bounds.width()+" H="+template.bounds.height();
                    canvas.drawText(msg, 20, 75, paintText);
                }
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

                if (fill_mode == FILL_MODE_FULL) {
                    createNewCircle((int) event.getX(),(int) event.getY());
                }
            }

            return true;
        }
    }

    class Circle {
        float x;
        float y;
        float r;
        boolean growing = true;
        MyColor colour;
        Paint paintCircle = new Paint();

        Circle (float x, float y, float r) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.paintCircle.setStrokeWidth(5);
            this.colour = new MyColor();
            this.paintCircle.setStyle(Paint.Style.STROKE);
            this.paintCircle.setARGB(255, colour.r, colour.g, colour.b);
        }

        Circle (float x, float y, float r, int col) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.paintCircle.setStrokeWidth(5);
            this.paintCircle.setStyle(Paint.Style.FILL_AND_STROKE);
            this.paintCircle.setColor(col);
        }

        void show(Canvas canvas) {
            canvas.drawCircle(this.x, this.y, this.r, this.paintCircle);
        }

        void grow() {
            if (this.growing) {
                this.r++;
            }
        }

        boolean edges() {
            return (this.x-this.r < 0 || this.y-this.r < 0 ||
                    this.x+this.r > myCanvasView.getWidth() ||
                    this.y+this.r > myCanvasView.getHeight());
        }

        boolean overlapping(Circle other) {
            if (this.equals(other)) {
                return false;
            }
            float distance =  (float) Math.sqrt(Math.pow(this.x - other.x, 2)+Math.pow(this.y - other.y, 2));
            float result = distance - this.r - other.r;
            return (result < 4);
        }
    }
}
