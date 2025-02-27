package com.example.missingpartsdetection.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import com.example.missingpartsdetection.utils.ImageProcess;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComparisonActivity extends AppCompatActivity {
    private EditText toolsInput;
    private Button compareButton, captureButton,backButton;
    private ImageView deviceImageView_IN;
    private ImageView deviceImageView_OUT;
    private Device device;
    private static List<Device> deviceList = new ArrayList<>();
    String photoPath_OUT = "";
    private ArrayList<Bitmap> bitmapsList_IN = new ArrayList<>();
    private ArrayList<Bitmap> bitmapsList_OUT = new ArrayList<>();
    Boolean Consistent_flags;
    private RelativeLayout loadingView; // 加载动画视图
    private DatabaseHelper databaseHelper;

    // 得到的的处理后的图片
    String image1Base64 = null;
    String image2Base64 = null;

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
            intent.putExtra("inOutFlag", "in");
            startActivity(intent);
        });

        deviceImageView_OUT.setOnClickListener(v -> {
            Intent intent = new Intent(ComparisonActivity.this, AlbumActivity.class);
            intent.putExtra("DeviceId", device.getId());
            intent.putExtra("inOutFlag", "out");
            startActivity(intent);
        });

        // Capture button functionality
        captureButton.setOnClickListener(v -> {
            Intent intent = new Intent(ComparisonActivity.this, CameraActivity.class);
            intent.putExtra("DeviceId", device.getId());
            intent.putExtra("inOutFlag", "out");
            startActivityForResult(intent, 1);
        });

        // Compare button functionality
        compareButton.setOnClickListener(v -> {
            if (photoPath_OUT.isEmpty()) {
                Toast.makeText(this, "请先拍摄照片", Toast.LENGTH_SHORT).show();
            } else {
                // 显示加载动画
                showLoading();
                new Thread(() -> {
                    // 模拟处理延时
                    try {
                        Thread.sleep(2000);// 实际比较逻辑将放置在这里

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


//                    HttpRequest httpRequest = new HttpRequest("/process_image");
//                    String photoPath = device.getPhotoPath();
//
//                    ArrayList<Map<String, Object>> result1 = null;
//                    ArrayList<Map<String, Object>> result2 = null;
//
//                    try {
//                        String response = httpRequest.getCompareResult(photoPathIn, photoPathOut);
//                        ObjectMapper objectMapper = new ObjectMapper();
//                        Map<String, Object> responseJson = objectMapper.readValue(response, new TypeReference<Map<String, Object>>(){});
//
//                        image1Base64 = Objects.requireNonNull(responseJson.get("image_base64_1")).toString();
//                        image2Base64 = Objects.requireNonNull(responseJson.get("image_base64_2")).toString();
//                        String image1CheckedPath = device.getId() + "_IN_Checked.jpg";
//                        String image2CheckedPath = device.getId() + "_OUT_Checked.jpg";
//                        ImageProcess imageProcess = new ImageProcess();
//                        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//                        imageInCheckedPath = new File(storageDir, image1CheckedPath).getAbsolutePath();
//                        imageOutCheckedPath = new File(storageDir, image2CheckedPath).getAbsolutePath();
//                        imageProcess.saveBase64ToFile(image1Base64, imageInCheckedPath);
//                        imageProcess.saveBase64ToFile(image2Base64, imageOutCheckedPath);
//
//                        result1 = (ArrayList<Map<String, Object>>) responseJson.get("result1");
//                        result2 = (ArrayList<Map<String, Object>>) responseJson.get("result2");
//
//                        if (result1 != null){
//                            for (Map<String, Object> result: result1) {
//                                String className = result.get("class_name").toString();
//                                int num = Integer.parseInt(result.get("count").toString());
//                                bundle_In.putInt(className, num);
//                            }
//                        }
//                        if (result2!=null){
//                            for (Map<String, Object> result:
//                                    result2) {
//                                String className = result.get("class_name").toString();
//                                int num = Integer.parseInt(result.get("count").toString());
//                                bundle_Out.putInt(className, num);
//                            }
//                        }
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }


                    // 处理结束后，更新UI
                    runOnUiThread(() -> {
                        hideLoading();
                        // 准备跳转到新页面
                        Intent intent = new Intent(ComparisonActivity.this, DeviceCheckActivity.class);
                        // 添加图片到列表
                        ArrayList<String> photoList_IN = loadImagesFromDevice("in");
                        ArrayList<String> photoList_OUT = loadImagesFromDevice("out");
                        for (String file : photoList_IN) {
                            Bitmap bitmapIn = BitmapFactory.decodeFile(file);
                            bitmapsList_IN.add(bitmapIn);
                        }   
                        for (String file : photoList_OUT) {
                            Bitmap bitmapOut = BitmapFactory.decodeFile(file);
                            bitmapsList_IN.add(bitmapOut);
                        }
                        intent.putParcelableArrayListExtra("leftImages", bitmapsList_IN); // 发送位图列表
                        intent.putParcelableArrayListExtra("rightImages", bitmapsList_OUT);
                        startActivity(intent);
                    });
                }).start();
            }
        });
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

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE); // 显示加载动画
    }

    private void hideLoading() {
        loadingView.setVisibility(View.GONE); // 隐藏加载动画
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            photoPath_OUT = data.getStringExtra("photoPath");
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath_OUT);
            deviceImageView_OUT.setImageBitmap(bitmap);
        }
    }


}