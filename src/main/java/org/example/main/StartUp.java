package org.example.main;

import org.apache.commons.io.FileUtils;
import org.example.views.Image;
import org.example.views.MdFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StartUp {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            // 保存今天壁纸
            saveTodayImage2File();
            // 按条件保存壁纸
            // saveImages2File(0, 8);
            // test();
        } else if (args.length == 2) {
            download(args[0], args[1]);
        }
    }

    private static void download(String start, String end) throws IOException {
        LocalDate fromDate = LocalDate.parse(start.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        YearMonth fromYearMonth = YearMonth.of(fromDate.getYear(), fromDate.getMonthValue());
        LocalDate toDate = LocalDate.parse(end.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        YearMonth toYearMonth = YearMonth.of(toDate.getYear(), toDate.getMonthValue());
        List<MdFile> mdFiles = new ArrayList<>();
        while (!fromYearMonth.isAfter(toYearMonth)) {
            Path path = MdFile.VIEWS_PATH.resolve(String.valueOf(fromYearMonth.getYear()));
            if (!Files.exists(path)) {
                fromYearMonth = fromYearMonth.plusYears(1);
                continue;
            }
            path = path.resolve(String.format("%d-%02d.md", fromYearMonth.getYear(), fromYearMonth.getMonthValue()));
            if (Files.exists(path)) {
                mdFiles.add(new MdFile(String.format("必应%d年%02d月壁纸", fromYearMonth.getYear(), fromYearMonth.getMonthValue()), path));
            }
            fromYearMonth = fromYearMonth.plusMonths(1);
        }
        List<Image> images = new ArrayList<>();
        for (MdFile mdFile : mdFiles) {
            List<Image> monthImages = mdFile.getImages();
            for (Image image : monthImages) {
                if (!fromDate.isAfter(image.getLocalDate()) && !toDate.isBefore(image.getLocalDate())) {
                    images.add(image);
                }
            }
        }
        Path DOWN_PATH = Paths.get("target").resolve("images");
        if (Files.exists(DOWN_PATH)) {
            Files.walkFileTree(DOWN_PATH, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return super.postVisitDirectory(dir, exc);
                }
            });
        }
        Files.createDirectories(DOWN_PATH);
        if (images.size() > 0) {
            List<String> fileNames = new ArrayList<>();
            int threadSize = Math.min(images.size(), Runtime.getRuntime().availableProcessors() * 2 + 1);
            ExecutorService es = Executors.newFixedThreadPool(threadSize);
            ArrayBlockingQueue<Image> queue = new ArrayBlockingQueue<>(images.size());
            queue.addAll(images);

            for (int i = 0; i < threadSize; i++) {
                es.execute(() -> {
                    while (!queue.isEmpty()) {
                        Image image = null;
                        String name = null;
                        try {
                            image = queue.poll(30, TimeUnit.MINUTES);
                            LocalDate localDate = image.getLocalDate();
                            // 获取年份的文件夹，不存在则创建
                            Path path = DOWN_PATH.resolve(String.valueOf(localDate.getYear()));
                            // 获取月份的md文件，不存在则创建
                            path = path.resolve(localDate.getYear() + "-" + localDate.getMonthValue());
                            name = String.format("%02d-%s", image.getLocalDate().getDayOfMonth(), image.getName());
                            path = path.resolve(name);
                            FileUtils.copyURLToFile(new URL(image.getFullMaxPixelUrl()), path.toFile());
                        } catch (Exception e) {
                            fileNames.add(image.getDate() + "    " + name);
                        }
                    }
                });
            }
            // 执行子线程
            es.shutdown();
            try {
                while (!es.awaitTermination(100, TimeUnit.MINUTES)) {
                    // 超时的时候向线程池中所有的线程发出中断
                    es.shutdownNow();
                }
            } catch (InterruptedException e) {
                es.shutdownNow();
            }
            Path TEXT_PATH = DOWN_PATH.resolve("说明.txt");
            Files.createFile(TEXT_PATH);
            String text = "下载范围 " + fromDate.toString() + "  " + toDate;
            Files.write(TEXT_PATH, text.getBytes(), StandardOpenOption.APPEND);
            Files.write(TEXT_PATH, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
            text = "下载总数 " + images.size() + ", 成功 " + (images.size() - fileNames.size()) + " , 失败 " + fileNames.size();
            Files.write(TEXT_PATH, text.getBytes(), StandardOpenOption.APPEND);
            if (fileNames.size() != 0) {
                Files.write(TEXT_PATH, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                Files.write(TEXT_PATH, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                text = "以下为失败列表：";
                Files.write(TEXT_PATH, text.getBytes(), StandardOpenOption.APPEND);
                Files.write(TEXT_PATH, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                String join = String.join(System.lineSeparator(), fileNames);
                Files.write(TEXT_PATH, join.getBytes(), StandardOpenOption.APPEND);
                Files.write(TEXT_PATH, System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                text = "请进行单独下载！";
                Files.write(TEXT_PATH, text.getBytes(), StandardOpenOption.APPEND);
            }
            File file = new File("images.zip");
            if (file.exists()) {
                file.delete();
            }
            //压缩结果输出，即压缩包
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(DOWN_PATH.toString());
            //递归压缩文件夹
            zipFile(fileToZip, fileToZip.getName(), zipOut);
            //关闭输出流
            zipOut.close();
            fos.close();

            Files.walkFileTree(DOWN_PATH, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return super.postVisitDirectory(dir, exc);
                }
            });

        }


    }

    /**
     * 将fileToZip文件夹及其子目录文件递归压缩到zip文件中
     *
     * @param fileToZip 递归当前处理对象，可能是文件夹，也可能是文件
     * @param fileName  fileToZip文件或文件夹名称
     * @param zipOut    压缩文件输出流
     * @throws IOException
     */
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        //不压缩隐藏文件夹
        if (fileToZip.isHidden()) {
            return;
        }
        //判断压缩对象如果是一个文件夹
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                //如果文件夹是以“/”结尾，将文件夹作为压缩箱放入zipOut压缩输出流
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                //如果文件夹不是以“/”结尾，将文件夹结尾加上“/”之后作为压缩箱放入zipOut压缩输出流
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            //遍历文件夹子目录，进行递归的zipFile
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            //如果当前递归对象是文件夹，加入ZipEntry之后就返回
            return;
        }
        //如果当前的fileToZip不是一个文件夹，是一个文件，将其以字节码形式压缩到压缩包里面
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
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

    public static void test() throws IOException {
        List<Image> images = new ArrayList<>();
        for (int i = 41; i <= 45; i++) {
            String path = "http://www.sowang.com/bbs/forum.php?mod=forumdisplay&fid=67&page=%s";
            Document doc = Jsoup.connect(String.format(path, i)).get();

            Elements elements = doc.select("#threadlisttableid tbody[id^=normalthread] th a.xst");
            for (Element element : elements) {
                doc = Jsoup.connect("http://www.sowang.com/bbs/" + element.attr("href")).get();
                Element threadSubject = doc.getElementById("thread_subject");
                String subject = threadSubject.text();
                subject = subject.substring(subject.length() - 10);
                String date = subject.substring(subject.indexOf("20")).trim();
                if (date.length() == 9) {
                    date = date.substring(0, 6) + date.substring(7);
                }
                System.out.println(date);
                // if (!"20190308".equals(date)) {
                //     continue;
                // }
                Elements all = doc.select(".pcb td");
                String url = all.select("img").first().attr("src");
                if (url.indexOf("&") != -1) {
                    url = url.substring(url.indexOf("/", 15), url.indexOf("&"));
                } else {
                    url = url.substring(url.indexOf("/", 15));
                }
                String text = all.first().wholeText();
                String[] texts = text.split("\r\n");
                String desc = "";
                for (int j = 0; j < texts.length; j++) {
                    String str = texts[j];
                    if (str.contains("img")) {
                        continue;
                    }
                    if (str != null && !str.isEmpty()) {
                        str = str.replace("@", " © ");
                        desc = str;
                        break;
                    }
                }
                if (desc == null || desc.isEmpty()) {
                    System.out.println(date + " desc为空");
                }
                desc = desc.replace("必应壁纸：", "");
                desc = desc.replace("（", "");
                desc = desc.replace("）", "");
                desc = desc.replace(">", "");
                desc = desc.replace("<", "");
                desc = desc.replaceAll("\u00a0", " ").replaceAll(" +", " ");
                if (!desc.contains(" (©")) {
                    desc = desc.replace(" ©", " (©") + ")";
                }
                System.out.println(desc);
                System.out.println(url);
                String alt = url.substring(0, url.lastIndexOf("_"));
                String link = "/search?q=%s&form=hpcapt&mkt=zh-cn&filters=HpDate:\"%s_1600\"";
                String encodeToString = URLEncoder.encode(desc.substring(0, desc.indexOf(" ")), "UTF-8");
                link = String.format(link, encodeToString, date);
                System.out.println(link);
                System.out.println();
                images.add(new Image(date, url, " ", desc, alt, link));
            }
        }
        MdFile.writeMdFiles(images);
    }
}
