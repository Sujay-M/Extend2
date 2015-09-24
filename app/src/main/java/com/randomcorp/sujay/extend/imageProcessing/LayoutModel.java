package com.randomcorp.sujay.extend.imageProcessing;

import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sujay on 29/8/15.
 */
public class LayoutModel
{
    private List<Integer[]> layout;
    private HashMap<Integer,Integer[]> clientRects;
    static LayoutModel LAYOUT = null;
    private LayoutModel()
    {
        layout = new ArrayList<Integer[]>();
        clientRects = new HashMap<>();
    }
    public static LayoutModel getSingleton()
    {
        if(LAYOUT==null)
            LAYOUT = new LayoutModel();
        return LAYOUT;
    }
    public void setLayout(List<Integer[]> layout)
    {
        this.layout.clear();
        this.layout.addAll(layout);
    }
    public List<Integer[]> getLayout()
    {
        if(layout.size()==0)
            return null;
        return layout;
    }

    public void setClientRect(int devNo,Integer[] box)
    {
        clientRects.put(devNo,box);
    }

    public Integer[] getClientRect(int devNo)
    {
        if(clientRects.containsKey(devNo))
            return clientRects.get(devNo);
        return null;
    }
}
