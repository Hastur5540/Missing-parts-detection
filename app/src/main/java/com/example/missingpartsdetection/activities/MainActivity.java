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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.missingpartsdetection.R;
import com.example.missingpartsdetection.database.DatabaseHelper;
import com.example.missingpartsdetection.entity.Device;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private EditText idInput;
    private Button listButton, captureButton, submitButton;
    private String photoPath = "";
    private ImageView imageView;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listButton = findViewById(R.id.listButton);
        idInput = findViewById(R.id.idInput);
        captureButton = findViewById(R.id.buttonCamera);
        submitButton = findViewById(R.id.submitButton);
        imageView = findViewById(R.id.imageView_1);
        databaseHelper = new DatabaseHelper(this);

        String deviceFolderName = "Device_temp"; // 暂时存储图片
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);
        // 创建文件夹，如果不存在则创建
        if (!storageDir.exists() && !storageDir.mkdirs()) {
        return;
        }

        listButton.setOnClickListener(v -> {
            deleteTempFiles();
            Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
            startActivity(intent);
            finish();
        });

        captureButton.setOnClickListener(v -> {
            if (validateInputs()) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("DeviceId", idInput.getText().toString());
                intent.putExtra("inOutFlag", "in");
                startActivityForResult(intent, 1);
            }
        });

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                if (photoPath.isEmpty()) {
                    Toast.makeText(this, "请先拍摄照片", Toast.LENGTH_SHORT).show();
                } else {
                    String id = idInput.getText().toString();
                    // 检查工号是否已存在
//                    if (isDeviceIdExists(id)) {
//                        Toast.makeText(this, "设备号 " + id + " 已存在", Toast.LENGTH_SHORT).show();
//                    } else {
//                        databaseHelper.addDevice(id, photoPath);
//                        saveImage();
//                        Toast.makeText(this, "设备信息已保存！", Toast.LENGTH_SHORT).show();
//                        clearInputs();
//                    }
                    if (isDeviceIdExists(id)) {
                        saveImage();
                        Toast.makeText(this, "图片已补充！", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    } else {
                        databaseHelper.addDevice(id, photoPath);
                        saveImage();
                        Toast.makeText(this, "设备信息已保存！", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    }
                }
            }
        });

        submitButton.setOnLongClickListener(v -> {
            String deviceId = idInput.getText().toString().trim();
            if (deviceId.isEmpty()) {
                // 如果设备号为空，跳转到另一个页面
                Intent intent = new Intent(MainActivity.this, DeveloperPageActivity.class);
                startActivity(intent);
                return true; // 表示已处理长按事件
            }
            return false; // 表示未处理长按事件
        });


        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
            intent.putExtra("DeviceId", idInput.getText().toString());
            intent.putExtra("temp", "temp");
            intent.putExtra("inOutFlag", "in");
            startActivity(intent);
        });

    }

    public boolean isDeviceIdExists(String deviceId) {
        Device device = databaseHelper.getDeviceById(deviceId);
        boolean exists = false;
        if(device !=null){
            exists=true;
        }
        return exists;
    }

    private boolean validateInputs() {
        if (idInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请先输入设备号", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void clearInputs() {
        idInput.setText("");
        imageView.setImageDrawable(null); // 清空图片显示
        photoPath = ""; // 清空照片路径
    }

    private String loadFirstImagesFromDevice(String inOutFlag) {

        String deviceFolderName = "Device_temp"; // 使用设备ID命名文件夹
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);
        String fileName = inOutFlag+'_'+ idInput.getText().toString();
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

    private void saveImage() {
        // 获取设备ID
        String id = idInput.getText().toString();
        String deviceFolderName = "Device_" + id; // 使用设备ID命名文件夹
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);

        // 创建文件夹，如果不存在则创建
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            return;
        }

        // 复制 Device_temp 下的所有文件到新创建的文件夹
        File tempDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Device_temp");
        if (tempDir.exists() && tempDir.isDirectory()) {
            File[] tempFiles = tempDir.listFiles();
            if (tempFiles != null) {
                for (File tempFile : tempFiles) {
                    // 复制文件到新目录
                    File newFile = new File(storageDir, tempFile.getName());
                    try {
                        copyFile(tempFile, newFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 调用方法删除 Device_temp 中的所有文件
                deleteTempFiles();
            }
        }
    }

    // 新方法：删除文件
    private void deleteTempFiles() {
        File tempDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Device_temp");
        File[] tempFiles = tempDir.listFiles();
        for (File tempFile : tempFiles) {
            if (tempFile.delete()) {
                // 成功删除文件
                System.out.println("Deleted: " + tempFile.getName());
            } else {
                // 删除失败
                System.out.println("Failed to delete: " + tempFile.getName());
            }
        }
    }

    // 复制文件的方法
    private void copyFile(File sourceFile, File destFile) throws IOException {
        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            photoPath = loadFirstImagesFromDevice("in");
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            Log.e("MainActivity", "inside："+bitmap);
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
        }
    }
}
