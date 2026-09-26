package com.spc.codingtrain;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CODINGTRAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // enable the hyperlink to Coding Train on the textview
        TextView tv = findViewById(R.id.textView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv.setText(Html.fromHtml(getString(R.string.intro), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv.setText(Html.fromHtml(getString(R.string.intro)));
        }
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void buttonPress (View v) {
        Intent intent = null;
        Button button = (Button) v;
        String msg = button.getText().toString();
        int id = v.getId();
        if (id == R.id.button1) { // OpenGL
            intent = new Intent(MainActivity.this, OpenGLActivity.class);
        } else if (id == R.id.button2) { // Canvas Basics
            intent = new Intent(MainActivity.this, CanvasActivity.class);
        } else if (id == R.id.button3) { // Fireworks
            intent = new Intent(MainActivity.this, FireworksActivity.class);
        } else if (id == R.id.button4) { // Metaballs
            intent = new Intent(MainActivity.this, MetaballsActivity.class);
        } else if (id == R.id.button5) { // Smart Rockets
            intent = new Intent(MainActivity.this, SmartRocketsActivity.class);
        } else if (id == R.id.button6) { // Double Pendulum
            intent = new Intent(MainActivity.this, PendulumActivity.class);
        } else if (id == R.id.button7) { // Snakes & Ladders
            intent = new Intent(MainActivity.this, SnakesAndLadders.class);
        } else if (id == R.id.button8) { // Circle Packing
            intent = new Intent(MainActivity.this, CirclePackingActivity.class);
        } else if (id == R.id.button9) { // Mitosis
            intent = new Intent(MainActivity.this, MitosisActivity.class);
        } else if (id == R.id.button11) { // Langton's Ant
            intent = new Intent(MainActivity.this, LangtonsAnt.class);
        } else if (id == R.id.button12) { // Phyllotaxis
            intent = new Intent(MainActivity.this, Phyllotaxis.class);
        } else if (id == R.id.button13) { // Perlin Noise
            intent = new Intent(MainActivity.this, PerlinNoise.class);
        } else if (id == R.id.button14) { // Maze Generation
            intent = new Intent(MainActivity.this, MazeGenActivity.class);
        } else if (id == R.id.button18) { // 3D Text Crawl
            intent = new Intent(MainActivity.this, TextCrawl.class);
        } else {
            Log.i(TAG, "Unavailable feature: " + msg);
            Toast toast = Toast.makeText(getApplicationContext(), msg, LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            if (button.getCurrentTextColor() == Color.RED) {
                button.setVisibility(View.GONE);
                toast.setText("Donkey! Still not available! Removing from menu...");
            } else {
                button.setTextColor(Color.RED);
                toast.setText("Sorry, unavailable feature: " + msg);
            }
            toast.show();
        }

        if (intent != null) {
            Log.i(TAG, "Starting intent for " + msg + "(" + intent.toString() + ")");
            startActivity(intent);
        }
    }
}