package com.randomcorp.sujay.extend.Utils;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import com.randomcorp.sujay.extend.R;
import com.randomcorp.sujay.extend.models.ClientListModel;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by sujay on 13/9/15.
 */
public class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.ViewHolder> {
    private ArrayList<ClientListModel> mDataset;

    public ClientListAdapter()
    {
        mDataset = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.client_list_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        ClientListModel info = mDataset.get(position);
        holder.deviceName.setText(info.deviceName);
        holder.modelName.setText(info.modelName);
        holder.isSelected.setChecked(info.isSelected);
        holder.avatar.setImageDrawable(info.drawable);
        holder.isSelected.setTag(info);
        holder.isSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CheckBox cb = (CheckBox)v;
                ClientListModel clicked = (ClientListModel)cb.getTag();
                Log.d("Clicked","device name = "+clicked.deviceName );
                clicked.isSelected = cb.isChecked();
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView avatar;
        public TextView deviceName,modelName;
        public CheckBox isSelected;
        public ViewHolder(View v) {
            super(v);
            avatar = (ImageView)v.findViewById(R.id.iv_avatar);
            deviceName = (TextView)v.findViewById(R.id.tv_deviceName);
            modelName = (TextView)v.findViewById(R.id.tv_model_name);
            isSelected = (CheckBox)v.findViewById(R.id.cb_selected);
        }
    }

    public void add(String deviceName,String modelName,InetAddress address)
    {
        int position = mDataset.size();
        mDataset.add(position, new ClientListModel(deviceName,modelName,address));
        notifyItemInserted(position);
    }

    public ArrayList<InetAddress> getSelectedAddresses()
    {
        ArrayList<InetAddress> addresses = new ArrayList<>();

        for (ClientListModel info:mDataset)
        {
            if(info.isSelected)
                addresses.add(info.ipAddress);
        }
        return addresses;
    }

    public void clearList()
    {
        mDataset.clear();
        notifyDataSetChanged();
    }
}
