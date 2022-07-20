package org.example.image;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Image implements Comparable<Image>{

    // BING API
    // idx 0 今天，1 昨天，2 前日...
    // n 返回的图片个数 最大为8
    public static String BING_API = "https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&nc=1612409408851&pid=hp&FORM=BEHPTB&uhd=1&uhdwidth=3840&uhdheight=2160";

    private static String BING_URL = "https://cn.bing.com";

    private String date;
    private String url;
    private String title;
    private String desc;

    public static Image getTodayImage() throws IOException {
        String jsonText = IOUtils.toString(URI.create(BING_API), StandardCharsets.UTF_8);
        JSONObject obj = (JSONObject) JSON.parseObject(jsonText).getJSONArray("images").get(0);
        return new Image(obj);
    }

    public Image(JSONObject obj) {
        this((String) obj.get("enddate"), (String) obj.get("url"), (String) obj.get("title"), (String) obj.get("copyright"));
    }

    public Image(String date, String url, String title, String desc) {
        this.date = date;
        if (!url.contains("http")) {
            url = BING_URL + url;
        }
        this.url = url;
        this.title = escape(title);
        this.desc = escape(desc);
    }

    private String escape(String str){
        // markdown 字符转义
        if (str.indexOf("\"")!=-1&&str.indexOf("\\\"")==-1) {
            return str.replace("\"", "\\\"");
        }
        return str;
    }

    public String getDate() {
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getUrl() {
        // _1920x1080.jpg 高清
        // _UHD.jpg 超高清4K
        return url;
    }

    public String getWith1000Url() {
        return url + "&w=1000";
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isToday(){
        LocalDate imageDate = LocalDate.parse(date,DateTimeFormatter.BASIC_ISO_DATE);
        return LocalDate.now().equals(imageDate);
    }

    public String getTopMarkdownText() {
        StringBuilder content = new StringBuilder();
        content.append("[![").append(getTitle()).append("](");
        content.append(getWith1000Url());
        content.append(" \"").append(getDesc()).append("\"").append(")]");
        content.append("(").append(getUrl()).append(")<br>");
        content.append("<center>**").append(getDate()).append("**<center/>");
        return content.toString();
    }

    public String getMarkdownText() {
        StringBuilder content = new StringBuilder();
        content.append("[![").append(getTitle()).append("](");
        content.append(getUrl());
        content.append(" \"").append(getDesc()).append("\"").append(")]");
        content.append("(").append(getUrl()).append(")<br>");
        content.append("<center>").append(getDate()).append("<center/>");
        return content.toString();
    }

    @Override
    public int compareTo(Image image) {
        LocalDate imageDate1 = LocalDate.parse(this.date, DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate imageDate2 = LocalDate.parse(image.date, DateTimeFormatter.BASIC_ISO_DATE);
        return Math.negateExact(imageDate1.compareTo(imageDate2));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Image image = (Image)obj;
        return Objects.equals(getDesc(), image.getDesc()) && Objects.equals(getDate(), image.getDate())
               && Objects.equals(getUrl(), image.getUrl())&&Objects.equals(getTitle(), image.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(desc, date, url, title);
    }

    @Override
    public String toString() {
        String format = this.date + this.title + "\n" + this.url + "\n" + this.getDesc() + "\n";
        System.out.println(format);
        return format;
    }
}
