package com.example.missingpartsdetection.utils;
import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ZoomableImageView extends androidx.appcompat.widget.AppCompatImageView {
    private Matrix matrix = new Matrix();
    private float[] lastEvent = null;
    private float startDistance = 0f;
    private float scaleFactor = 1f;
    private float minScale = 1f;
    private float maxScale = 5f;

    public ZoomableImageView(Context context) {
        super(context);
        init();
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(new TouchListener());
    }

    private class TouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    lastEvent = null;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    startDistance = getDistance(event);
                    if (startDistance > 10f) {
                        float[] midPoint = getMidPoint(event);
                        matrix.set(getImageMatrix());
                        lastEvent = new float[4];
                        lastEvent[0] = midPoint[0];
                        lastEvent[1] = midPoint[1];
                        lastEvent[2] = event.getX(0);
                        lastEvent[3] = event.getX(1);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 2 && startDistance > 10f) {
                        // 双指缩放
                        float newDist = getDistance(event);
                        if (newDist > 10f) {
                            float scale = newDist / startDistance;
                            scaleFactor *= scale;
                            if (scaleFactor > maxScale) scaleFactor = maxScale;
                            if (scaleFactor < minScale) scaleFactor = minScale;

                            matrix.set(getImageMatrix());
                            matrix.postScale(scaleFactor, scaleFactor, lastEvent[0], lastEvent[1]);
                            setImageMatrix(matrix);
                        }
                        startDistance = newDist;
                    } else if (event.getPointerCount() == 1 && lastEvent != null) {
                        // 单指拖动
                        matrix.set(getImageMatrix());
                        float dx = event.getX() - lastEvent[2];
                        float dy = event.getY() - lastEvent[3];
                        matrix.postTranslate(dx, dy);
                        setImageMatrix(matrix);
                        lastEvent[2] = event.getX();
                        lastEvent[3] = event.getY();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    lastEvent = null;
                    break;
            }
            return true;
        }

        private float getDistance(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        }

        private float[] getMidPoint(MotionEvent event) {
            float[] midPoint = new float[2];
            midPoint[0] = (event.getX(0) + event.getX(1)) / 2;
            midPoint[1] = (event.getY(0) + event.getY(1)) / 2;
            return midPoint;
        }
    }
}