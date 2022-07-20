package org.example.image;

import java.io.IOException;
import java.util.List;

public class MySave {
    public static void main(String[] args) throws IOException {
        // 保存今天壁纸
        saveTodayImage();
        // 按条件保存壁纸
        // saveSevenImage(0, 8);
    }

    public static void saveTodayImage() throws IOException {
        MdFile readMeFile = MdFile.getReadMeFile();
        MdFile imagesFile = MdFile.getImagesFile();
        Image todayImage = Image.getTodayImage();
        readMeFile.addImage(todayImage);
        readMeFile.writeMdFile();
        imagesFile.addImage(todayImage);
        imagesFile.writeMdFile();
    }

    public static void saveSevenImage(int idx,int num) throws IOException {
        MdFile readMeFile = MdFile.getReadMeFile();
        MdFile imagesFile = MdFile.getImagesFile();
        List<Image> images = Image.getImages(idx, num);
        readMeFile.setImages(images);
        imagesFile.setImages(images);
        readMeFile.writeMdFile();
        imagesFile.writeMdFile();
    }
}
