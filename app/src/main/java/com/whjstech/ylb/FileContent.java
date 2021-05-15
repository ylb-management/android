package com.whjstech.ylb;

import java.util.Date;


public class FileContent {

    public int maxValueIndex;
    public int maxValue;
    public Date maxValueTime;

    public int minValueIndex;
    public int minValue;
    public Date minValueTime;

    //第一个32字节区
    public String fileClass;
    public Date createFileTime;
    public int idNumber;
    public int interval;
    public int channel;
    public Date startRecordTime;
    public int pointsNumber;
    //第二个32字节
    public int[] sampleValue;
    public int[] setValue;
    public int dotPositon;
    public int upperLimitValue;
    //第三个32字节
    public int voltage;
    public String hsVersion;
    //记录数据
    public int[] recordPoints;
}
