package org.example.image;

import java.io.IOException;

public class MySave {
    public static void main(String[] args) throws IOException {
        MdFile readMeFile = MdFile.getReadMeFile();
        MdFile imagesFile = MdFile.getImagesFile();

        Image todayImage = Image.getTodayImage();

        readMeFile.addImage(todayImage);
        readMeFile.writeMdFile();

        imagesFile.addImage(todayImage);
        imagesFile.writeMdFile();
    }
}
