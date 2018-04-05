package com.spc.codingtrain;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by shaun on 25/03/18 as first OpenGL ES SurfaceView attempt
 */

class MyGLSurfaceView extends GLSurfaceView {
    private static final String TAG = "GLES-SurfaceView";
    private final MyGLRenderer mRenderer;

    // Ensure that that XML view attributes are obtained and passed to super...
    public MyGLSurfaceView (Context context, AttributeSet attrs) {
        super (context, attrs);
        //Create an OpenGL ED 2.0 context
        setEGLContextClientVersion(2);
        mRenderer = new MyGLRenderer();
        //Set the renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
        // render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f/320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent (MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                //reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1;
                }

                //reverse direction of rotation to left of the mid-line
                if (x > getWidth() / 2) {
                    dy = dy * -1;
                }

                mRenderer.setAngle(
                        mRenderer.getAngle() +
                                ((dx + dy) * TOUCH_SCALE_FACTOR));
                requestRender();
        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}
