package com.example.missingpartsdetection.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.missingpartsdetection.R;
import com.example.missingpartsdetection.database.DatabaseHelper;
import com.example.missingpartsdetection.entity.Device;

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
        imageView = findViewById(R.id.imageView);
        databaseHelper = new DatabaseHelper(this);

        listButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
            startActivity(intent);
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
                    if (isDeviceIdExists(id)) {
                        Toast.makeText(this, "设备号 " + id + " 已存在", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseHelper.addDevice(id, photoPath);
                        Toast.makeText(this, "设备信息已保存！", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    }
                }
            }
        });


        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
            intent.putExtra("DeviceId", idInput.getText().toString());
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            photoPath = data.getStringExtra("photoPath");
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            imageView.setImageBitmap(bitmap);
        }
    }
}
