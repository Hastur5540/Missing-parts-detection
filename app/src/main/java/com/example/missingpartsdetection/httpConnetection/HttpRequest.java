package com.example.missingpartsdetection.httpConnetection;

import android.util.Log;

import com.example.missingpartsdetection.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;

public class HttpRequest {

    private final String url;

    public HttpRequest(String url){
        String root_path = Constants.TARGET_IP_ADDRESS + ":" + Constants.PORT;

        this.url = root_path + url;
    }


    public String getCompareResult(ArrayList<String> photosPath) throws IOException, JSONException {
//        System.out.println(this.url);
        JSONObject jsonObject = new JSONObject();
        JSONArray photosArray = new JSONArray();
        for (String photoPath:photosPath){
            String base64Image = encodeImageToBase64(photoPath);
            photosArray.put(base64Image);
        }
        jsonObject.put("photos", photosArray);
        // 设置连接
        InputStream inputStream = null;
        String jsonResponse = null;


        // 创建 URL 并打开连
        URL urlObj = new URL(this.url);
        HttpURLConnection httpConn = (HttpURLConnection) urlObj.openConnection();
//            httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
//            httpConn.setDoInput(true);
        httpConn.setRequestMethod("POST");
        httpConn.setRequestProperty("Content-Type", "application/json");  // 设置请求头，告诉服务器这是 JSON 数据

        // 写入 JSON 数据到请求体
        OutputStream outputStream = httpConn.getOutputStream();
        outputStream.write(jsonObject.toString().getBytes());
        outputStream.flush();

        // 获取服务器响应
        StringBuilder response = new StringBuilder();
        inputStream = httpConn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        jsonResponse = response.toString();


        return jsonResponse;
    }

    private String encodeImageToBase64(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        FileInputStream fileInputStream = new FileInputStream(imageFile);
        byte[] imageBytes = new byte[(int) imageFile.length()];
        fileInputStream.read(imageBytes);
        fileInputStream.close();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
//
//
//    private static void addFilePart(OutputStream outputStream, File uploadFile, String fieldName, String boundary, String LINE_FEED) throws Exception {
//        StringBuilder formData = new StringBuilder();
//        formData.append("--").append(boundary).append(LINE_FEED);
//        formData.append("Content-Disposition: form-data; name=\"").append(fieldName)
//                .append("\"; filename=\"").append(uploadFile.getName()).append("\"").append(LINE_FEED);
//        formData.append("Content-Type: ").append("image/jpeg").append(LINE_FEED);
//        formData.append(LINE_FEED);
//        outputStream.write(formData.toString().getBytes());
//
//        try (FileInputStream inputStream = new FileInputStream(uploadFile)) {
//            byte[] buffer = new byte[4096];
//            int bytesRead = -1;
//            while ((bytesRead = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//            }
//        }
//        outputStream.write(LINE_FEED.getBytes());
//    }
//
//    public static void main(String[] args) throws IOException {
//        HttpRequest httpRequest = new HttpRequest("/process_image");
//
//        String image1Path = "C:\\Users\\yujia\\Desktop\\江浩\\easy\\IMG20240727101344.jpg";
//        String image2Path = "C:\\Users\\yujia\\Desktop\\江浩\\easy\\IMG20240727101350.jpg";
//
//        System.out.println(httpRequest.getCompareResult(image1Path, image2Path));


//        try {
//            File imageFile = new File(image1Path);
//            FileInputStream fileInputStream = new FileInputStream(imageFile);
//            byte[] imageBytes = new byte[(int) imageFile.length()];
//            fileInputStream.read(imageBytes);
//            fileInputStream.close();
//
//            // 将字节数组转换为Base64字符串
//            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
//            System.out.println("Base64 String: " + imageBase64);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
