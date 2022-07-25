package org.example.image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
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
        // 获取README.md文件信息
        return new MdFile("必应每日壁纸", README_PATH);
    }

    public static MdFile getCurrentMonthFile() throws IOException {
        // 获取当前北京时间
        LocalDate now = LocalDate.now(ZoneId.of("UTC+8"));
        // 获取当前年份的文件夹，不存在则创建
        Path path = IMAGES_PATH.resolve(String.valueOf(now.getYear()));
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        // 获取当前月份的md文件，不存在则创建
        path = path.resolve(String.format("%d-%02d.md", now.getYear(), now.getMonthValue()));
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        // 获取当前月份的md文件信息
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
                String name = text.substring(text.lastIndexOf("[") + 1, text.lastIndexOf("]"));
                String maxPixelUrl = text.substring(text.lastIndexOf("/"), text.lastIndexOf(")"));
                markdownText = markdownText.substring(0, markdownText.indexOf("<br/>"));
                String alt = markdownText.substring(markdownText.indexOf("/", 20), markdownText.indexOf("&"));
                text = markdownText.substring(markdownText.indexOf("(") + 1, markdownText.lastIndexOf("]") - 1);
                String url = text.substring(text.indexOf("/", 15), text.indexOf("&"));
                String title = text.substring(text.indexOf("\"") + 1, text.indexOf("&#10;"));
                String desc = text.substring(text.indexOf("&#10;") + 5, text.lastIndexOf("\"")).replace("&#10;", " (") + ")";
                String link = markdownText.substring(markdownText.lastIndexOf("/"), markdownText.lastIndexOf(")"));
                LocalDate imageDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
                Image image = new Image(imageDate.format(DateTimeFormatter.BASIC_ISO_DATE), url, title, desc, alt, link);
                image.put("name", name);
                image.put("maxPixelUrl", maxPixelUrl);
                images.add(image);
            }
        }
    }

    private void writeMdFile() throws IOException {
        if (images.isEmpty()) {
            return;
        }
        Collections.sort(images);
        images = images.stream().distinct().collect(Collectors.toList());
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < images.size(); i++) {
            if (i >= README_IMAGE_NUM && path.equals(README_PATH)) {
                // 限制README.md显示图片数量
                break;
            }
            Image image = images.get(i);
            if (i == 0 && context.length() == 0 && image.isToday() && path.equals(README_PATH)) {
                // README.md顶部展示今天最新图片
                context.append("||").append(System.lineSeparator());
                context.append("|:---:|").append(System.lineSeparator());
                context.append("|").append(image.getTopMarkdownText()).append("|").append(System.lineSeparator());
                context.append(System.lineSeparator());
            }

            if (i == 0) {
                context.append("||||").append(System.lineSeparator());
                context.append("|:---:|:---:|:---:|").append(System.lineSeparator());
            }
            if ((i + 1) % 3 == 0) {
                // 一排展示3张图片
                context.append("|").append(image.getMarkdownText()).append("|").append(System.lineSeparator());
            } else {
                context.append("|").append(image.getMarkdownText());
            }
        }
        Files.write(path, title.getBytes());
        Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
        Files.write(path, context.toString().getBytes(), StandardOpenOption.APPEND);

        if (path.equals(README_PATH)) {
            // README.md显示图片的历史归档
            // 找出所有历史归档
            Stream<Path> pathStream = Files.walk(IMAGES_PATH);
            List<String> list = new ArrayList<>();
            pathStream.filter(pathTemp -> pathTemp.toString().endsWith(".md")).sorted((o1, o2) -> {
                // 按时间进行排序
                String fileName1 = o1.getFileName().toString();
                YearMonth month1 = YearMonth.parse(fileName1.substring(0, fileName1.lastIndexOf(".")));
                String fileName2 = o2.getFileName().toString();
                YearMonth month2 = YearMonth.parse(fileName2.substring(0, fileName2.lastIndexOf(".")));
                return Math.negateExact(month1.compareTo(month2));
            }).limit(HISTORY_ARCHIVE_NUM).forEach(pathTemp -> {
                // 限制展示个数，格式化展示格式
                String name = pathTemp.getFileName().toString();
                list.add(String.format("[%s](%s)", name.substring(0, name.lastIndexOf(".")), pathTemp.toString().replace("\\", "/")));
            });
            if (!list.isEmpty()) {
                // 归档信息写入README.md
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                Files.write(path, "### 历史壁纸归档".getBytes(), StandardOpenOption.APPEND);
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                String join = String.join(" | ", list);
                Files.write(path, join.getBytes(), StandardOpenOption.APPEND);
            }

        }
    }

    public void addImages(List<Image> images) throws IOException {
        // 排除重复
        images.removeAll(this.images);
        if (!images.isEmpty()) {
            this.images.addAll(images);
            writeMdFile();
        }
    }

    public void addImage(Image image) throws IOException {
        if (!this.images.contains(image)) {
            this.images.add(image);
            writeMdFile();
        }
    }
}
