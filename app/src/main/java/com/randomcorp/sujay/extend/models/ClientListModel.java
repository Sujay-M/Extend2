package com.randomcorp.sujay.extend.models;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.randomcorp.sujay.extend.Utils.RoundedCharacterDrawable;

import java.net.InetAddress;

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
        this.drawable = new RoundedCharacterDrawable(deviceName.charAt(0), getColor(deviceName.charAt(0)));
        isSelected = false;

    }
    private int getColor(char ch)
    {
        return Color.BLUE;
    }

}
