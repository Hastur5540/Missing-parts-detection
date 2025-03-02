package com.example.missingpartsdetection.activities;

import static com.example.missingpartsdetection.utils.Constants.*;
import static java.lang.Math.max;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.missingpartsdetection.R;
import com.example.missingpartsdetection.utils.Constants;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.example.missingpartsdetection.utils.Constants.*;

public class CameraActivity extends AppCompatActivity {
    private PreviewView cameraPreview;
    private Button captureButton;
    private Button backButton;
    private ImageView imageView;
    private ImageView overlayImageView;
    private Button selectPhotoButton;
    private SeekBar seekBar;
    // Add these constants for request codes
    private static final int PICK_IMAGE_REQUEST = 102;

    private String photoPath="";
    private String d_id = null;

    private ImageCapture capturedImg;
//    private View captureFrame;
    private HashMap<String, Integer> screenHW = new HashMap<>();

    private final float CAMERA_PREVIEW_HW_RATIO = (float)SAVED_IMG_RESOLUTION_HEIGHT/SAVED_IMG_RESOLUTION_WIDTH;
    private int cameraPreviewHeight;
    private int cameraPreviewWidth;

    private ProcessCameraProvider processCameraProvider;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private int photoCount = 0; // Photo count
    private TextView photoCountTextView; // Reference for the TextView
    private  String inOutFlag = "";


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // 得当前activity的h 和 w
        getScreenHW(this);
        super.onConfigurationChanged(newConfig);
        startCamera();
        overlayImageView.setVisibility(View.GONE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraPreview = findViewById(R.id.cameraPreview);
//        captureFrame = findViewById(R.id.captureFrame);
        captureButton = findViewById(R.id.captureButton_1);
        backButton = findViewById(R.id.backButton);
        overlayImageView = findViewById(R.id.overlayImageView);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        seekBar = findViewById(R.id.seekBar);
        photoCountTextView = findViewById(R.id.photoCountTextView);
        String deviceId = getIntent().getStringExtra("DeviceId");
        if (deviceId != null) {
            d_id = deviceId;
        }

        inOutFlag = getIntent().getStringExtra("inOutFlag");
        photoCount = getPhotoCount();
        photoCountTextView.setText("已拍摄: " + photoCount + " 张"); // 更新文本提示

        getScreenHW(this);
        // 查看当前摄像头权限并获取
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            startCamera();
        }

        startCamera();

        overlayImageView.setOnClickListener(v -> {
            if (overlayImageView.getVisibility() == View.VISIBLE) {
                overlayImageView.setVisibility(View.GONE); // 点击后隐藏
            }
        });

        captureButton.setOnClickListener(v -> capturePhoto());

        backButton.setOnClickListener(v -> {

            finish(); // 结束当前活动
        });

        // Check "inOutFlag" and show button/seekBar for "out"

        if ("out".equals(inOutFlag)) {
            selectPhotoButton.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);

            // Select photo button click listener
            selectPhotoButton.setOnClickListener(v -> choosePhoto());

            // SeekBar listener to adjust transparency
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float alpha = progress / 100f; // Convert to a fraction
                    overlayImageView.setAlpha(alpha); // Set transparency
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    private void choosePhoto() {
        Intent intent = new Intent(this, Camera_AlbumActivity.class);
        intent.putExtra("DeviceId", d_id); // 传递设备ID
        intent.putExtra("inOutFlag", "in"); // 传入 in/out 标志
        startActivityForResult(intent, 1); // 启动Camera_AlbumActivity并等待返回
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 判断从选择照片页面返回的结果
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // 获取照片的路径
            String selectedImagePath = data.getStringExtra("SelectedImagePath");
            if (selectedImagePath != null) {
                // 根据路径读取图片并设置到 overlayImageView
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, cameraPreviewWidth, cameraPreviewHeight, true);

                // 获取 cameraPreview 的位置和尺寸
                int left = cameraPreview.getLeft();
                int top = cameraPreview.getTop();

                // 设置 overlayImageView 的布局参数
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) overlayImageView.getLayoutParams();
                params.width = cameraPreviewWidth;
                params.height = cameraPreviewHeight;
                params.leftMargin = left;  // 对齐左边
                params.topMargin = top;    // 对齐顶部

