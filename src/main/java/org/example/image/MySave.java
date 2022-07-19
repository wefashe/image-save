package org.example.image;

import java.io.IOException;

public class MySave {
    public static void main(String[] args) throws IOException {
        MdFile md = new MdFile(MdFile.README_PATH);
        md.addImage(Image.getTodayImage());
        md.writeMdFile();
    }
}
