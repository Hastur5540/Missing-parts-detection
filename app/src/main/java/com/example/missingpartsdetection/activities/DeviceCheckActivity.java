package com.example.missingpartsdetection.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.missingpartsdetection.R;

import java.util.ArrayList;

public class DeviceCheckActivity extends AppCompatActivity {

    private LinearLayout leftGroup, rightGroup;
    private TextView tvComparisonResult;
    private Button backButton;
    private ArrayList<Integer> leftImages;  // 左侧图片资源ID列表
    private ArrayList<Integer> rightImages; // 右侧图片资源ID列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_check);

        // 初始化视图
        leftGroup = findViewById(R.id.leftGroup);
        rightGroup = findViewById(R.id.rightGroup);
        tvComparisonResult = findViewById(R.id.tvComparisonResult);
        backButton = findViewById(R.id.backButton);

        // 检查视图是否为空
        if (leftGroup == null || rightGroup == null) {
            throw new IllegalStateException("Error: leftGroup or rightGroup not found in layout!");
        }

        // 加载图片资源（通过资源ID）
        leftImages = new ArrayList<>();
        rightImages = new ArrayList<>();

        // 添加左组图片资源ID
        leftImages.add(R.drawable.image_1);
        leftImages.add(R.drawable.image_2);
        leftImages.add(R.drawable.image_3);

        // 添加右组图片资源ID
        rightImages.add(R.drawable.image_1_1);
        rightImages.add(R.drawable.image_2_2);
        rightImages.add(R.drawable.image_3_3);

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
     * @param imageResources 图片资源ID列表
     */
    private void loadImages(LinearLayout layout, ArrayList<Integer> imageResources) {
        if (layout == null) {
            Log.e("DeviceCheckActivity", "loadImages: layout is null!");
            return;
        }

        layout.removeAllViews(); // 清空布局中的内容
        for (int resId : imageResources) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    300 // 设置图片高度
            ));
            imageView.setPadding(8, 8, 8, 8);

            // 使用 Glide 加载图片（资源ID）
            Glide.with(this).load(resId).into(imageView);

            // 将加载好的ImageView添加到布局
            layout.addView(imageView);
        }
    }

    /**
     * 对比两组图片是否一致
     */
    private void compareImages() {
        // 验证图片资源数量是否一致
        if (leftImages.size() != rightImages.size()) {
            tvComparisonResult.setText("零件不一致");
            tvComparisonResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }

        for (int i = 0; i < leftImages.size(); i++) {
            if (!leftImages.get(i).equals(rightImages.get(i))) {
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