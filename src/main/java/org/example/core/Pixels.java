package org.example.core;

/**
 * 已知分辨率
 */
public enum Pixels {

    // 超高清UHD 8k  必应壁纸不一定存在
    PIX_UHD_7680X4320("UHD",7680,4320),
    // 超高清UHD 4K 必应壁纸不一定存在
    PIX_UHD_3840X2160("UHD",3840,2160),
    // 全高清FHD
    PIX_1920X1200("1920x1200", 1920, 1200),
    // 高清HD 1080P
    PIX_1920X1080("1920x1080",1920,1080),
    PIX_1366X768("1366x768", 1366, 768),
    PIX_1280X768("1280x768",1280,768),
    // 标清SD 720P
    PIX_1280X720("1280x720",1280,720),
    // 手机高清 1080x1920
    PIX_11080X1920("1080x1920",1080,1920),
    PIX_1024X768("1024x768",1024,768),
    PIX_800X600("800x600",800,600),
    PIX_800X480("800x480",800,480),
    PIX_768X1280("768x1280",768,1280),
    PIX_720X1280("720x1280",720,1280),
    PIX_640X480("640x480",640,480),
    PIX_480X800("480x800",480,800),
    PIX_400X240("400x240",400,240),
    PIX_320X240("320x240",320,240),
    PIX_240X320("240x320",240,320);

    private String resolution;
    private int width;
    private int height;

    Pixels(String resolution, int width, int height) {
        this.resolution = resolution;
        this.width = width;
        this.height = height;
    }

    public static Pixels getPixel(int width, int height) {
        Pixels[] pixels = Pixels.values();
        for (Pixels pixel : pixels) {
            if (pixel.getHeight() <= height && pixel.getWidth() <= width) {
                return pixel;
            }
        }
        if (PIX_UHD_7680X4320.getWidth() < width && PIX_UHD_7680X4320.getHeight() < height) {
            return PIX_UHD_7680X4320;
        }
        return PIX_UHD_3840X2160;
    }


    public String getResolution() {
        return resolution;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
