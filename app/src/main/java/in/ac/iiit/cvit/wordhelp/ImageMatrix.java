package in.ac.iiit.cvit.wordhelp;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;

/**
 * All operations to be adjusted on the ImageMatrix goes here.
 * API's to convert ImageView coordinates to image coordinates also here.
 */

public class ImageMatrix {
    public Matrix A;
    private float scale, maxScale, minScale;
    private Matrix A_inverse;
    private int viewWidth, viewHeight;
    public ImageMatrix(){
        /* Hardcoding scale for now. */
        scale = 1.0f;
        minScale = 1.0f;
        maxScale = 5.0f;
        A = new Matrix();
        A_inverse = new Matrix();
    }

    public void reset(){
    }

    public void fitImage(int viewWidth, int viewHeight, int width, int height){
        float scaleX, scaleY;
        scaleX = (float)viewWidth/(float)width;
        scaleY = (float)viewHeight/(float)height;

        float scale;
        scale = Math.min(scaleX, scaleY);
        A.setScale(scale, scale);
    }

    public void adjustToScale(float factor, PointF focus, int viewWidth, int viewHeight){
        float prospectiveScale;
        prospectiveScale = scale * factor;

        /* Check new scale doesn't exceed bounds. */
        prospectiveScale = Math.min(prospectiveScale, maxScale);
        prospectiveScale = Math.max(prospectiveScale, minScale);

        /* Adjust factor, scale accordingly */
        factor = prospectiveScale/scale;
        scale = prospectiveScale;

        A.postScale(factor, factor, focus.x, focus.y);
    }

    public ArrayList<Point> ImageViewCoordinates(ArrayList<PointF> Points, PointF scroll){

        /* Some pre-processing */
        float[] toTransform = new float[2* Points.size()];
        int index = 0;
        for(PointF p: Points){
            toTransform[index] = p.x;
            toTransform[index+1] = p.y;
            index = index + 2;
        }

        /* Invert matrix, adjust for scroll, project */
        A.invert(A_inverse);
        Log.d("Scroll", WUtils.toString(scroll));
        A_inverse.postTranslate(scroll.x, scroll.y);
        A_inverse.mapPoints(toTransform);

        /* Post Processing */

        ArrayList<Point> transformed = new ArrayList<>();
        for(index = 0; index < 2*Points.size(); index+=2){
            Point p = new Point();
            p.set((int)toTransform[index], (int)toTransform[index+1]);
            transformed.add(p);
        }

        return transformed;
    }

}
