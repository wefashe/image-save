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
     * 今天的图片保存到md文件中
     *
     * @throws IOException
     */
    public static void saveTodayImage2File() throws IOException {
        // 获取README.md文件
        MdFile readMeFile = MdFile.getReadMeFile();
        // 获取当前月份的md文件
        MdFile currentMonthFile = MdFile.getCurrentMonthFile();
        // 获取今天的图片
        Image todayImage = Image.getTodayImage();
        // 今天的图片添加到README.md文件中
        readMeFile.addImage(todayImage);
        // 今天的图片添加到当前月份的md文件中
        currentMonthFile.addImage(todayImage);
    }

    /**
     * 按条件获取图片保存到md文件中
     *
     * @param idx 范围0~7， 0 今天；1 昨天；2 前天；...
     * @param num 范围1-8  获取日期往后n天的图片
     * @throws IOException
     */
    public static void saveImages2File(int idx, int num) throws IOException {
        // 获取README.md文件
        MdFile readMeFile = MdFile.getReadMeFile();
        // 获取当前月份的md文件
        MdFile currentMonthFile = MdFile.getCurrentMonthFile();
        // 按条件获取图片
        List<Image> images = Image.getImages(idx, num);
        // 图片添加到README.md文件中
        readMeFile.addImages(images);
        // 图片添加到当前月份的md文件中
        currentMonthFile.addImages(images);
    }
}
