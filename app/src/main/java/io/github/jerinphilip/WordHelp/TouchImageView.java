package io.github.jerinphilip.WordHelp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends ImageView {
    Matrix matrix;
    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 3f;
    float[] m;
    int viewWidth, viewHeight;
    static final int CLICK = 3;
    float saveScale = 1f;
    protected float origWidth, origHeight;
    int oldMeasuredWidth, oldMeasuredHeight;
    ScaleGestureDetector mScaleDetector;
    GestureDetector gDetector;
    boolean frozen = false;
    int xs, ys, xe, ye;

    Context context;

    Paint paint = new Paint();

    public TouchImageView(Context context) {
        super(context);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(5);
        sharedConstructing(context);
        reset();
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
        reset();
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gDetector = new GestureDetector(context, new GestureListener());
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                gDetector.onTouchEvent(event);
                PointF curr = new PointF(event.getX(), event.getY());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        last.set(curr);
                        start.set(last);
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;
                            float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);
                            float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);
                            if (!frozen)
                                matrix.postTranslate(fixTransX, fixTransY);
                            fixTrans();
                            last.set(curr.x, curr.y);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mode = NONE;
                        if ( frozen){
                            int [] coords_start, coords_last;
                            coords_start = getPointerCoords(start.x, start.y);
                            coords_last = getPointerCoords(last.x, last.y);
                            Log.d("transformed start:", String.valueOf(coords_start[0])+","+String.valueOf(coords_start[1]));
                            Log.d("transformed end:", String.valueOf(coords_last[0])+","+String.valueOf(coords_last[1]));
                            setCropRect(coords_start, coords_last);
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                }
                setImageMatrix(matrix);
                invalidate();
                return true; // indicate event was handled
            }
        });

    }

    public void setCropRect(int[] s, int[] e){
        xs = Math.min(s[0], e[0]);
        ys = Math.min(s[1], e[1]);

        xe = Math.max(s[0], e[0]);
        ye = Math.max(s[1], e[1]);
    }

    public int[] getCropDimensions(){
        int [] result = new int[]{xs, ys, xe-xs, ye-ys};
        return result;
    }

    public void setMaxZoom(float x) {
        maxScale = x;
    }

    @Override
    protected void  onDraw(Canvas canvas){
        super.onDraw(canvas);
        // Draw the lines
        if(frozen)
            canvas.drawLine(start.x, start.y, last.x, last.y, paint);

    }

    void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];
        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);
        if (fixTransX != 0 || fixTransY != 0)
            matrix.postTranslate(fixTransX, fixTransY);
    }

    final int[] getPointerCoords(float x, float y)
    {
        final float[] coords = new float[] { x, y};
        final int [] coords_i = new int []{0, 0};
        Matrix newMatrix = new Matrix();
        matrix.invert(newMatrix);
        newMatrix.postTranslate(getScrollX(), getScrollY());
        newMatrix.mapPoints(coords);
        coords_i[0] = (int)Math.floor(coords[0]);
        coords_i[1] = (int)Math.floor(coords[1]);
        return coords_i;
    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;
        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }
        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0)
            return;
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;
        if (saveScale == 1) {
            //Fit to screen.
            float scale;
            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
                return;
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();
            float scaleX = (float) viewWidth / (float) bmWidth;
            float scaleY = (float) viewHeight / (float) bmHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);
            // Center the image
            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;
            matrix.postTranslate(redundantXSpace, redundantYSpace);
            origWidth = viewWidth - 2 * redundantXSpace;
            origHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }
        fixTrans();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //float x = e.getX();
            //float y = e.getY();
            frozen = !frozen;
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }
            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)
                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);
            else
                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
            fixTrans();
            return true;
        }
    }

    public void reset(){
        matrix = new Matrix();
        saveScale = 1.0f;
        mode = NONE;
        m = new float[9];
        frozen = false;

    }
    /*

    @Override
    public Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putParcelable("matrix", matrix);
        bundle.putInt("stuff", this.stuff); // ... save stuff
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            this.stuff = bundle.getInt("stuff"); // ... load stuff
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }

    */
}
