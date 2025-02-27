package com.example.missingpartsdetection.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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


    private float CAMERA_PREVIEW_HW_RATIO = -1;
    private float FRAME_CAMERAPREVIEW_RATIO = (float) 0.8;
    private float CAPTURE_FRAME_HW_RATIO = (float) 1.4;
    private float MAX_HEIGHT_RATIO = (float) 3.5/4;

    private ProcessCameraProvider processCameraProvider;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private int photoCount = 0; // Photo count
    private TextView photoCountTextView; // Reference for the TextView

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

        // 得当前activity的h 和 w
        getScreenHW(this);

        String deviceId = getIntent().getStringExtra("DeviceId");
        if (deviceId != null) {
            d_id = deviceId;
        }


        // 查看当前摄像头权限并获取
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            startCamera();
        }

        startCamera();


        captureButton.setOnClickListener(v -> capturePhoto());

        backButton.setOnClickListener(v -> {

            finish(); // 结束当前活动
        });

        // Check "inOutFlag" and show button/seekBar for "out"
        String inOutFlag = getIntent().getStringExtra("inOutFlag");
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
                overlayImageView.setImageBitmap(bitmap);

                // 确保 overlayImageView 显示
                overlayImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    // 根据预定比例常量重塑View组件
    private void reshapeView(View view, int height_limit, int width, float hwRatio){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.width = width;
        params.height = Math.min((int)(params.width*hwRatio), height_limit);
        view.setLayoutParams(params);
        view.requestLayout();
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        if (CAMERA_PREVIEW_HW_RATIO != -1){
            reshapeView(cameraPreview, (int) (screenHW.get("height") * MAX_HEIGHT_RATIO), screenHW.get("width"), CAMERA_PREVIEW_HW_RATIO);
//            reshapeView(captureFrame, (int) (screenHW.get("height") * MAX_HEIGHT_RATIO * 0.8), (int) (screenHW.get("width") * FRAME_CAMERAPREVIEW_RATIO), CAPTURE_FRAME_HW_RATIO);
        }




        cameraProviderFuture.addListener(() -> {
            try {
                processCameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                capturedImg = new ImageCapture.Builder().build();

                processCameraProvider.unbindAll();

                if (CAMERA_PREVIEW_HW_RATIO == -1) {
                    processCameraProvider.bindToLifecycle(this, cameraSelector, capturedImg);
                    capturedImg.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
                        @Override
                        public void onCaptureSuccess(@NonNull ImageProxy image) {

                            int rotationDegree = image.getImageInfo().getRotationDegrees();
                            if (rotationDegree == 0 || rotationDegree == 180)
                                CAMERA_PREVIEW_HW_RATIO = (float) image.getHeight() / image.getWidth();
                            if (rotationDegree == 90 || rotationDegree == 270)
                                CAMERA_PREVIEW_HW_RATIO = (float) image.getWidth() / image.getHeight();

                            reshapeView(cameraPreview, (int) (screenHW.get("height") * MAX_HEIGHT_RATIO), screenHW.get("width"), CAMERA_PREVIEW_HW_RATIO);
//                            reshapeView(captureFrame, (int) (screenHW.get("height") * MAX_HEIGHT_RATIO * 0.8), (int) (screenHW.get("width") * FRAME_CAMERAPREVIEW_RATIO), CAPTURE_FRAME_HW_RATIO);

                            image.close();


                            processCameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, preview);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            exception.printStackTrace();
                        }
                    });
                }else{
                    processCameraProvider.bindToLifecycle(this, cameraSelector, preview, capturedImg);
                }

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

//                croppedImg = cropBitmapToFrame(bitmap, captureFrame);

                executorService.execute(() -> {
                    // 图片保存完成后，通过主线程运行后续逻辑
                    runOnUiThread(() -> {
                        returnToCameraScreen();
                    });
                    saveImage(bitmapToByteArray(bitmap));
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
        int rotationDegrees = image.getImageInfo().getRotationDegrees();

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        return rotateBitmap(bitmap, rotationDegrees);
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

//    private Bitmap cropBitmapToFrame(Bitmap original, View frame) {
//        int cameraPreviewHeight = cameraPreview.getHeight();
//        int cameraPreviewWidth = cameraPreview.getWidth();
//
//        // 需要先将图片缩放到CameraPreview上的大小
//        Bitmap scalaredImg = Bitmap.createScaledBitmap(original, cameraPreviewWidth, cameraPreviewHeight, true);
//
//        // Get frame position and size
//        int[] location = new int[2];
//        frame.getLocationOnScreen(location);
//        int frameX = location[0];
//        int frameY = location[1];
//        int frameWidth = frame.getWidth();
//        int frameHeight = frame.getHeight();
//
//
//        int[] previewLoc = new int[2];
//        cameraPreview.getLocationOnScreen(previewLoc);
//        int preFrameX = previewLoc[0];
//        int preFrameY = previewLoc[1];
//
//
//        // 返回截取结果
////        return original;
//        return Bitmap.createBitmap(scalaredImg, frameX-preFrameX, frameY-preFrameY, frameWidth, frameHeight);
//    }


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
        }else{
            setContentView(R.layout.activity_camera);
            cameraPreview = findViewById(R.id.cameraPreview);
            captureButton = findViewById(R.id.captureButton_1);
            backButton = findViewById(R.id.backButton);
        }

        // 重新启动相机
        startCamera();
        photoCountTextView = findViewById(R.id.photoCountTextView);
        photoCount++; // 每次捕获成功后，增加照片数量
        photoCountTextView.setText("已拍摄: " + photoCount + " 张"); // 更新文本提示
        // 设置按钮点击事件
        captureButton.setOnClickListener(v1 -> capturePhoto());
        backButton.setOnClickListener(v2 -> finish());
    }

    private void saveImage(byte[] data) {
        // 获取设备ID
        String deviceFolderName = "Device_" + d_id; // 使用设备ID命名文件夹
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);

        // 创建文件夹，如果不存在则创建
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            return;
        }

        // 创建图片文件名
        String inOutFlag = getIntent().getStringExtra("inOutFlag") + "_";
        String fileName = inOutFlag + d_id + "_" + System.currentTimeMillis() + ".jpg"; // 以时间戳确保文件名唯一
        File imageFile = new File(storageDir, fileName);
        photoPath = imageFile.getAbsolutePath();
        // 写入图片文件
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 将 Bitmap 压缩为 PNG 格式，质量为 100（无损压缩）
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

}