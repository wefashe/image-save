package org.example.image;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
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
        String path = "http://www.sowang.com/bbs/forum.php?mod=forumdisplay&fid=67&page=2";
        Document doc = Jsoup.connect(path).get();

        Elements elements = doc.select("#threadlisttableid th a.xst");
        for (Element element : elements) {
            doc = Jsoup.connect("http://www.sowang.com/bbs/" + element.attr("href")).get();
            Element threadSubject = doc.getElementById("thread_subject");
            String subject = threadSubject.text();
            String date = subject.substring(subject.lastIndexOf(" ")).trim();
            // if (!"20220614".equals(date)) {
            //     continue;
            // }
            System.out.println(date);
            Elements all = doc.select(".pcb td");
            String url = all.select("img").first().attr("src");
            url = url.substring(url.indexOf("/", 15), url.indexOf("&"));
            String text = all.first().wholeText();
            String[] texts = text.split("\r\n");
            String desc = texts[3];
            desc = desc.replaceAll("\u00a0", " ").replaceAll(" +", " ").replace(" ©", " (©")+")";
            System.out.println(desc);
            System.out.println(url);
            String link = "/search?q=%s&form=hpcapt&mkt=zh-cn&filters=HpDate:\"%s_1600\"";
            String encodeToString = URLEncoder.encode(desc.substring(0, desc.indexOf(" ")),"UTF-8");
            link = "https://cn.bing.com" + String.format(link, encodeToString, date);
            System.out.println(link);
            System.out.println();
        }
    }
}
