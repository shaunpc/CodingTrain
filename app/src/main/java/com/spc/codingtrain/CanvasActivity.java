package com.spc.codingtrain;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class CanvasActivity extends AppCompatActivity {

    private static final String TAG = "CANVAS-BASICS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_canvas);
    }

    public void doneButtonClick(View v) {
        finish();
    }
}