                overlayImageView.setLayoutParams(params);
                overlayImageView.requestLayout();

                overlayImageView.setImageBitmap(scaledBitmap);

                // 确保 overlayImageView 显示
                overlayImageView.setVisibility(View.VISIBLE);
            }

        }
    }

    // 根据预定比例常量重塑View组件
    private void reshapeView(View view, int height_limit, int width, float hwRatio, int flag){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        if(flag==0){
            params.width = width;
            params.height = Math.min((int)(params.width*hwRatio), height_limit);

            cameraPreviewWidth = params.width;
            cameraPreviewHeight = params.height;

        }else{
            // 此时获取action_Bar的height
            int actionBarHeight = -1;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }

            params.height = width - actionBarHeight;
            params.width = Math.min((int)(params.height*hwRatio), height_limit);

            cameraPreviewHeight = params.height;
            cameraPreviewWidth = params.width;
        }
        view.setLayoutParams(params);
        view.requestLayout();

//        System.out.println("123");
    }


    private void startCamera() {
        int rotationDegrees = getWindowManager().getDefaultDisplay().getRotation();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {

            getScreenHW(this);
            if(rotationDegrees%2==1){  //横着的
                reshapeView(cameraPreview, 100000, screenHW.get("height"), CAMERA_PREVIEW_HW_RATIO, 1);
            }else{ //竖着的
                reshapeView(cameraPreview, 100000, screenHW.get("width"), CAMERA_PREVIEW_HW_RATIO, 0);
            }
            try {
                processCameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // 修改ImageCapture的设置以支持1280x720
                ImageCapture.Builder builder = new ImageCapture.Builder();
//                builder.setTargetResolution(new Size(1280, 120)); // 设置目标分辨率

                capturedImg = builder.build();

                processCameraProvider.unbindAll();
                processCameraProvider.bindToLifecycle(this, cameraSelector, preview, capturedImg);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (capturedImg == null) {
            return;
        }
        capturedImg.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                // 图片处理流程： 旋转，放缩，截取。
                Bitmap bitmap = imageProxyToBitmap(image);
//                Bitmap croppedImg = bitmap;
                executorService.execute(() -> {
                    // 图片保存完成后，通过主线程运行后续逻辑
                    runOnUiThread(() -> {
                        returnToCameraScreen();
                    });
                    Bitmap croppedImg = cropImg(bitmap);
                    String inOutFlag = getIntent().getStringExtra("inOutFlag");
                    saveImage(bitmapToByteArray(croppedImg), inOutFlag);
                    Log.d("CameraActivity", "photoPath: " + photoPath);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("photoPath", photoPath);
                    setResult(RESULT_OK, resultIntent);
                });
                image.close();

//                Toast.makeText(CameraActivity.this, "Captured and cropped image obtained", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });
    }


    private Bitmap imageProxyToBitmap(ImageProxy image) {
//        int rotationDegrees = image.getImageInfo().getRotationDegrees();

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
//        return rotateBitmap(bitmap, rotationDegrees);
    }


    // 旋转
    private Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        if (rotationDegrees == 0) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public Bitmap cropImg(Bitmap oriImg) {
        // 获取原始图片的宽度和高度
        int rotationDegrees = getWindowManager().getDefaultDisplay().getRotation();
        int oriWidth = oriImg.getWidth();
        int oriHeight = oriImg.getHeight();
        float widthRatio = (float)cameraPreviewWidth / oriWidth;
        float heightRatio= (float)cameraPreviewHeight/ oriHeight;

        float ratio = max(widthRatio, heightRatio);
        Float realWidth = cameraPreviewWidth / ratio;
        Float realHeight = cameraPreviewHeight / ratio;

        int ww = realWidth.intValue();
        int hh = realHeight.intValue();

        // 计算裁剪区域的起始坐标，确保不会超出图片边界
        int startX = max(0, (oriWidth - ww) / 2);
        int startY = max(0, (oriHeight - hh) / 2);

        // 确保裁剪区域在图片范围内
        startX = Math.min(startX, startX + ww);
        startY = Math.min(startY, startY + hh);

        // 使用 createBitmap 方法裁剪图像
        if(rotationDegrees % 2 == 1){
            return Bitmap.createBitmap(oriImg, startX, startY, ww, hh);
        }else {
            return Bitmap.createBitmap(oriImg, startX, startY, ww, hh);
        }
    }

    // 获取屏幕宽高（pixel）
    public void getScreenHW(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenHW.put("height", displayMetrics.heightPixels);
        screenHW.put("width", displayMetrics.widthPixels);
    }

    private void returnToCameraScreen() {
        if ("out".equals(getIntent().getStringExtra("inOutFlag"))) {
            selectPhotoButton.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);

            // Select photo button click listener
            selectPhotoButton.setOnClickListener(v -> choosePhoto());

            // SeekBar listener to adjust transparency
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float alpha = progress / 100f; // Convert to a fraction
                    overlayImageView.setAlpha(alpha); // Set transparency
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        // 重新启动相机
        startCamera();
        photoCountTextView = findViewById(R.id.photoCountTextView);
        photoCount = photoCount + 1;
        photoCountTextView.setText("已拍摄: " + photoCount + " 张"); // 更新文本提示
        // 设置按钮点击事件
        captureButton.setOnClickListener(v1 -> capturePhoto());
        backButton.setOnClickListener(v2 -> finish());
    }

    private void saveImage(byte[] data, String inOutFlag) {
        // 获取设备ID
        String deviceFolderName = "Device_temp"; // 默认临时存储
        if ("out".equals(inOutFlag)) {
            deviceFolderName = "Device_" + d_id;
        }
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);

        // 创建文件夹
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            return;
        }

        // 获取传递的 inOutFlag 并拼接文件名
        inOutFlag = getIntent().getStringExtra("inOutFlag") + "_";
        String fileName = inOutFlag + d_id + "_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(storageDir, fileName);
        photoPath = imageFile.getAbsolutePath();

        // 解码原始图像数据
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (originalBitmap != null) {
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            Log.d("CameraActivity", "Original Image: Width = " + width + ", Height = " + height);

            // 计算缩小后的尺寸（等比例缩小2倍）
            int newWidth = width / 4;
            int newHeight = height / 4;

            // 创建缩小的 Bitmap
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

            // 保存缩小后的图片
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); // 90% 质量压缩
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 释放 Bitmap 资源
            originalBitmap.recycle();
            scaledBitmap.recycle();

            Log.d("CameraActivity", "Scaled Image Saved: Width = " + newWidth + ", Height = " + newHeight);
        }
    }

    private int getPhotoCount() {
        if(inOutFlag.equals("in")){
            File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Device_temp");
            if (storageDir.exists() && storageDir.isDirectory()) {
                File[] files = storageDir.listFiles();
                int count = 0;
                if (files != null) {
                    for (File file : files) {
                        // 检查文件扩展名是否为图片格式
                        if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png"))) {
                            count++;
                        }
                    }
                }
                return count;
            }
        }else{
            File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Device_"+d_id);
            if (storageDir.exists() && storageDir.isDirectory()) {
                File[] files = storageDir.listFiles();
                int count = 0;
                if (files != null) {
                    for (File file : files) {
                        // 检查文件扩展名是否为图片格式
                        if (file.isFile() && (file.getName().startsWith("out"))) {
                            count++;
                        }
                    }
                }
                return count;
            }
        }
        return 0; // 如果文件夹不存在，则返回0
    }

    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 将 Bitmap 压缩为 PNG 格式，质量为 100（无损压缩）
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

}