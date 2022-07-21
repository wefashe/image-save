package org.example.image;

/**
 * 已知分辨率
 */
public enum Pixels {
    // 超高清4K 3840×2160(4K×2K)及以上
    PIX_UHD("UHD"),
    // 全高清 1920×1080
    PIX_1920X1200("1920x1200"),
    PIX_1920X1080("1920x1080"),
    PIX_1366X768("1366x768"),
    PIX_1280X768("1280x768"),
    // 手机高清 1080x1920
    PIX_11080X1920("1080x1920"),
    PIX_1024X768("1024x768"),
    PIX_800X600("800x600"),
    PIX_800X480("800x480"),
    PIX_768X1280("768x1280"),
    PIX_720X1280("720x1280"),
    PIX_640X480("640x480"),
    PIX_480X800("480x800"),
    PIX_400X240("400x240"),
    PIX_320X240("320x240"),
    PIX_240X320("240x320");

    private String resolution;

    Pixels(String resolution) {
        this.resolution = resolution;
    }

    public String getResolution() {
        return resolution;
    }
}
