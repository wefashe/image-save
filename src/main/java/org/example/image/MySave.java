package org.example.image;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MySave {
    public static void main(String[] args) throws IOException {
        // 保存今天壁纸
        saveTodayImage2File();
        // 按条件保存壁纸
        // saveImages2File(0, 8);
        // test();
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
        for (int i = 14; i <= 20; i++) {
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
                // if (!"20210525".equals(date)) {
                //     continue;
                // }
                Elements all = doc.select(".pcb td");
                String url = all.select("img").first().attr("src");
                url = url.substring(url.indexOf("/", 15), url.indexOf("&"));
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
                    desc = desc.replace(" ©", " (©")+")";
                }
                System.out.println(desc);
                System.out.println(url);
                String alt = url.substring(0, url.lastIndexOf("_"));
                String link = "/search?q=%s&form=hpcapt&mkt=zh-cn&filters=HpDate:\"%s_1600\"";
                String encodeToString = URLEncoder.encode(desc.substring(0, desc.indexOf(" ")),"UTF-8");
                link = String.format(link, encodeToString, date);
                System.out.println(link);
                System.out.println();
                images.add(new Image(date, url, " ", desc, alt, link));
            }
        }
        MdFile.writeMdFiles(images);
    }
}
