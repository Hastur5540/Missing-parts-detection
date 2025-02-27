package com.example.missingpartsdetection.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.missingpartsdetection.R;
import com.example.missingpartsdetection.database.DatabaseHelper;
import com.example.missingpartsdetection.entity.Device;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity {
    private static List<Device> deviceList = new ArrayList<>();
    private ArrayAdapter<Device> adapter;
    private Button backButton, searchButton;
    private EditText searchInput;
    private DatabaseHelper databaseHelper;
    private ListView deviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        deviceListView = findViewById(R.id.deviceListView);
        searchInput = findViewById(R.id.searchInput);
        backButton = findViewById(R.id.backButton);
        searchButton = findViewById(R.id.searchButton);

        databaseHelper = new DatabaseHelper(this);
        deviceList = databaseHelper.getAlldevices();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        deviceListView.setAdapter(adapter);

        // 返回按钮点击事件
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(DeviceListActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 搜索按钮点击事件
        searchButton.setOnClickListener(v -> performSearch());

        // 列表项点击事件
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            Device selectedDevice = deviceList.get(position);
            startComparisonActivity(selectedDevice);
        });

        // 搜索框文本变化监听
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 实时搜索，这里可以保留
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // 执行搜索
    private void performSearch() {
        String searchQuery = searchInput.getText().toString().trim();
        Device device = databaseHelper.getDeviceById(searchQuery); // 根据需查询的工人ID传入参数
        if (device != null) {
            startComparisonActivity(device);
            return;
        }
        Toast.makeText(this, "无此人", Toast.LENGTH_SHORT).show();
    }

    // 跳转到比较活动
    private void startComparisonActivity(Device device) {
        Intent intent = new Intent(DeviceListActivity.this, ComparisonActivity.class);
        intent.putExtra("device", device);
        intent.putExtra("deviceList", new ArrayList<>(deviceList));
        startActivityForResult(intent,1);
    }

    private void deletePhoto(String photoPath) {
        File photoFile = new File(photoPath);
        if (photoFile.exists()) {
            boolean deleted = photoFile.delete();
            if (deleted) {
                Log.d("WorkerList", "Deleted photo: " + photoPath);
            } else {
                Log.d("WorkerList", "Failed to delete photo: " + photoPath);
            }
        }
    }

    //移除工人并刷新列表
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Device device = (Device) data.getSerializableExtra("worker");
            if (device != null) {
                String photoPath = device.getPhotoPath();
                deletePhoto(photoPath);
            }
            databaseHelper.deleteDevice(device.getId());
            deviceList.remove(device);
            adapter.notifyDataSetChanged();
        }
    }
}