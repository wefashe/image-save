package org.example.image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MdFile extends File {

    public static Path README_PATH = Paths.get("README.md");
    public static Path IMAGES_PATH = Paths.get("images/");

    private Path path;
    private List<Image> images;

    public MdFile(String pathname) throws IOException {
        this(Paths.get(pathname), new ArrayList<>());
        readMdFile();
    }

    public MdFile(Path path) throws IOException {
        this(path, new ArrayList<>());
        readMdFile();
    }

    public MdFile(Path path, List<Image> images) throws IOException {
        super(path.toString());
        this.path = path;
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        this.images = images;
    }

    private void readMdFile() throws IOException {
        List<String> lines = Files.readAllLines(path);
        if (lines.size() < 2) {
            return;
        }
        String fitstLine = lines.get(1);
        int line = 3;
        if ("||".equals(fitstLine)) {
            line = 7;
        }
        for (int i = line; i < lines.size(); i++) {
            String content = lines.get(i);
            String[] markdownTexts = content.split("\\|");
            for (String markdownText : markdownTexts) {
                if (markdownText.isEmpty()) {
                    continue;
                }
                String date = markdownText.substring(markdownText.indexOf("<center>") + 8, markdownText.indexOf("<center/>"));
                markdownText = markdownText.substring(markdownText.indexOf("[")+1, markdownText.lastIndexOf("]"));
                String title = markdownText.substring(markdownText.indexOf("[") + 1, markdownText.lastIndexOf("]"));
                markdownText = markdownText.substring(markdownText.lastIndexOf("]")+2, markdownText.lastIndexOf(")"));
                String url = markdownText.substring(0, markdownText.indexOf(" "));
                String desc = markdownText.substring(markdownText.indexOf("\"")+1, markdownText.lastIndexOf("\""));
                LocalDate imageDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
                images.add(new Image(imageDate.format(DateTimeFormatter.BASIC_ISO_DATE), url, title, desc));
            }
        }
        images = images.stream().distinct().collect(Collectors.toList());
        Collections.sort(images);
    }

    public void writeMdFile() throws IOException {
        if (images.isEmpty()) {
            return;
        }
        images = images.stream().distinct().collect(Collectors.toList());
        Collections.sort(images);
        StringBuilder otherContext = new StringBuilder();
        StringBuilder todayContext = new StringBuilder();
        for (int i = 0; i < images.size(); i++) {
            Image image = images.get(i);
            if (image.isToday()) {
                todayContext.append("||\n");
                todayContext.append("|:---:|\n");
                todayContext.append("|").append(image.getTopMarkdownText()).append("|\n");
            }

            if (i == 0) {
                otherContext.append("||||\n");
                otherContext.append("|:---:|:---:|:---:|\n");
            }
            if ((i + 1) % 3 == 0) {
                otherContext.append("|").append(image.getMarkdownText()).append("|\n");
            } else {
                otherContext.append("|").append(image.getMarkdownText());
            }
        }
        if (todayContext.length() != 0) {
            todayContext.append("\n");
        }
        todayContext.append(otherContext);
        String title = "## 必应每日壁纸";
        Files.write(path, title.getBytes());
        Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
        Files.write(path,todayContext.toString().getBytes(), StandardOpenOption.APPEND);
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images.stream().distinct().collect(Collectors.toList());
    }

    public void addImage(Image image) {
        if (!images.contains(image)) {
            this.images.add(image);
        }
    }
}
