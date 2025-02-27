package com.example.missingpartsdetection.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.missingpartsdetection.R;

import java.io.File;
import java.util.ArrayList;

public class DeviceCheckActivity extends AppCompatActivity {

    private LinearLayout leftGroup, rightGroup;
    private TextView tvComparisonResult;
    private Button backButton;
    private ArrayList<Bitmap> leftImages;  // 左侧图片 Bitmap 列表
    private ArrayList<Bitmap> rightImages; // 右侧图片 Bitmap 列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_check);

        // 初始化视图
        leftGroup = findViewById(R.id.leftGroup);
        rightGroup = findViewById(R.id.rightGroup);
        tvComparisonResult = findViewById(R.id.tvComparisonResult);
        backButton = findViewById(R.id.backButton);

        // 获取传递的数据
        if (getIntent() != null) {
            leftImages = getIntent().getParcelableArrayListExtra("leftImages");
            rightImages = getIntent().getParcelableArrayListExtra("rightImages");
        }

        // 动态加载图片到布局
        loadImages(leftGroup, leftImages);
        loadImages(rightGroup, rightImages);

        // 对比两组图片显示结果
        compareImages();

        // 设置返回按钮点击事件
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * 动态加载图片到指定布局
     * @param layout 布局（左组或右组）
     * @param bitmaps Bitmap 列表
     */
    private void loadImages(LinearLayout layout, ArrayList<Bitmap> bitmaps) {
        if (layout == null) {
            Log.e("DeviceCheckActivity", "loadImages: layout is null!");
            return;
        }

        layout.removeAllViews(); // 清空布局中的内容
        for (Bitmap bitmap : bitmaps) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    300 // 设置图片高度
            ));
            imageView.setPadding(8, 8, 8, 8);

            // 设置 Bitmap 到 ImageView
            imageView.setImageBitmap(bitmap);

            // 将加载好的 ImageView 添加到布局
            layout.addView(imageView);
        }
    }

    /**
     * 对比两组图片是否一致
     */
    private void compareImages() {

        for (int i = 0; i < leftImages.size(); i++) {
            // 这里可以加入具体的对比逻辑
            if (!leftImages.get(i).sameAs(rightImages.get(i))) {
                tvComparisonResult.setText("零件不一致");
                tvComparisonResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                return;
            }
        }

        // 如果合格
        tvComparisonResult.setText("零件一致");
        tvComparisonResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
    }
}