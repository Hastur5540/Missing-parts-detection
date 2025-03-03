package com.example.missingpartsdetection.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private Button currentVisibleDeleteButton = null;


    @SuppressLint("ClickableViewAccessibility")
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
        adapter = new ArrayAdapter<Device>(this, R.layout.list_item, R.id.text_item, deviceList) {
            @NonNull
            @Override
            public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.text_item);
                Button deleteButton = view.findViewById(R.id.deleteButton_1);

                // 设置设备信息显示
                Device device = getItem(position);
                if (device != null) {
                    textView.setText("设备号："+device.getId());
                }

                // 初始化隐藏删除按钮
                deleteButton.setVisibility(View.INVISIBLE);
                return view;
            }
        };
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
        // 处理长按事件
        deviceListView.setOnItemLongClickListener((parent, view, position, id) -> {
            // 隐藏之前显示的删除按钮
            if (currentVisibleDeleteButton != null) {
                currentVisibleDeleteButton.setVisibility(View.INVISIBLE);
            }

            // 获取当前列表项的删除按钮
            currentVisibleDeleteButton = view.findViewById(R.id.deleteButton_1);
            currentVisibleDeleteButton.setVisibility(View.VISIBLE);

            // 设置全局触摸监听
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            rootView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 转换按钮位置到屏幕坐标
                    int[] location = new int[2];
                    currentVisibleDeleteButton.getLocationOnScreen(location);

                    // 判断点击位置是否在按钮范围内
                    float x = event.getRawX();
                    float y = event.getRawY();
                    boolean isInside = x >= location[0] &&
                            x <= (location[0] + currentVisibleDeleteButton.getWidth()) &&
                            y >= location[1] &&
                            y <= (location[1] + currentVisibleDeleteButton.getHeight());

                    if (!isInside) {
                        currentVisibleDeleteButton.setVisibility(View.INVISIBLE);
                        currentVisibleDeleteButton = null;
                        rootView.setOnTouchListener(null); // 移除全局监听
                    }
                }
                return false;
            });

            // 删除按钮点击监听
            currentVisibleDeleteButton.setOnClickListener(v -> {
                deleteDevice(position);
                currentVisibleDeleteButton.setVisibility(View.INVISIBLE);
                currentVisibleDeleteButton = null;
                rootView.setOnTouchListener(null); // 移除全局监听
            });

            return true;
        });

// 修改列表项点击监听（添加隐藏按钮逻辑）
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            if (currentVisibleDeleteButton != null) {
                currentVisibleDeleteButton.setVisibility(View.INVISIBLE);
                currentVisibleDeleteButton = null;
                getWindow().getDecorView().findViewById(android.R.id.content)
                        .setOnTouchListener(null);
            }
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

    private void deleteDevice(int position) {
        Device device = deviceList.get(position);
        String id = device.getId();
        File tempDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Device_"+id);
        File[] tempFiles = tempDir.listFiles();
        for (File tempFile : tempFiles) {
            if (tempFile.delete()) {
                // 成功删除文件
                Log.d("DeviceList", "Deleted photo: "+ tempFile.getName());
            } else {
                // 删除失败
                Log.d("DeviceList", "Failed to delete: " + tempFile.getName());
            }
        }
        databaseHelper.deleteDevice(id); // 从数据库中删除
        deviceList.remove(position); // 从列表中删除
        adapter.notifyDataSetChanged(); // 更新适配器
        Toast.makeText(this, "设备"+id+"已删除", Toast.LENGTH_SHORT).show(); // 提示用户
    }
}