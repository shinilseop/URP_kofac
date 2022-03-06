package com.example.urp_led_contrller;

import java.net.HttpURLConnection;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Get_CCT extends Thread {
    private String ip, serialNumber;
    private int port;
    boolean isRun;

    public Get_CCT(String ip, int port, String serialNumber){
        this.ip=ip;
        this.port=port;
        this.serialNumber=serialNumber;
        isRun=true;
    }

    public void run(){
        HttpURLConnection urlConn=null;
        while(isRun){
            try {
                System.out.println("TRY");
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                System.out.println("SEND : "+"http://"+ip+":"+port+"/hakbu_get_cct/");
                RequestBody body = new FormBody.Builder()
                        .add("serialNumber", serialNumber)
                        .build();
                Request request = new Request.Builder()
                        .url("http://"+ip+":"+port+"/hakbu_get_cct/")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();

                System.out.println(response.body().string());

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConn != null)
                    urlConn.disconnect();
            }
        }
    }
}
