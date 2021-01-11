package com.example.wallpaperapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpClient {

    /**获取APi的Josn结果 返回String*/
    public  String doGetForApi(String httpurl) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;

        try {
            URL url = new URL(httpurl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5*1000);
            connection.setReadTimeout(5*10000);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            connection.disconnect();
        }
        return result;
    }
    /**得到图片 以byte[]返回*/
    public byte[] doGetForPicUrl(String picUrl){

        HttpURLConnection connection = null;
        InputStream is = null;
        ByteArrayOutputStream br =null;
        byte[] PicData = null;
        try {
            URL url = new URL(picUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5*1000);
            connection.setReadTimeout(5*10000);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                br = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int len = 0;
                while( (len=is.read(buffer)) != -1 ){
                    br.write(buffer, 0, len);
                }
                PicData =  br.toByteArray();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            connection.disconnect();
        }
        return PicData;
    }

    /**解析Json 获取所需图片的url*/
    public String[] parseJson(String result){
        String[] picUrls ;
        try{
            JSONObject jsonObject = new JSONObject(result);
            JSONObject jsonObject1 = jsonObject.getJSONObject("res");
            JSONArray jsonArray = jsonObject1.getJSONArray("vertical");
            picUrls = new String[jsonArray.length()];
            for (int i=0;i<jsonArray.length();i++){
                JSONObject temp = (JSONObject) jsonArray.get(i);
                picUrls[i] = temp.getString("preview");
            }
            return picUrls;
        } catch (JSONException e){
            e.printStackTrace();
        }
       return null;
    }
}
