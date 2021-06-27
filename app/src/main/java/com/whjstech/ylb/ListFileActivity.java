package com.whjstech.ylb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListFileActivity extends AppCompatActivity {
    private String rootPath;
    private TextView pathTextView;
    private ListView fileListView;
    private List<MyFile> myFileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_file);

        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/压力数据";
        pathTextView = (TextView)findViewById(R.id.tv_path);
        fileListView = (ListView)findViewById(R.id.lv_file);
        getFileDir(rootPath);
        final FileListViewAdapter adapter = new FileListViewAdapter(ListFileActivity.this,R.layout.item_file_list,myFileList);
        fileListView.setAdapter(adapter);
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = myFileList.get(position).getFileName();
                String path = myFileList.get(position).getFilePath();
                long size = myFileList.get(position).getSize();
                File file = new File(path);

                if(file.isDirectory()){
                    getFileDir(path);
                    adapter.notifyDataSetChanged();
                }else {
                    Intent intent = new Intent(ListFileActivity.this, DisplayDataActivity.class);
                    intent.putExtra("name",name);
                    intent.putExtra("path",path);
                    intent.putExtra("size",size);
                    startActivity(intent);
                }
            }
        });
    }

    public void getFileDir(String filePath){
        myFileList.clear();
        try{
            pathTextView.setText("PATH: "+filePath);
            File f = new File(filePath);
            File[] files = f.listFiles();

            if(!filePath.equals(rootPath)){
                myFileList.add(new MyFile(".返回上一层目录",rootPath,0));
            }

            if(files != null){
                int count = files.length;
                for(int i = 0; i < count; i++){
                    File file = files[i];
                    long size = file.isFile()?file.length():0;
                    myFileList.add(new MyFile(file.getName(),file.getPath(),size));
                }
                //Collections.sort(items,new mComparator());
                Collections.sort(myFileList,new mComparator());

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class mComparator implements Comparator<MyFile> {
        @Override
        public int compare(MyFile o1, MyFile o2) {
            return o1.getFileName().compareTo(o2.getFileName());
        }
    }
}
