package org.example.image;

import java.io.IOException;
import java.util.List;

public class MySave {
    public static void main(String[] args) throws IOException {
        // 保存今天壁纸
        saveTodayImage2File();
        // 按条件保存壁纸
        // saveImages2File(0, 8);
    }

    /**
     * 今天的壁纸保存到md文件中
     *
     * @throws IOException
     */
    public static void saveTodayImage2File() throws IOException {
        // 获取今天的壁纸
        Image todayImage = Image.getTodayImage();
        // 今天的壁纸添加到md文件中
        MdFile.writeMdFile(todayImage);
    }

    /**
     * 按条件获取壁纸保存到md文件中
     *
     * @param idx 范围0~7， 0 今天；1 昨天；2 前天；...
     * @param num 范围1-8  获取日期往后n天的壁纸
     * @throws IOException
     */
    public static void saveImages2File(int idx, int num) throws IOException {
        // 按条件获取壁纸
        List<Image> images = Image.getImages(idx, num);
        // 壁纸添加到md文件中
        MdFile.writeMdFiles(images);
    }
}
