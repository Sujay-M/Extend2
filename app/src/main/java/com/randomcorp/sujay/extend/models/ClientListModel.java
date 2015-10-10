package com.randomcorp.sujay.extend.models;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;

import com.randomcorp.sujay.extend.Utils.RoundedCharacterDrawable;

import java.net.InetAddress;
import java.util.Random;

/**
 * Created by sujay on 13/9/15.
 */
public class ClientListModel
{
    public RoundedCharacterDrawable drawable;
    public  String deviceName,modelName;
    public boolean isSelected;
    public InetAddress ipAddress;
    public ClientListModel(String deviceName,String modelName,InetAddress ipAddress)
    {
        this.deviceName = deviceName;
        this.modelName = modelName;
        this.ipAddress = ipAddress;
        this.drawable = new RoundedCharacterDrawable(deviceName.charAt(0), getColor());
        isSelected = false;

    }
    private int getColor()
    {
        String[] colors = {"#F44336","#E91E63","#9C27B0","#673AB7","#3F51B5","#2196F3",
                            "#03A9F4","#00BCD4","#009688","#4CAF50","#8BC34A","#CDDC39",
                            "#FFEB3B","#FFC107","#FF9800","#FF5722","#795548","#9E9E9E",
                            "#607D8B","#303F9F"};
        Random r = new Random(SystemClock.elapsedRealtime());
        return Color.parseColor(colors[r.nextInt(21)]);

    }

}
