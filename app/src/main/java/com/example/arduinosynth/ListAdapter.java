package com.example.arduinosynth;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Context context;
    private List<Object> deviceList;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textName, textAddress;
        LinearLayout linearLayout;

        public ViewHolder(View v)
        {
            super(v);
            textName = v.findViewById(R.id.textViewDeviceName);
            textAddress = v.findViewById(R.id.textViewDeviceAddress);
            linearLayout = v.findViewById(R.id.linearLayoutDeviceInfo);
        }
    }

    public ListAdapter(Context context, List<Object> deviceList)
    {
        this.context = context;
        this.deviceList = deviceList;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.info, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position)
    {
        ViewHolder itemHolder = (ViewHolder) holder;
        final Info info = (Info) deviceList.get(position);
        itemHolder.textName.setText(info.getDeviceName());
        itemHolder.textAddress.setText(info.getDeviceHardwareAddress());

        itemHolder.linearLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(context,MainActivity.class);
                intent.putExtra("deviceName", info.getDeviceName());
                intent.putExtra("deviceAddress",info.getDeviceHardwareAddress());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return deviceList.size();
    }
}