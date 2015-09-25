package com.randomcorp.sujay.extend.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.randomcorp.sujay.extend.imageProcessing.NewLayout;
import com.randomcorp.sujay.extend.R;
import com.randomcorp.sujay.extend.networking.ExtendProtocol;
import com.randomcorp.sujay.extend.networking.ServerExtendProtocol;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sujay on 21/9/15.
 */
public class LayoutConfigurationActivity extends AppCompatActivity implements View.OnClickListener, NewLayout.UpdateView, View.OnTouchListener,ExtendProtocol {
    private static final String TAG = "Layout Configuration";

    private static final byte REQUEST_TAKE_PHOTO = 1;
    private ImageView mImageView;
    private Button bNext;
    private ServerExtendProtocol server;
    private Bitmap captured;
    private boolean imageAvailable;
    private NewLayout processor;
    private int devNo;
    private ProgressDialog dialog;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    if(imageAvailable && captured!=null)
                    {
                        processor.setORIGINAL(captured);
                        devNo = 0;
                    }
                    Log.i(TAG, "OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_configuration_activity);
        mImageView = (ImageView)findViewById(R.id.iv_layout);
        mImageView.setOnTouchListener(this);
        bNext = (Button)findViewById(R.id.b_next);
        bNext.setOnClickListener(this);
        bNext.setVisibility(View.INVISIBLE);
        server = ServerExtendProtocol.getSingleton();
        dispatchTakePictureIntent();
        imageAvailable = false;
        processor = new NewLayout(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug())
        {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        }
        else
        {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void dispatchTakePictureIntent()
    {
        server.sendCommandToAll(commandWhite,false);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex)
            {

            }
            if (photoFile != null)
            {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                takePictureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException
    {

        String imageFileName = "temp.png";
        File storageDir = Environment.getExternalStorageDirectory();
        File image = new File(storageDir,imageFileName);
        return image;
    }

    private void setImage(Bitmap bmp)
    {
        mImageView.setImageBitmap(bmp);
    }

    private void setPic()
    {
        try
        {
            int targetW = mImageView.getWidth();
            int targetH = mImageView.getHeight();
            String path = Environment.getExternalStorageDirectory()+"/temp.png";
            File f = new File(path);
            if(f.exists())
            {
                Log.d(TAG,"File exists");
                Toast.makeText(this,"file found",Toast.LENGTH_SHORT).show();
                InputStream is = new FileInputStream(f);
                Bitmap bmp = BitmapFactory.decodeStream(is);

                int width = bmp.getWidth();
                int height = bmp.getHeight();
                Log.d(TAG,"bmp.width = "+width+"  bmp.height = "+height);
                float xScale = ((float) targetW) / width;
                float yScale = ((float) targetH) / height;
                float scale = (xScale <= yScale) ? xScale : yScale;

                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);

                captured = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
                imageAvailable = true;
                setImage(captured);

            }
            else
            {
                Log.d(TAG,"image not found");
            }

        }
        catch (FileNotFoundException e)
        {
            Log.d(TAG, "image not found");
            Toast.makeText(this,"file not found",Toast.LENGTH_SHORT).show();

        }
        catch (IllegalArgumentException e)
        {
            Log.d(TAG, "width and heights are 0");
            Toast.makeText(this,"width and heights are 0",Toast.LENGTH_SHORT).show();
        }
        catch(ArithmeticException e)
        {
            Log.d(TAG, "Divide by 0");
            Toast.makeText(this,"divide by 0",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            setPic();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.layout_configuration_menus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_capture:
                dispatchTakePictureIntent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId()==R.id.b_next)
        {
            if(devNo<server.noOfClients())
            {
                processor.assignBox(devNo);
                devNo+=1;
                if(devNo<server.noOfClients())
                {
                    server.sendCommandMessage(devNo-1,commandWhite,false);
                    server.sendCommandMessage(devNo,commandRed,false);
                    bNext.setVisibility(View.INVISIBLE);
                }
                else
                {
                    server.sendCommandMessage(devNo-1,commandWhite,false);
                    dialog = new ProgressDialog(LayoutConfigurationActivity.this);
                    dialog.setMessage("Processing....");
                    dialog.show();
                    processor.createLayout();
                    //createlayout and startactivity
                }
            }


        }
    }

    @Override
    public void setBitmapImage(Bitmap image,boolean screenDetection)
    {
        setImage(image);
        if(screenDetection)
            server.sendCommandMessage(devNo,commandRed,false);
    }

    @Override
    public void layoutCreated(boolean value)
    {
        if(dialog != null && dialog.isShowing())
        {
            dialog.dismiss();
        }
        Intent i = new Intent(this,ControllerActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if(imageAvailable && devNo < server.noOfClients())
        {

            int cols = captured.getWidth();
            int rows = captured.getHeight();

            int xOffset = (mImageView.getWidth() - cols) / 2;
            int yOffset = (mImageView.getHeight() - rows) / 2;

            Log.d(TAG,"coordinates = ("+event.getX()+","+event.getY()+")");

            Log.d(TAG,"Offsets x = "+xOffset+" y = "+yOffset);

            int x = (int)event.getX() - xOffset;
            int y = (int)event.getY() - yOffset;

            if(processor.touchEvent(x,y,devNo))
                bNext.setVisibility(View.VISIBLE);

        }
        return false;
    }
}
