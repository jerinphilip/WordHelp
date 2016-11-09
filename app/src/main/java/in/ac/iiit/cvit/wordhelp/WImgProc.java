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


        if (swipePath.size() == 1){
            ArrayList<Point> P = neighbours(swipePath.get(0), img.rows(), img.cols());
            for(Point p: P){
                swipePath.add(p);
            }

        }
        img = cropOptimized(img, swipePath);


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


    public static Mat cropOptimized(Mat img, ArrayList<Point> path){
        int H, W;
        H = img.cols();
        W = img.rows();

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

        return result;
    }
}
