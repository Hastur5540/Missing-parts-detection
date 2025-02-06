package com.example.missingpartsdetection.entity;

import java.io.Serializable;

public class Device implements Serializable {
    private String id;            // 设备号
    private String photoPath;

    // 构造函数
    public Device(String id, String photoPath) {
        this.id = id;
        this.photoPath = photoPath;
    }

    // Getter 和 Setter 方法

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Device)) return false;
        Device other = (Device) obj;
        return this.id.equals(other.id); // 根据唯一字段比较
    }

    @Override
    public int hashCode() {
        return id.hashCode(); // 使用唯一字段生成 hashCode
    }

    @Override
    public String toString() {
        return " (设备号: " + id + ")";
    }
}