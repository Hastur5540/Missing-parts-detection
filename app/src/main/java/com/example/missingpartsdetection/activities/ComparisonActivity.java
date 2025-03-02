package com.example.missingpartsdetection.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.missingpartsdetection.R;
import com.example.missingpartsdetection.database.DatabaseHelper;
import com.example.missingpartsdetection.entity.Device;
import com.example.missingpartsdetection.httpConnetection.HttpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComparisonActivity extends AppCompatActivity {
    private EditText toolsInput;
    private Button compareButton, captureButton,backButton;
    private ImageView deviceImageView_IN;
    private ImageView deviceImageView_OUT;
    private Device device;
    private static List<Device> deviceList = new ArrayList<>();
    String photoPath_OUT = "";
    private RelativeLayout loadingView; // 加载动画视图
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        toolsInput = findViewById(R.id.toolsInput);
        compareButton = findViewById(R.id.compareButton);
        captureButton = findViewById(R.id.captureButton);
        deviceImageView_IN = findViewById(R.id.deviceImageView_IN);
        deviceImageView_OUT = findViewById(R.id.deviceImageView_OUT);
        device = (Device) getIntent().getSerializableExtra("device"); // Keep using Serializable
        deviceList = (List<Device>) getIntent().getSerializableExtra("deviceList");
        backButton = findViewById(R.id.backButton);
        databaseHelper = new DatabaseHelper(this);

        //返回按钮
        backButton.setOnClickListener(v -> {
            finish();
        });
        // 获取加载视图
        loadingView = findViewById(R.id.loadingView);
        toolsInput.setText(device.getId());
        String photoPath_IN = loadFirstImagesFromDevice("in");
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath_IN);
        deviceImageView_IN.setImageBitmap(bitmap);
        photoPath_OUT = loadFirstImagesFromDevice("out");
        Bitmap bitmap_1= BitmapFactory.decodeFile(photoPath_OUT);
        deviceImageView_OUT.setImageBitmap(bitmap_1);

        deviceImageView_IN.setOnClickListener(v -> {
            Intent intent = new Intent(ComparisonActivity.this, AlbumActivity.class);
            intent.putExtra("DeviceId", device.getId());
            intent.putExtra("temp", "notTemp");
            intent.putExtra("inOutFlag", "in");
            startActivity(intent);
        });

        deviceImageView_OUT.setOnClickListener(v -> {
            Intent intent = new Intent(ComparisonActivity.this, AlbumActivity.class);
            intent.putExtra("DeviceId", device.getId());
            intent.putExtra("temp", "notTemp");
            intent.putExtra("inOutFlag", "out");
            startActivity(intent);
        });

        // Capture button functionality
        captureButton.setOnClickListener(v -> {
            Intent intent = new Intent(ComparisonActivity.this, CameraActivity.class);
            intent.putExtra("DeviceId", device.getId());
            intent.putExtra("inOutFlag", "out");
            startActivityForResult(intent, 2);
        });

        // Compare button functionality
        compareButton.setOnClickListener(v -> {
            if (photoPath_OUT.isEmpty()) {
                Toast.makeText(this, "请先拍摄照片", Toast.LENGTH_SHORT).show();
            } else {
                // 显示加载动画
                showLoading();
                new Thread(() -> {
                    HttpRequest httpRequest = new HttpRequest("/upload_json");
                    String response = "";
                    Pair<ArrayList<String>, ArrayList<String>> modelsAndOutImages = loadImagesFromDevice();
                    try {
                            response = httpRequest.getCompareResult(modelsAndOutImages, photoPath_OUT);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    if(response.equals("success")){
                        // 处理结束后，更新UI
                        runOnUiThread(() -> {
                            deletePics();
                            hideLoading();
                            // 准备跳转到新页面
                            Intent intent = new Intent(ComparisonActivity.this, DeviceCheckActivity.class);
                            intent.putExtra("DeviceId", device.getId());
                            startActivity(intent);
                        });
                    }else{
                        hideLoading();
                    }
                }).start();
            }
        });
    }

    private void deletePics() {
        String deviceFolderName = "Device_" + device.getId(); // 使用设备ID命名文件夹
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);
        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && (file.getName().startsWith("jizhun_")||file.getName().startsWith("all_"))) {
                        if (file.delete()) {
                            // 成功删除文件
                            System.out.println("Deleted: " + file.getName());
                        } else {
                            // 删除失败
                            System.out.println("Failed to delete: " + file.getName());
                        }
                    }
                }
            }
        }
    }

    private String loadFirstImagesFromDevice(String inOutFlag) {

        String deviceFolderName = "Device_" + device.getId(); // 使用设备ID命名文件夹
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);
        String fileName = inOutFlag+'_'+ device.getId();
        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().startsWith(fileName)) {
                        return file.getAbsolutePath();
                    }
                }
            }
        }
        return deviceFolderName;
    }

    private ArrayList<String> loadImagesFromDevice(String inOutFlag) {
        ArrayList<String> photoList = new ArrayList<>();
        String deviceFolderName = "Device_" + device.getId(); // 使用设备ID命名文件夹
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);

        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    inOutFlag = inOutFlag + "_";
                    String fileName = inOutFlag + device.getId();
                    if (file.isFile() && file.getName().startsWith(fileName)) {
                        photoList.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return photoList;
    }

    private Pair<ArrayList<String>, ArrayList<String>> loadImagesFromDevice() {
        ArrayList<String> photoList = new ArrayList<>();
        ArrayList<String> modelList = new ArrayList<>();

        String deviceFolderName = "Device_" + device.getId(); // 使用设备ID命名文件夹
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);

        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    String filePath = file.getAbsolutePath();
                    if(fileName.startsWith("out")){
                        photoList.add(filePath);
                    }else{
                        modelList.add(filePath);
                    }
                }
            }
        }
        return new Pair<>(modelList, photoList);
    }

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE); // 显示加载动画
    }

    private void hideLoading() {
        loadingView.setVisibility(View.GONE); // 隐藏加载动画
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            photoPath_OUT = data.getStringExtra("photoPath");
            Log.e("ComparisonActivity", "inside："+photoPath_OUT);
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath_OUT);
            deviceImageView_OUT.setImageBitmap(bitmap);
        }
    }


}