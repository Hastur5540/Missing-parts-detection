package com.example.missingpartsdetection.httpConnetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import androidx.appcompat.app.AppCompatActivity;

import com.example.missingpartsdetection.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

public class HttpRequest {

    private final String url;

    public HttpRequest(String url){
        String root_path = Constants.TARGET_IP_ADDRESS + ":" + Constants.PORT;

        this.url = root_path + url;
    }


    public String getCompareResult(Pair<ArrayList<String>, ArrayList<String>> modelsAndPhotosPath, String haha) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray photosArray = new JSONArray();
        JSONArray modelsArray = new JSONArray();

        List<String> modelsPath = modelsAndPhotosPath.first;
        List<String> photosPath = modelsAndPhotosPath.second;

        for (String photoPath : photosPath) {
            String base64Image = encodeImageToBase64(photoPath);
            photosArray.put(base64Image);
        }
        for (String modelPath: modelsPath
             ) {
            String base64Image = encodeImageToBase64(modelPath);
            modelsArray.put(base64Image);
        }

        jsonObject.put("photos", photosArray);
        jsonObject.put("model", modelsArray);

        HttpURLConnection httpConn = null;
        String jsonResponse = null;

        String errorMess = "";
        try {
            // 创建 URL 并打开连接
            URL urlObj = new URL(this.url);
            httpConn = (HttpURLConnection) urlObj.openConnection();
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type", "application/json");
//            httpConn.setConnectTimeout(30000); // 5秒连接超时
//            httpConn.setReadTimeout(30000); // 10秒读取超时

            // 发送请求
            OutputStream outputStream = httpConn.getOutputStream();
            outputStream.write(jsonObject.toString().getBytes());
            outputStream.flush();

            // 获取响应码
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    jsonResponse = response.toString();
                }

                // 解析 JSON 响应
                ObjectMapper objMapper = new ObjectMapper();
                try {
                    JsonNode jsonNode = objMapper.readTree(jsonResponse);
                    JsonNode processedImagesList = jsonNode.get("shuchu_images");
                    if (processedImagesList != null) {
                        for (JsonNode node : processedImagesList) {
                            String name = node.get("filename").asText();
                            String imageBase64 = node.get("base64").asText();
                            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
                            this.saveReturnedImage(imageBytes, name, haha);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("JSON 解析错误：" + e.getMessage());
                }

            } else {
                // 服务器错误或客户端错误
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    System.err.println("HTTP 错误 (" + responseCode + "): " + errorResponse.toString());
                }
            }
        } catch (SocketTimeoutException e) {
            return "连接超时";
        } catch (IOException e) {
            return "网络错误";
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }

        return "处理成功";
    }


    private String encodeImageToBase64(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        FileInputStream fileInputStream = new FileInputStream(imageFile);
        byte[] imageBytes = new byte[(int) imageFile.length()];
        fileInputStream.read(imageBytes);
        fileInputStream.close();
        return Base64.getEncoder().encodeToString(imageBytes);
    }


    public void saveReturnedImage(byte[] data, String name, String filePath) {
        String savePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        File saveFile = new File(savePath);
        if (!saveFile.exists() && !saveFile.mkdirs()) {
            return;
        }

        savePath = savePath + name;
        // 解码图像数据以获取宽高
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            // 输出图像尺寸到日志
            Log.d("CameraActivity", "Image saved: Width = " + width + ", Height = " + height);
        }
        // 写入图片文件
        try (FileOutputStream fos = new FileOutputStream(savePath)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
