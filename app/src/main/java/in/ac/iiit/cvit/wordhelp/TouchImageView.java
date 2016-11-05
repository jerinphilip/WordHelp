package in.ac.iiit.cvit.wordhelp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * TODO: document your custom view class.
 */
public class TouchImageView extends ImageView {
    Context _context;
    ImageMatrix matrix;

    ScaleGestureDetector sGD;
    GestureDetector gD;

    Path swipe;
    ArrayList<PointF> swipePath;
    ArrayList<Point> swipePathImage;

    Paint paint;

    private int op_mode;

    private int ZOOM = 1, DRAW = 0;


    int viewWidth, viewHeight;


    public TouchImageView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TouchImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        /* Custom init function to initialize from all three constructors above. */
        super.setClickable(true);
        super.setWillNotDraw(false);

        this._context = context;

        /* Initialize listeners */

        sGD = new ScaleGestureDetector(context, new ScaleListener());
        gD = new GestureDetector(context, new GestureListener());


        /* Set images and stuff. */
        matrix = new ImageMatrix();
        setImageMatrix(matrix.A);
        setScaleType(ScaleType.MATRIX);

        /* CanvasPath and the Points */
        swipe = new Path();
        swipePath = new ArrayList<>();
        swipePathImage = new ArrayList();

        /* Paint Setup */
        int strokeWidth = 10;
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);


        /* Listener */
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                sGD.onTouchEvent(motionEvent);
                gD.onTouchEvent(motionEvent);


                PointF touch = new PointF(motionEvent.getX(), motionEvent.getY());
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        /* Reset and start path again */

                        swipe.reset();
                        swipe.moveTo(touch.x, touch.y);

                        swipePath.clear();
                        swipePath.add(touch);

                        break;
                    case MotionEvent.ACTION_UP:
                        /* If swipe crop and send. Else send entire image and coordinates. */
                        PointF scroll = new PointF();
                        scroll.set(getScrollX(), getScrollY());
                        swipePathImage = matrix.ImageViewCoordinates(swipePath, scroll);
                        //WUtils.debugPoints(Points);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        /* Keep updating path and add points to swipePath */
                        swipe.lineTo(touch.x, touch.y);
                        swipePath.add(touch);
                        break;
                }

                invalidate();
                return true;
            }
        });


    }

    public void resetCanvas(){
        swipePath.clear();
        swipe.reset();
        swipePathImage.clear();
        matrix.reset();
        setImageMatrix(matrix.A);
        invalidate();

    }


    public ArrayList<Point> getSwipePathImage(){
        return swipePathImage;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(swipe, paint);

        /* */
        Drawable image = getDrawable();
        int width, height;
        width = image.getIntrinsicWidth();
        height = image.getIntrinsicHeight();
        RectF bbox = WUtils.boundingBox(swipePath, width, height);
        matrix.A.mapRect(bbox, bbox);
        canvas.drawRect(bbox, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        Drawable image = getDrawable();
        int width, height;
        width = image.getIntrinsicWidth();
        height = image.getIntrinsicHeight();

        matrix.fitImage(viewWidth, viewHeight, width, height);
        setImageMatrix(matrix.A);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e){
            return true;
        }

        public boolean onDoubleTap(MotionEvent e){
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector){
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector){
            op_mode = ZOOM;
            Log.d("Scale", "Detected Scale");
            float mScaleFactor;
            PointF touch = new PointF();

            mScaleFactor = detector.getScaleFactor();
            touch.set(detector.getFocusX(), detector.getFocusY());
            matrix.adjustToScale(mScaleFactor, touch, viewWidth, viewHeight);
            setImageMatrix(matrix.A);
            return true;
        }
    }


}
