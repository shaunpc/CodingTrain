package com.spc.codingtrain;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CODINGTRAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void buttonPress (View v) {
        Intent intent = null;
        Button button = (Button) v;
        String msg = button.getText().toString();
        switch (v.getId()) {
            case R.id.button1: // OpenGL
                intent = new Intent(MainActivity.this, OpenGLActivity.class);
                break;
            case R.id.button2: // Canvas Basics
                intent = new Intent(MainActivity.this, CanvasActivity.class);
                break;
            case R.id.button3: // Fireworks
                intent = new Intent(MainActivity.this, FireworksActivity.class);
                break;
            case R.id.button4: // Metaballs
                intent = new Intent(MainActivity.this, MetaballsActivity.class);
                break;
            case R.id.button5: // Smart Rockets
                intent = new Intent(MainActivity.this, SmartRocketsActivity.class);
                break;
            case R.id.button6: // Double Pendulum
                intent = new Intent(MainActivity.this, PendulumActivity.class);
                break;
            case R.id.button7: // Snakes & Ladders
                intent = new Intent(MainActivity.this, SnakesAndLadders.class);
                break;
            case R.id.button8: // Circle Packing
                intent = new Intent(MainActivity.this, CirclePackingActivity.class);
                break;
            default:
                Log.i(TAG, "Unavailable feature: " + msg);
                Toast toast = Toast.makeText(getApplicationContext(),msg,LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0,0);
                if (button.getCurrentTextColor() == Color.RED) {
                    button.setVisibility(View.GONE);
                    toast.setText("Donkey! Still not available! Removing from menu...");
                } else {
                    button.setTextColor(Color.RED);
                    toast.setText("Sorry, unavailable feature: " + msg);
                }
                toast.show();
                break;
        }

        if (intent != null) {
            Log.i(TAG, "Starting intent for " + msg + "(" + intent.toString() + ")");
            startActivity(intent);
        }
    }
}