package org.example.image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MdFile extends File {

    private final static Path README_PATH = Paths.get("README.md");
    private final static Path IMAGES_PATH = Paths.get("images/");

    private final static int README_IMAGE_NUM = 30;
    private final static int HISTORY_ARCHIVE_NUM = 24;

    private String title;
    private Path path;
    private List<Image> images;

    public MdFile(String title, Path path) throws IOException {
        this(title, path, new ArrayList<>());
    }

    public MdFile(String title, Path path, List<Image> images) throws IOException {
        super(path.toString());
        this.title = "## " + title;
        this.path = path;
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        this.images = images;
        readMdFile();
    }

    public static MdFile getReadMeFile() throws IOException {
        return new MdFile("必应每日壁纸", README_PATH);
    }

    public static MdFile getImagesFile() throws IOException {
        LocalDate now = LocalDate.now();
        Path path = IMAGES_PATH.resolve(String.valueOf(now.getYear()));
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        path = path.resolve(String.format("%d-%02d.md", now.getYear(), now.getMonthValue()));
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        return new MdFile(String.format("必应%d年%02d月壁纸", now.getYear(), now.getMonthValue()), path);
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
            if (content.startsWith("###")) {
                break;
            }
            String[] markdownTexts = content.split("\\|");
            for (String markdownText : markdownTexts) {
                if (markdownText.isEmpty()) {
                    continue;
                }
                String text = markdownText.substring(markdownText.indexOf("<center>") + 8, markdownText.indexOf("<center/>"));
                String date = text.substring(0, text.indexOf(" "));
                markdownText = markdownText.substring(0, markdownText.indexOf("<br/>"));
                String alt = markdownText.substring(markdownText.indexOf("[", 2) + 1, markdownText.indexOf("&"));
                text = markdownText.substring(markdownText.indexOf("(") + 1, markdownText.lastIndexOf("]") - 1);
                String url = text.substring(0, text.indexOf("&"));
                String title = text.substring(text.indexOf("\"")+1, text.indexOf("&#10;"));
                String desc = text.substring(text.indexOf("&#10;")+5, text.lastIndexOf("\""));
                System.out.println(markdownText);
                String link = markdownText.substring(markdownText.lastIndexOf("]") + 2, markdownText.lastIndexOf(")"));
                LocalDate imageDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
                images.add(new Image(imageDate.format(DateTimeFormatter.BASIC_ISO_DATE), url, title, desc, alt, link));
            }
        }
        images = images.stream().distinct().collect(Collectors.toList());
        Collections.sort(images);
    }

    public void writeMdFile() throws IOException {
        if (images.isEmpty()) {
            return;
        }
        Collections.sort(images);
        images = images.stream().distinct().collect(Collectors.toList());
        StringBuilder otherContext = new StringBuilder();
        StringBuilder todayContext = new StringBuilder();
        for (int i = 0; i < images.size(); i++) {
            if (i >= README_IMAGE_NUM) {
                break;
            }
            Image image = images.get(i);
            if (i == 0 && image.isToday() && !path.startsWith(IMAGES_PATH) && todayContext.length() == 0) {
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
        Files.write(path, title.getBytes());
        Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
        Files.write(path, todayContext.toString().getBytes(), StandardOpenOption.APPEND);

        if (path.equals(README_PATH)) {
            Stream<Path> pathStream = Files.walk(IMAGES_PATH);
            List<String> list = new ArrayList<>();
            pathStream.filter(pathTemp -> pathTemp.toString().endsWith(".md")).sorted((o1, o2) -> {
                String fileName1 = o1.getFileName().toString();
                YearMonth month1 = YearMonth.parse(fileName1.substring(0, fileName1.lastIndexOf(".")));
                String fileName2 = o2.getFileName().toString();
                YearMonth month2 = YearMonth.parse(fileName2.substring(0, fileName2.lastIndexOf(".")));
                return Math.negateExact(month1.compareTo(month2));
            }).limit(HISTORY_ARCHIVE_NUM).forEach(pathTemp -> {
                String name = pathTemp.getFileName().toString();
                list.add(String.format("[%s](%s)", name.substring(0, name.lastIndexOf(".")), pathTemp.toString()));
            });
            if (!list.isEmpty()) {
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                Files.write(path, "### 历史归档:".getBytes(), StandardOpenOption.APPEND);
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                String join = String.join(" | ", list);
                Files.write(path, join.getBytes(), StandardOpenOption.APPEND);
            }

        }
    }

    public void setImages(List<Image> images) {
        this.images.addAll(images);
        this.images = this.images.stream().distinct().collect(Collectors.toList());
    }

    public void addImage(Image image) {
        if (!images.contains(image)) {
            this.images.add(image);
        }
    }
}
