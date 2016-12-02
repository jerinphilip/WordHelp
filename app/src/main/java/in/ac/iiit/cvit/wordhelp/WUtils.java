package in.ac.iiit.cvit.wordhelp;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;


import java.util.ArrayList;

/**
 * Created by jerin on 27/10/16.
 */

class WUtils {
    public static String toString(Point P){
        return "("+String.valueOf(P.x) + "," + String.valueOf(P.y) + ")";
    }
    public static String toString(PointF P){
        return "("+String.valueOf(P.x) + "," + String.valueOf(P.y) + ")";
    }

    public static String toString(org.opencv.core.Rect r){
        return "("+String.valueOf(r.x)+","+String.valueOf(r.y)+","+String.valueOf(r.width)
                +", "+String.valueOf(r.height);
    }


    public static void debugPoints(ArrayList<Point> Points){
        for(Point p: Points){
            Log.d("DebugAlist<P>", toString(p));
        }
    }

    public static RectF boundingBox(ArrayList<PointF> path, int W, int H){
        /* Detects bounding box from path so operations need to be done only there. */
        if(path.size() > 2) {
            float min_x, max_x, min_y, max_y;

        /* Initialize with values. Path should contain at least two points. */

            min_x = path.get(0).x;
            min_y = path.get(0).y;

            max_x = min_x;
            max_y = min_y;
            for (PointF p : path) {
                min_x = Math.min(min_x, p.x);
                max_x = Math.max(max_x, p.x);

                min_y = Math.min(min_y, p.y);
                max_y = Math.max(max_y, p.y);
            }

            int tolerance = 0;
            min_x = Math.max(0, min_x - tolerance);
            min_y = Math.max(0, min_y - tolerance);

            max_x = Math.min(W, max_x + tolerance);
            max_y = Math.min(H, max_y + tolerance);


            return new RectF(min_x, min_y, max_x, max_y);
        }
        else{
            return new
                    RectF(0, 0, W, H);
        }
    }


}
