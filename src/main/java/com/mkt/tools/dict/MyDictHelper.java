package com.mkt.tools.dict;


public class MyDictHelper {

    private static IMyDict myDict;

    public MyDictHelper(IMyDict myDict){
        this.myDict = myDict;
    }

    public static String getDesc(String name,Object value){
        return  myDict.getDesc(name,String.valueOf(value));
    }

}
