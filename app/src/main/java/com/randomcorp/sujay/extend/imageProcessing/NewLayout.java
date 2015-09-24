package com.randomcorp.sujay.extend.imageProcessing;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sujay on 21/9/15.
 */
public class NewLayout
{

    public interface UpdateView
    {
        void setBitmapImage(Bitmap image,boolean screenDetection);
        void layoutCreated(boolean value);
    }

    private Mat ORIGINAL = null;
    private Mat mask = null;
    private UpdateView callback;
    private List<RotatedRect> boundingBoxes = new ArrayList<>();
    private final int SENSITIVITY = 50;
    private final Scalar minThresh = new Scalar(0,255-SENSITIVITY,0);
    private final Scalar maxThresh = new Scalar(255,255,255);
    private Handler handler;
    private boolean screensFound;

    public NewLayout(final UpdateView callback)
    {
        this.callback = callback;
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what)
                {
                    case 1:
                        for (RotatedRect r:boundingBoxes)
                        {
                            Rect rect = r.boundingRect();
                            Core.rectangle(ORIGINAL, rect.tl(), rect.br(), new Scalar(255, 0, 0), 2);
                        }
                        Bitmap bm = Bitmap.createBitmap(ORIGINAL.cols(), ORIGINAL.rows(),Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(ORIGINAL, bm);
                        screensFound = true;
                        callback.setBitmapImage(bm,true);
                        break;
                    case 2:
                        callback.layoutCreated(true);
                        break;
                }
                return false;
            }
        });
        screensFound = false;
    }

    public void setORIGINAL(Bitmap ORIGINAL)
    {
        this.ORIGINAL = new Mat();
        Utils.bitmapToMat(ORIGINAL,this.ORIGINAL);
        mask = new Mat(this.ORIGINAL.size(), CvType.CV_8U);
        mask.setTo(new Scalar(0));
        startScreenDtection();
    }

    private void startScreenDtection()
    {
        Thread t = new Thread(new ScreenDetectionRunnable());
        t.start();
    }

    class ScreenDetectionRunnable implements Runnable
    {
        @Override
        public void run()
        {
            Mat result = new Mat();
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Imgproc.cvtColor(ORIGINAL, result, Imgproc.COLOR_RGB2HLS);
            Core.inRange(result, minThresh, maxThresh, result);
            Imgproc.findContours(result,contours,new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            boundingBoxes.clear();
            for (int i=0; i<contours.size(); i++)
            {
                MatOfPoint current = contours.get(i);
                if(Imgproc.contourArea(current)<1000)
                    continue;
                MatOfPoint2f contour2f = new MatOfPoint2f( current.toArray() );
                double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                RotatedRect rrect = Imgproc.minAreaRect(approxCurve);
                Rect rect = rrect.boundingRect();
                if((Imgproc.contourArea(current))<(rect.area()-1000))
                    if(((float)rect.width/(float)rect.height)>1.7)
                        continue;
                if((float)rect.width/(float)rect.height<.5)
                    continue;

                Point[] vertices = new Point[4];
                rrect.points(vertices);
                boundingBoxes.add(rrect);
            }
            Message msg = handler.obtainMessage();
            msg.what = 1;
            handler.sendMessage(msg);
        }
    }

    public boolean touchEvent(int x,int y,int devNo)
    {
        Point p = new Point(x,y);
        for (RotatedRect cur:boundingBoxes)
        {
            if(cur.boundingRect().contains(p))
            {
                testAssignBox(cur.boundingRect(),devNo);
                return true;
            }
        }
        return false;
    }

    private void testAssignBox(Rect box,int devno)
    {
        LayoutModel model = LayoutModel.getSingleton();
        Integer[] rect = new Integer[4];
        rect[0] = box.x;
        rect[1] = box.y;
        rect[2] = box.width;
        rect[3] = box.height;
        model.setClientRect(devno,rect);
        Mat result = ORIGINAL.clone();
        Core.rectangle(result, box.tl(), box.br(), new Scalar(255, 255, 0), -1);
        Bitmap bm = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, bm);
        callback.setBitmapImage(bm,false);
    }

    public void assignBox(int devNo)
    {
        LayoutModel model = LayoutModel.getSingleton();
        Mat result = ORIGINAL.clone();
        Integer[] rect = model.getClientRect(devNo);
        Rect box = new Rect(rect[0],rect[1],rect[2],rect[3]);
        Core.rectangle(result, box.tl(), box.br(), new Scalar(0, 255,0), -1);
        Core.rectangle(mask, box.tl(), box.br(), new Scalar(255), -1);
        Bitmap bm = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, bm);
        callback.setBitmapImage(bm,false);
    }

    public void createLayout()
    {
        Log.d("ImgProc",""+mask.type());
        Thread t = new Thread(new Runnable() {
            @Override
            public void run()
            {
                Log.d("ImgProc","thread started");
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                int i = 30,j,k;
                do
                {
                    contours.clear();
                    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(i, i));
                    Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel);
                    Imgproc.findContours(mask.clone(),contours,new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
                    Log.d("ImgProc","i = "+i+" no of contours = "+contours.size());
                    i+=20;
                }while(contours.size()!=1);
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(0).toArray() );
                double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
                MatOfPoint points = new MatOfPoint( approxCurve.toArray() );
                Rect layout = Imgproc.boundingRect(points);
                int top = layout.y;
                int left = layout.x;
                int bottom = top+layout.height;
                int right = left+layout.width;
                double data[],lpixel[],tpixel[],ltpixel[];
                List<Integer[]> set = new ArrayList<>();

                for(i=top;i<=bottom;i++)
                {
                    for(j=left;j<=right;j++)
                    {
                        data = mask.get(i,j);
                        lpixel = (j!=0)?mask.get(i,j-1):new double[]{0};
                        tpixel = (i!=0)?mask.get(i-1,j):new double[]{0};
                        ltpixel = (i!=0)&&(j!=0)?mask.get(i-1,j-1):new double[]{0};
                        if(data[0]!=0 && tpixel[0]==0 && lpixel[0]==0)
                        {
                            for(k = j+1;k<=right;k++)
                            {
                                data = mask.get(i,k);
                                if(data[0]==0)
                                    break;
                            }
                            set.add(new Integer[]{j,i,k-1,-1});
                        }
                        else if(data[0]!=0 && tpixel[0]==0 && ltpixel[0]!=0)
                        {
                            List<Integer[]> temp = new ArrayList<Integer[]>();
                            for(Integer[] rect:set)
                            {
                                if(rect[2]==j-1 && rect[3]==-1)
                                {
                                    for(k = j;k<=right;k++)
                                    {
                                        data = mask.get(i,k);
                                        if(data[0]==0)
                                            break;
                                    }
                                    if(rect[0]==141 && i==101 && (k-1)==572)
                                        Log.d("found1","rect = "+rect[0]+" "+rect[1]+" "+rect[2]+" "+rect[3]);
                                    temp.add(new Integer[]{rect[0],i,k-1,-1});
                                }

                            }
                            if(temp.size()!=0)
                                set.addAll(temp);
                        }
                        else if(data[0]!=0 && lpixel[0]==0)
                        {
                            List<Integer[]> temp = new ArrayList<Integer[]>();
                            for(Integer[] rect:set)
                            {
                                if(rect[3]==i-1 && j>rect[0] && j<rect[2])
                                {
                                    for(k = j;k<=right;k++)
                                    {
                                        data = mask.get(i,k);
                                        if(data[0]==0)
                                            break;
                                    }
                                    if(j==141 && rect[1]==101 && min(k-1,rect[2])==572)
                                        Log.d("found2","rect = "+rect[0]+" "+rect[1]+" "+rect[2]+" "+rect[3]);
                                    temp.add(new Integer[]{j,rect[1],min(k-1,rect[2]),-1});
                                }

                            }
                            if(temp.size()!=0)
                                set.addAll(temp);
                        }
                        else if(data[0]==0 && lpixel[0]!=0 && tpixel[0]!=0)
                        {
                            List<Integer[]> temp = new ArrayList<Integer[]>();
                            for(Integer[] rect:set)
                            {
                                if(rect[3]==-1)
                                {
                                    if(rect[0]==141 && rect[1]==101 && j==572)
                                        Log.d("found3","rect = "+rect[0]+" "+rect[1]+" "+rect[2]+" "+rect[3]);
                                    temp.add(new Integer[]{rect[0],rect[1],min(j,rect[2]),-1});
                                }
                            }
                            if(temp.size()!=0)
                                set.addAll(temp);
                        }

                        data = mask.get(i,j);
                        if(data[0]==0)
                        {
                            for(Integer[] rect:set)
                            {
                                if((rect[3]==-1 && i>rect[1] && j>rect[0] && j<rect[2]) || (i==bottom&&rect[3]==-1))
                                    rect[3] = i;
                            }
                        }

                    }
                }
                LayoutModel layoutModel = LayoutModel.getSingleton();
                layoutModel.setLayout(set);

                Message msg = handler.obtainMessage();
                msg.what = 2;
                handler.sendMessage(msg);
            }
        });
        t.start();
    }
    private int min(int a,int b)
    {
        return (a>=b)?b:a;
    }

}
