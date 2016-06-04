package com.wjw.coolweather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by wangjianwei on 2016/6/1.
 */
public class HttpUtil {
    public  static void sendHttpRequest(final String address, final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(address); //URL对象，用地址初始化
                    connection = (HttpURLConnection) url.openConnection();//建立连接HttpURLConnenction
                    connection.setRequestMethod("GET");//get方式
                    connection.setConnectTimeout(8000);//连接超时
                    connection.setReadTimeout(8000);

                    connection.setRequestProperty("Content-type", "application/x-java-serialized-object");

                    InputStream in = connection.getInputStream();//获取输入流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));//获取bufferReader
                    StringBuilder response = new StringBuilder();//新建StringBuilder对象
                    String line;
                    while((line = reader.readLine())!= null){
                        response.append(line);
                    }
                    if(listener != null){
                        listener.onFinish(response.toString()); //回调函数，传入返回数据
                    }
                }catch (Exception e){
                    if(listener != null){
                        listener.onError(e);
                    }
                }finally {
                    if(connection != null){
                        connection.disconnect();                    }
                }
            }
        }).start();
    }
}
