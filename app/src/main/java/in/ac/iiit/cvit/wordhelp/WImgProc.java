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
        Imgproc.threshold(img, img, 127, 255, Imgproc.THRESH_OTSU);
        Imgproc.erode(img, img, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)));

        // Downsample.
        // Processing is done on an image of size 640x640 tops.
        double MAX_SIZE = 640;
        double wScale = MAX_SIZE/(double)img.width();
        double hScale = MAX_SIZE/(double)img.height();

        double scale = Math.min(wScale, hScale);
        Log.d("Scale", String.valueOf(scale));
        Imgproc.resize(img, img, new Size(), scale, scale, Imgproc.INTER_CUBIC);

        if (swipePath.size() == 1){
            ArrayList<Point> P = neighbours(swipePath.get(0), img.rows(), img.cols());
            for(Point p: P){
                swipePath.add(p);
            }

        }

        ArrayList<Point> scaledSwipePath = new ArrayList<>();
        for(Point p: swipePath){
            p.x = (int)(p.x*scale);
            p.y = (int)(p.y*scale);
            scaledSwipePath.add(p);
        }

        if (scaledSwipePath.size() == 1){
            ArrayList<Point> P = neighbours(scaledSwipePath.get(0), img.rows(), img.cols());
            for(Point p: P){
                scaledSwipePath.add(p);
            }

        }

        Rect bbox = cropOptimized(img, scaledSwipePath);

        // Upsample Scale obtained bounding box.
        bbox.x = (int)(bbox.x/scale);
        bbox.y = (int)(bbox.y/scale);
        bbox.width = (int)(bbox.width/scale);
        bbox.height = (int)(bbox.height/scale);

        Mat original = new Mat();
        Utils.bitmapToMat(bmp, original);
        img = new Mat(original, bbox);
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(img, img, 127, 255, Imgproc.THRESH_OTSU);
        bmp = Bitmap.createBitmap(bmp, 0, 0, img.cols(), img.rows());
        Utils.matToBitmap(img, bmp);
        Log.d("Image Dimensions", WUtils.toString(new Point(img.cols(), img.rows())));
        return bmp;
    }



    public static ArrayList<Point> neighbours(Point p, int W, int H){
        ArrayList<Point> result = new ArrayList<>();
        int x, y;
        for(int dx=-1; dx<=1; dx++){
            for(int dy=-1; dy<=1; dy++){
                if(!(dx==0 && dy==0)){
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


    public static Rect cropOptimized(Mat img, ArrayList<Point> path){
        int H, W;
        H = img.rows();
        W = img.cols();

        int[][] color = new int[H][W];
        int UNTOUCHED = 0, MARKED = 1, VISITED = 2;

        int min_i, max_i, min_j, max_j;
        min_i = H; min_j = W;
        max_i = 0; max_j = 0;

        ArrayList<Point> sparseImage = new ArrayList<>();

        Stack<Point> S;
        S = new Stack<Point>();
        for(Point p: path){
            int i, j;
            i = p.y;
            j = p.x;
            Log.d("Point", WUtils.toString(p));
            Log.d("Bounds: (W, H):", WUtils.toString(new Point(W, H)));

            Log.d("Accessing", WUtils.toString(new Point(i, j)));
            double[] value = img.get(i, j);
            if((int)value[0] == 0 && color[i][j] == UNTOUCHED){
                S.push(p);
                color[i][j] = MARKED;
            }

            while (!S.empty()){
                Point root = S.pop();
                i = root.y;
                j = root.x;
                sparseImage.add(root);
                color[i][j] = VISITED;
                min_i = Math.min(min_i, i);
                max_i = Math.max(max_i, i);

                min_j = Math.min(min_j, j);
                max_j = Math.max(max_j, j);

                for(Point child: neighbours(root, W, H)){
                    int ii, jj;
                    ii = child.y; jj = child.x;
                    double[] childValue = img.get(ii, jj);
                    if((int)childValue[0] == 0 && color[ii][jj] == UNTOUCHED){
                        S.push(child);
                        color[ii][jj] = MARKED;
                    }
                }
            }
        }

        Rect bbox = new Rect(min_j, min_i, max_j - min_j + 1, max_i - min_i + 1);
        Mat result = new Mat(img.size(), img.type());
        result = new Mat(result, bbox);
        double[] white = {255.0};
        for(Point p: sparseImage){
            result.put(p.y-min_i, p.x-min_j, white);
        }

        return bbox;
    }
}
