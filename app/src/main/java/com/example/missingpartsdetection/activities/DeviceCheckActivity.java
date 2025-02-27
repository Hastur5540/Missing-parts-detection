package com.example.missingpartsdetection.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.missingpartsdetection.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeviceCheckActivity extends AppCompatActivity {

    private LinearLayout imageGroupsContainer;
    private Button backButton;
    private ArrayList<Bitmap> leftImages = new ArrayList<Bitmap>();
    private ArrayList<Bitmap> rightImages = new ArrayList<Bitmap>();
    private ArrayList<String> leftImageNames = new ArrayList<String>();
    private ArrayList<String> rightImageNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_check);

        imageGroupsContainer = findViewById(R.id.imageGroupsContainer);
        backButton = findViewById(R.id.backButton);

        // 添加左组图片资源ID
        leftImages.add(BitmapFactory.decodeResource(getResources(), R.drawable.peizhun_1));
        leftImages.add(BitmapFactory.decodeResource(getResources(), R.drawable.peizhun_2));
        leftImages.add(BitmapFactory.decodeResource(getResources(), R.drawable.peizhun_6));

        // 添加右组图片资源ID
        rightImages.add(BitmapFactory.decodeResource(getResources(), R.drawable.all_1_11));
        rightImages.add(BitmapFactory.decodeResource(getResources(), R.drawable.all_2_11));
        rightImages.add(BitmapFactory.decodeResource(getResources(), R.drawable.all_6_10));

        leftImageNames.add("peizhun_1");
        leftImageNames.add("peizhun_2");
        leftImageNames.add("peizhun_6");

        rightImageNames.add("all_1_11");
        rightImageNames.add("all_2_11");
        rightImageNames.add("all_6_10");

        // 处理图片分组和显示
        processAndDisplayImages();

        backButton.setOnClickListener(v -> finish());
    }

    private void processAndDisplayImages() {
        // 创建映射来分组图片
        Map<Integer, Bitmap> leftMap = new HashMap<>();
        Map<Integer, Bitmap> rightMap = new HashMap<>();
        Map<Integer, String> statusMap = new HashMap<>();

        // 处理左侧图片（peizhun）
        for (int i = 0; i < leftImages.size(); i++) {
            String[] parts = leftImageNames.get(i).split("[_.]");
            if (parts.length >= 2) {
                try {
                    int id = Integer.parseInt(parts[1]);
                    leftMap.put(id, leftImages.get(i));
                } catch (NumberFormatException e) {
                    Log.e("ImageProcessing", "Error parsing left image ID", e);
                }
            }
        }

        // 处理右侧图片（all）
        for (int i = 0; i < rightImages.size(); i++) {
            String[] parts = rightImageNames.get(i).split("[_.]");
            if (parts.length >= 3) {
                try {
                    int id = Integer.parseInt(parts[1]);
                    String statusCode = parts[2].substring(0, 2); // 取前两位状态码
                    rightMap.put(id, rightImages.get(i));
                    statusMap.put(id, statusCode);
                } catch (NumberFormatException e) {
                    Log.e("ImageProcessing", "Error parsing right image ID", e);
                }
            }
        }

        // 找到所有匹配的ID
        Set<Integer> commonIds = new HashSet<>(leftMap.keySet());
        commonIds.retainAll(rightMap.keySet());

        // 为每个匹配的ID创建视图
        for (int id : commonIds) {
            addImagePair(leftMap.get(id), rightMap.get(id), statusMap.get(id));
        }
    }

    private void addImagePair(Bitmap leftBitmap, Bitmap rightBitmap, String statusCode) {
        // Create a container for the group
        LinearLayout groupContainer = new LinearLayout(this);
        LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        groupParams.setMargins(0, dpToPx(8), 0, dpToPx(8)); // Margin for spacing between groups
        groupContainer.setLayoutParams(groupParams);
        groupContainer.setOrientation(LinearLayout.VERTICAL);

        // Determine background color based on status
        int backgroundColor;
        if (statusCode != null && (statusCode.contains("1"))) {
            backgroundColor = Color.parseColor("#FF0000"); // Light Red
        } else {
            backgroundColor = Color.parseColor("#D3D3D3"); // Light Gray
        }
        groupContainer.setBackgroundColor(backgroundColor);

        // Create a container for the row of images
        LinearLayout container = new LinearLayout(this);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setWeightSum(2f);

        // Add left-side image with margin
        ImageView leftImage = createImageView(leftBitmap, 1);
        LinearLayout.LayoutParams leftParams = (LinearLayout.LayoutParams) leftImage.getLayoutParams();
        leftParams.setMargins(dpToPx(8), dpToPx(8), dpToPx(0), dpToPx(8)); // Set left margins
        leftImage.setLayoutParams(leftParams);
        container.addView(leftImage);

        // Create a right-side container
        FrameLayout rightContainer = new FrameLayout(this);
        rightContainer.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
        ));

        // Add right-side image with margin
        ImageView rightImage = new ImageView(this);
        rightImage.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rightImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        rightImage.setImageBitmap(rightBitmap);

        // Set margins for right-side image
        FrameLayout.LayoutParams rightParams = (FrameLayout.LayoutParams) rightImage.getLayoutParams();
        rightParams.setMargins(dpToPx(0), dpToPx(8), dpToPx(8), dpToPx(8)); // Set right margins
        rightImage.setLayoutParams(rightParams);

        rightContainer.addView(rightImage);

        // Status text
        TextView statusText = new TextView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.END;
        statusText.setLayoutParams(params);
        statusText.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        statusText.setBackgroundColor(Color.parseColor("#80000000")); // Semi-transparent
        statusText.setTextColor(Color.WHITE);
        statusText.setText(parseStatusCode(statusCode));

        rightContainer.addView(statusText);
        container.addView(rightContainer);

        // Add the image row to the group container
        groupContainer.addView(container);

        // Finally, add the group to the main container
        imageGroupsContainer.addView(groupContainer);
    }


    private ImageView createImageView(Bitmap bitmap, float weight) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                dpToPx(300),
                weight
        );
        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bitmap);
        return imageView;
    }

    private String parseStatusCode(String code) {
        if (code == null || code.length() < 2) {
            return "状态未知";
        }
        String loose = "";
        String fallOff = "";
        if(code.charAt(0) == '1'&&code.charAt(1) == '1'){
            loose = "松动";
            fallOff = "脱落";
        }
        if(code.charAt(0) == '1'&&code.charAt(1) == '0'){
            loose = "松动";
        }
        if(code.charAt(0) == '0'&&code.charAt(1) == '1'){
            fallOff = "脱落";
        }
        if(code.charAt(0) == '0'&&code.charAt(1) == '0'){
            loose = "正常";
        }
        return String.format("零件状态:\n%s\n%s", loose, fallOff);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}