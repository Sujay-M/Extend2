package com.randomcorp.sujay.extend.models;

import android.graphics.Bitmap;

import java.net.InetAddress;

/**
 * Created by sujay on 13/9/15.
 */
public class ClientListModel
{
    public Bitmap image;
    public  String deviceName;
    public boolean isSelected;
    public InetAddress ipAddress;
    public ClientListModel(Bitmap image,String deviceName,InetAddress ipAddress)
    {
        this.image = image;
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        isSelected = false;

    }

}
