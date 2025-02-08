package com.example.missingpartsdetection.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.missingpartsdetection.R;
import com.example.missingpartsdetection.utils.Constants;

public class DeveloperPageActivity extends AppCompatActivity {

    private EditText ipInput, portInput;
    private Button updateButton;
    private Button backButton; // 返回按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_page);

        // 绑定 UI 组件
        ipInput = findViewById(R.id.ipInput);
        portInput = findViewById(R.id.portInput);
        updateButton = findViewById(R.id.updateButton);
        backButton = findViewById(R.id.backButton);

        // 设置按钮点击事件 - 更新 IP 地址和端口号
        updateButton.setOnClickListener(v -> {
            String newIpAddress = ipInput.getText().toString().trim();
            String newPort = portInput.getText().toString().trim();

            // 检查 IP 地址和端口号是否为空
            if (TextUtils.isEmpty(newIpAddress)) {
                Toast.makeText(this, "请输入 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(newPort)) {
                Toast.makeText(this, "请输入端口号", Toast.LENGTH_SHORT).show();
                return;
            }

            // 检查端口号是否合法
            if (!isPortValid(newPort)) {
                Toast.makeText(this, "端口号必须在 1-65535 范围内", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新 Constants 中的 IP 地址和端口号
            Constants.TARGET_IP_ADDRESS = newIpAddress;
            Constants.PORT = newPort;

            // 提示用户更新成功
            Toast.makeText(this, "IP 和端口已更新:\nIP: " +
                    Constants.TARGET_IP_ADDRESS + "\nPort: " + Constants.PORT, Toast.LENGTH_LONG).show();
        });

        // 设置返回按钮点击事件
        backButton.setOnClickListener(v -> {
            // 返回上一个页面
            finish();
        });
    }

    // 检查端口号是否合法
    private boolean isPortValid(String portText) {
        try {
            int port = Integer.parseInt(portText);
            return port >= 1 && port <= 65535; // 1 - 65535 是合法端口号范围
        } catch (NumberFormatException e) {
            return false;
        }
    }
}