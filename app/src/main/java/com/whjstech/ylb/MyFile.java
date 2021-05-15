package com.whjstech.ylb;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;



public class MyFile {
    private String fileName;
    private String filePath;
    private long fileSize;
    private FileContent fileContent;


    private byte[] bytes;
    String TAG = "MyFile";


    public MyFile(String name, String path, long size){
        this.fileName = name;
        this.filePath = path;
        this.fileSize = size;
        //resolveContent();
    }
    public String getFileName() {
        return fileName;
    }
    public String getFilePath(){
        return filePath;
    }

    public long getSize(){
        return fileSize;
    }

    public String getFileSize() {
        if(fileSize == 0){
            return "";
        }
        if (fileSize<1024){
            return String.valueOf(fileSize)+"B";
        }
        return String.valueOf(fileSize/1024)+"KB";
    }

    public FileContent getFileContent(){
        resolveContent();
        return fileContent;
    }

    private void resolveContent(){
        if (fileSize == 0){
            return;
        }
        fileContent = new FileContent();
        read2Bytes(filePath);
        getFirstBlockInfo(bytes);
        getSecondBlockInfo(bytes);
        getThirdBlockInfo(bytes);
        getRecordPoints(bytes,fileContent.pointsNumber);
    }

    private void read2Bytes(String path){
        try{
            File file = new File(path);
            long length = file.length();
            bytes = new byte[(int)length];

            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            int r = bufferedInputStream.read(bytes);
            if(r != length){
                throw new IOException("读取文件不正常");
            }
            bufferedInputStream.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void getFirstBlockInfo(final byte[] bytes){
        byte[] temp = new byte[6];
        //获取文件类型
        System.arraycopy(bytes,0,temp,0,6);
        fileContent.fileClass = new String(temp);
        Log.d(TAG, "getFirstBlockInfo: "+fileContent.fileClass);
        //获取生成文件的时刻
        System.arraycopy(bytes,8,temp,0,6);
        fileContent.createFileTime = bytes2Date(temp);
        Log.d(TAG, "getFirstBlockInfo: "+fileContent.createFileTime);
        //获取表号
        fileContent.idNumber = bytes2Int(bytes,14,2);
        //获取记录间隔
        fileContent.interval = bytes2Int(bytes,16,2);
        //获取通道数
        fileContent.channel = bytes2Int(bytes,18,1);
        //记录起始时刻
        System.arraycopy(bytes,19,temp,0,6);
        fileContent.startRecordTime = bytes2Date(temp);
        //获取记录数据个数
        fileContent.pointsNumber = bytes2Int(bytes,25,4);
    }

    private void getSecondBlockInfo(final byte[] bytes){
        fileContent.sampleValue = new int[6];
        fileContent.setValue = new int[6];
        for(int i = 0; i < 6; i++){
            fileContent.sampleValue[i] = bytes2Int(bytes,32+4*i,2);
            fileContent.setValue[i] = bytes2Int(bytes,34+4*i,2);
        }
        //获取小数点位数
        fileContent.dotPositon= bytes2Int(bytes,56,1);
        //获取压力上限值
        fileContent.upperLimitValue = bytes2Int(bytes,57,2);
    }

    private void getThirdBlockInfo(final byte[] bytes){
        byte[] temp = new byte[6];
        //获取电压
        fileContent.voltage = bytes2Int(bytes,64,2);
        Log.d(TAG, "getThirdBlockInfo: "+fileContent.voltage);
        //获取版本号
        System.arraycopy(bytes,66,temp,0,6);
        fileContent.hsVersion = new String(temp);
        Log.d(TAG, "getThirdBlockInfo: "+fileContent.hsVersion);
    }

    public void getRecordPoints(final byte[] bytes,int number){
        if (number <= 0){
            return;
        }
        fileContent.recordPoints = new int[number];
        fileContent.recordPoints[0] = bytes2Int(bytes,128,2);
        fileContent.minValue = fileContent.maxValue = fileContent.recordPoints[0];
        for (int i = 1; i<number; i++){
            fileContent.recordPoints[i] = bytes2Int(bytes,128+2*i,2);

            if (fileContent.recordPoints[i] > fileContent.maxValue){
                fileContent.maxValueIndex = i;
                fileContent.maxValue = fileContent.recordPoints[i];
            }
            if (fileContent.recordPoints[i] < fileContent.minValue){
                fileContent.minValueIndex = i;
                fileContent.minValue = fileContent.recordPoints[i];
            }

        }

        long max = fileContent.startRecordTime.getTime()+fileContent.interval*fileContent.maxValueIndex*1000;
        long min = fileContent.startRecordTime.getTime()+fileContent.interval*fileContent.minValueIndex*1000;
        fileContent.maxValueTime = new Date(max);
        fileContent.minValueTime = new Date(min);

    }

    private Date bytes2Date(byte[] bytes){
        return new Date((int)bytes[0]+100,(int)bytes[1]-1,(int)bytes[2],
                (int)bytes[3],(int)bytes[4],(int)bytes[5]);
    }

    private int bytes2Int(byte[] bytes,int index, int n){
        int result = 0;
        for(int i = 0; i < n; i++){
            int temp = bytes[index+i]<0?bytes[index+i]+256:bytes[index+i];
            result |= temp<<(8*i);
        }
        return result;
    }
}
