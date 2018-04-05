package com.spc.codingtrain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class OpenGLActivity extends AppCompatActivity {

    private static final String TAG = "OPENGL-BASICS";
    TextView tv_KeyInfo, tv_LogInfo;
    private MyGLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_opengl);
        tv_KeyInfo = findViewById(R.id.keyInfo);
        tv_LogInfo = findViewById(R.id.logInfo);
        glSurfaceView = findViewById(R.id.surfaceView);
    }

    public void doneButtonClick(View v) {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pauses the rendering thread
        glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resumes the rendering thread
        glSurfaceView.onResume();
    }
}

