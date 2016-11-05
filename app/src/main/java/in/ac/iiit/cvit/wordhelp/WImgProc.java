package in.ac.iiit.cvit.wordhelp;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by jerin on 27/10/16.
 */

public class WImgProc {
    public static Bitmap process(Bitmap bmp, ArrayList<Point> swipePath){
        Mat img = new Mat();
        Utils.bitmapToMat(bmp, img);
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);

        img = cropWord(img, swipePath);

        bmp = Bitmap.createBitmap(bmp, 0, 0, img.cols(), img.rows());
        Utils.matToBitmap(img, bmp);
        Log.d("Image Dimensions", WUtils.toString(new Point(img.cols(), img.rows())));
        return bmp;
    }

    public static Mat cropWord(Mat img, ArrayList<Point> path){
        Rect cropWindow = boundingBox(path, img.cols(), img.rows());
        Log.d("CropWindow", WUtils.toString(cropWindow));
        img = new Mat(img, cropWindow);
        Imgproc.threshold(img, img, 127, 255, Imgproc.THRESH_OTSU);
        Imgproc.erode(img, img, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)));


        // Adjust points relative to cropped window.
        for(int i=0; i<path.size(); i++){
            Point p = path.get(i);
            path.set(i, new Point(p.x - cropWindow.x, p.y-cropWindow.y));

        }
        /*
        // Do a connected component analysis nearby.
        img = cropSubRoutine(img, path);
        */
        return img;


    }

    public static Rect boundingBox(ArrayList<Point> path, int W, int H){
        /* Detects bounding box from path so operations need to be done only there. */
        int min_x, max_x, min_y, max_y;

        /* Initialize with values. Path should contain at least two points. */

        min_x = path.get(0).x;
        min_y = path.get(0).y;

        max_x = min_x;
        max_y = min_y;

        for(Point p: path){
            min_x = Math.min(min_x, p.x);
            max_x = Math.max(max_x, p.x);

            min_y = Math.min(min_y, p.y);
            max_y = Math.max(max_y, p.y);
        }

        int tolerance = 10;
        min_x  = Math.max(0, min_x-tolerance);
        min_y  = Math.max(0, min_y-tolerance);

        max_x  = Math.min(W, max_x+tolerance);
        max_y  = Math.min(H, max_y+tolerance);


        return new Rect(min_x, min_y, max_x-min_x+1, max_y-min_y+1);
    }

    public static ArrayList<Point> neighbours(Point p, int W, int H){
        ArrayList<Point> result = new ArrayList<>();
        int x, y;
        for(int dx=-1; dx<=1; dx++){
            for(int dy=-1; dy<=1; dy++){
                if(dx!=0 && dy!=0){
                    x = p.x+dx;
                    y = p.y+dy;
                    if ( x >= 0 && x < W && y >= 0 && y < H){
                        result.add(new Point(x, y));
                    }
                }
            }
        }
        return result;
    }

    public static Mat cropSubRoutine(Mat img, ArrayList<Point> path){
        int H, W;
        W = img.cols();
        H = img.rows();

        /* Width is no of columns */
        /* Height is number of rows */


        Mat result_img = new Mat(img.size(), img.type());
        int[][] color = new int[H][W];
        int[][] bImage = roundValues(img);
        int UNTOUCHED = 0, MARKED = 1, VISITED = 2;


        /* Set all to unmarked. */
        for(int i=0; i<H; i++){
            for(int j=0; j<W; j++){
                color[i][j] = UNTOUCHED;
            }
        }


        /* Prepare for DFS */
        Stack<Point> S;
        S = new Stack<Point>();
        for (Point touched: path){
            int i, j;
            i = touched.y;
            j = touched.x;
            if(bImage[i][j] == 0 &&
                    color[i][j] == UNTOUCHED){
                S.push(touched);
                color[i][j] = MARKED;
            }
            while(!S.empty()){
                Point root = S.pop();
                i = root.y;
                j = root.x;
                //Log.d("Setting Visited", WUtils.toString(new Point(i, j)));

                color[i][j] = VISITED;
                double[] value = {255.0};
                result_img.put(i, j, value);

                for(Point p: neighbours(root, W, H)){
                    if(bImage[p.y][p.x] == 0 && color[p.y][p.x] == UNTOUCHED){
                        S.push(p);
                        color[p.y][p.x] = MARKED;
                    }
                }
            }
        }

        return result_img;
    }

    public static int[][] roundValues(Mat img){
        int[][] I = new int[img.rows()][img.cols()];
        //Log.d("Dim:", WUtils.toString(new Point(img.cols(), img.rows())));
        for(int i=0; i<img.rows(); i++){
            for(int j=0; j<img.cols(); j++){
                //Log.d("Dim:", WUtils.toString(new Point(i, j)));
                I[i][j] = (int)img.get(i, j)[0];
                //Log.d("Setting: ", WUtils.toString(new Point(i, j)));
                //
                // Log.d("Value", String.valueOf(I[i][j]));
            }
        }
        return I;
    }


}
