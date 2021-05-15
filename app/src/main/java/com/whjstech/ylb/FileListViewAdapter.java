package com.whjstech.ylb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.whjstech.ylb.R;

import java.util.List;

public class FileListViewAdapter extends ArrayAdapter<MyFile> {
    private int resourceId;
    public FileListViewAdapter(Context context, int resource, List<MyFile> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyFile myFile = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView ==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.fileName = (TextView) view.findViewById(R.id.file_name);
            viewHolder.fileSize = (TextView) view.findViewById(R.id.file_size);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.fileName.setText(myFile.getFileName());
        viewHolder.fileSize.setText(myFile.getFileSize());
        return view;
    }
    class ViewHolder{
        TextView fileName;
        TextView fileSize;
    }

}
