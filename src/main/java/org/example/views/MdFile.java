package org.example.views;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MdFile extends File {

    private final static Path README_PATH = Paths.get("README.md");
    public final static Path VIEWS_PATH = Paths.get("views");

    private final static int README_IMAGE_NUM = 30;

    private String title;
    private Path path;
    private List<Image> images;

    public MdFile(String title, Path path) throws IOException {
        this(title, path, new ArrayList<>());
    }

    public MdFile(String title, Path path, List<Image> images) throws IOException {
        super(path.toString());
        this.title = "### " + title;
        this.path = path;
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        this.images = images;
        readMdFile();
    }

    public static void writeMdFile(Image image) throws IOException {
        LocalDate imageLocalDate = image.getLocalDate();
        String year = String.valueOf(imageLocalDate.getYear());
        String fileName = String.format("%d-%02d.md", imageLocalDate.getYear(), imageLocalDate.getMonthValue());
        Path path = VIEWS_PATH.resolve(year).resolve(fileName);
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        String title = String.format("必应%d年%02d月壁纸", imageLocalDate.getYear(), imageLocalDate.getMonthValue());
        MdFile mdFile = new MdFile(title, path);
        mdFile.addImage(image);
        mdFile.writeMdFile();
        // 获取README.md文件信息
        MdFile readMeFile = new MdFile("最近壁纸展示", README_PATH);
        readMeFile.addImage(image);
        readMeFile.writeMdFile();
    }

    public static void writeMdFiles(List<Image> images) throws IOException {
        Map<String, MdFile> monthFileMap = new HashMap<>();
        for (Image image : images) {
            LocalDate imageLocalDate = image.getLocalDate();
            int year = imageLocalDate.getYear();
            int month = imageLocalDate.getMonthValue();
            String key = year + "-" + month;
            MdFile mdFile = monthFileMap.get(key);
            if (mdFile != null) {
                mdFile.addImage(image);
                continue;
            }

            // 获取年份的文件夹，不存在则创建
            Path path = VIEWS_PATH.resolve(String.valueOf(year));
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            // 获取月份的md文件，不存在则创建
            path = path.resolve(String.format("%d-%02d.md", year, month));
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            // 获取月份的md文件信息
            mdFile = new MdFile(String.format("必应%d年%02d月壁纸", imageLocalDate.getYear(), imageLocalDate.getMonthValue()), path);
            mdFile.addImage(image);
            monthFileMap.putIfAbsent(key, mdFile);
        }
        for (MdFile mdFile : monthFileMap.values().stream().collect(Collectors.toList())) {
            mdFile.writeMdFile();
        }
        // 获取README.md文件信息
        MdFile readMeFile = new MdFile("最近壁纸展示", README_PATH);
        readMeFile.addImages(images);
        readMeFile.writeMdFile();
    }

    private void readMdFile() throws IOException {
        List<String> lines = Files.readAllLines(path);
        if (lines.size() < 2) {
            return;
        }
        String fitstLine = lines.get(1);
        int line = 3;
        if (!path.equals(README_PATH)) {
            line = 4;
        }
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

    public void writeMdFile() throws IOException {
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
        if (!path.equals(README_PATH)) {
            title = "[<< 返回 README](../../README.md)" + System.lineSeparator() + title;
        }
        Files.write(path, title.getBytes());
        Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
        Files.write(path, context.toString().getBytes(), StandardOpenOption.APPEND);

        if (path.equals(README_PATH)) {
            // README.md显示图片的历史归档
            // 找出所有历史归档
            Stream<Path> pathStream = Files.walk(VIEWS_PATH);
            List<String> list = new ArrayList<>();
            pathStream.filter(pathTemp -> pathTemp.toString().endsWith(".md")).sorted((o1, o2) -> {
                // 按时间进行排序
                String fileName1 = o1.getFileName().toString();
                YearMonth month1 = YearMonth.parse(fileName1.substring(0, fileName1.lastIndexOf(".")));
                String fileName2 = o2.getFileName().toString();
                YearMonth month2 = YearMonth.parse(fileName2.substring(0, fileName2.lastIndexOf(".")));
                return Math.negateExact(month1.compareTo(month2));
            }).forEach(pathTemp -> {
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
                join = join.replace("-01.md) | ", "-01.md)  " + System.lineSeparator());
                Files.write(path, join.getBytes(), StandardOpenOption.APPEND);
            }
            if (images.size() != 0) {
                // 批量下载 README.md
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                Files.write(path, "### 批量壁纸下载".getBytes(), StandardOpenOption.APPEND);
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                String text = "先去[任务页面](https://github.com/wefashe/image-save/actions/workflows/mydown.yml)，点击`Run workflow`，输入下载起始时间，点击<kbd>Run workflow</kbd>按钮执行此下载任务，等待任务的执行完毕，  ";
                Files.write(path, text.getBytes(), StandardOpenOption.APPEND);
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                text = "来到[结果页面](https://github.com/wefashe/image-save/releases/tag/down_zip_tag)查看标题是否包含输入的下载起始日期，包含则说明下载成功，可以直接下载下面的images.zip压缩包  ";
                Files.write(path, text.getBytes(), StandardOpenOption.APPEND);
                Files.write(path, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
            }
        }
    }

    public void addImages(List<Image> images) throws IOException {
        // 排除重复
        images.removeAll(this.images);
        if (!images.isEmpty()) {
            this.images.addAll(images);
        }
    }

    public void addImage(Image image) throws IOException {
        if (!this.images.contains(image)) {
            this.images.add(image);
        }
    }

    public List<Image> getImages() {
        return images;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MdFile mdFile = (MdFile) obj;
        return Objects.equals(this.getPath(), mdFile.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPath());
    }
}
