package org.example.image;

import org.example.api.BingApi;
import org.example.db.Wallpaper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Image implements Comparable<Image> {

    private String date;
    private String url;
    private String title;
    private String desc;
    private String alt;
    private String link;

    /**
     * 最大分辨率的图片信息
     */
    Map<String, String> maxPixelUrlMap;

    public Image(String date, String url, String title, String desc, String alt, String link) {
        this.date = date;
        if (url.contains("&")) {
            url = url.substring(0, url.indexOf("&"));
        }
        this.url = url;
        this.title = escape(title);
        this.desc = escape(desc);
        this.alt = alt;
        this.link = link;
        this.maxPixelUrlMap = new HashMap<>();
    }

    public LocalDate getLocalDate() {
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
    }

    public String getDate() {
        LocalDate localDate = getLocalDate();
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getUrl() {
        // 默认超高清图片
        return url;
    }

    public String getPrefixUrl() {
        // 图片地址的分辨率和长宽可能会变，取前缀来确保唯一
        return url.substring(0, url.lastIndexOf("_"));
    }

    public void put(String key, String value) {
        this.maxPixelUrlMap.put(key, value);
    }

    /**
     * 通过分辨率获取图片下载的地址
     *
     * @param pixel
     * @return
     */
    public String getUrlByPixle(Pixels pixel) {
        // 设置图片的分辨率
        String suffix = "UHD.jpg";
        String resolution = pixel.getResolution();
        if (resolution != null && !resolution.isEmpty()) {
            suffix = resolution + ".jpg";
        }
        int width = pixel.getWidth();
        if (width != 0) {
            suffix += "&w=" + width;
        }
        int height = pixel.getHeight();
        if (height != 0) {
            suffix += "&h=" + height;
        }
        return url.replaceAll("[^_]+.jpg", suffix);
    }

    private static String makeFullUrl(String url) {
        return BingApi.BING_URL_PREFIX + url;
    }

    public Map<String, String> getMaxPixelUrl() {
        String fullMaxPixelUrl = url.replaceAll("[^_]+.jpg", "UHD.jpg");
        Map<String, String> pixelUrlMap = new HashMap<>();
        BufferedImage image = null;
        try {
            URL imageURL = URI.create(makeFullUrl(fullMaxPixelUrl)).toURL();
            image = ImageIO.read(imageURL);
            int width = image.getWidth();
            int height = image.getHeight();
            pixelUrlMap.put("name", "超高清");
            pixelUrlMap.put("maxPixelUrl", fullMaxPixelUrl);
            if (width >= 3840 && height >= 2160) {
                pixelUrlMap.put("name", "超高清4K");
                pixelUrlMap.put("maxPixelUrl", getUrlByPixle(Pixels.PIX_UHD_3840X2160));
            }
            if (width >= 7680 && height >= 4320) {
                pixelUrlMap.put("name", "超高清8K");
                pixelUrlMap.put("maxPixelUrl", fullMaxPixelUrl);
            }
        } catch (IOException e) {
            pixelUrlMap.put("name", "超高清");
            pixelUrlMap.put("maxPixelUrl", fullMaxPixelUrl);
        } finally {
            if (image != null) {
                image.getGraphics().dispose();
            }
        }
        return pixelUrlMap;
    }

    public String getFullMaxPixelUrl(){
        if (maxPixelUrlMap.isEmpty()) {
            maxPixelUrlMap = getMaxPixelUrl();
        }
        return makeFullUrl(maxPixelUrlMap.get("maxPixelUrl"));
    }

    public String getName(){
        return url.substring(url.indexOf("id=") + 3, url.lastIndexOf("_")) + ".jpg";
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 通过大小获取展示图片的地址
     * 分辨率不用太高，加快加载的速度
     *
     * @param width
     * @param height
     * @return
     */
    public String getShowUrlBySize(int width, int height) {
        String condition = "";
        if (width != 0) {
            condition += "&w=" + width;
        }
        if (width != 0) {
            condition += "&h=" + height;
        }
        // &w=xx&h=xx 设置图片的长宽
        return url.replaceAll("[^_]+.jpg", Pixels.PIX_1366X768.getResolution() + ".jpg") + condition;
    }

    public String getShowAltBySize(int width, int height) {
        String condition = "";
        if (width != 0) {
            condition += "&w=" + width;
        }
        if (height != 0) {
            condition += "&h=" + height;
        }
        // &w=xx&h=xx 设置图片的长宽
        return alt + condition;
    }

    public String getImgTitle() {
        return title + "&#10;" + desc.replace(" (", "&#10;").replace(")", "");
    }

    public String getSummaryDesc() {
        return desc.substring(0, desc.indexOf(" ("));
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
        Image image = (Image) obj;
        return Objects.equals(getPrefixUrl(), image.getPrefixUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPrefixUrl());
    }

    @Override
    public String toString() {
        return this.date + this.title + System.lineSeparator() + this.url + System.lineSeparator() + this.getDesc();
    }

    public static Image getImageByJson(Wallpaper wallpaper) {
        String date = wallpaper.getEnddate();
        String url = wallpaper.getUrl();
        String title = wallpaper.getTitle();
        String desc = wallpaper.getCopyright();
        String alt = wallpaper.getUrlbase();
        String copyrightlink = wallpaper.getCopyrightlink();
        String fullstartdate = wallpaper.getFullstartdate();
        String hpDate = fullstartdate.substring(0, 8) + "_" + fullstartdate.substring(8);
        String link = copyrightlink + "&filters=HpDate:\"" + hpDate + "\"";
        return new Image(date, url, title, desc, alt, link);
    }

    /**
     * 获取今天的壁纸
     *
     * @return
     */
    public static Image getTodayImage() {
        Wallpaper todayApiWallpaper = BingApi.getTodayApiWallpaper();
        return getImageByJson(todayApiWallpaper);
    }

    /**
     * 按条件获取壁纸
     *
     * @param idx 0 今天；1 昨天；2 前天 ... 最大值为7
     * @param num 指定的日期开始往前推共“num”张壁纸信息
     * @return
     * @throws IOException
     */
    public static List<Image> getImages(int idx, int num) throws IOException {
        List<Wallpaper> wallpapers = BingApi.getApiWallpapers(idx, num);
        List<Image> images = new ArrayList<>();
        for (Wallpaper obj : wallpapers) {
            images.add(getImageByJson(obj));
        }
        return images;
    }

    public boolean isToday() {
        LocalDate now = LocalDate.now(ZoneId.of("UTC+8"));
        LocalDate imageDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
        return now.equals(imageDate);
    }

    public String getTopMarkdownText() {
        String fullTopShowAlt = getShowAltBySize(1204, 677);
        fullTopShowAlt = makeFullUrl(fullTopShowAlt);
        String fullTopShowUrl = getShowUrlBySize(1204, 677);
        fullTopShowUrl = makeFullUrl(fullTopShowUrl);
        String imgTitle = getImgTitle();
        String fullLink = makeFullUrl(link);
        return String.format("[![%s](%s \"%s\")](%s)<br/><center><sup>**新**</sup>&nbsp;%s，%s<center/>",
                fullTopShowAlt, fullTopShowUrl, imgTitle, fullLink, getTitle(), getSummaryDesc());
    }

    public String getMarkdownText() {
        String fullShowAlt = getShowAltBySize(384, 216);
        fullShowAlt = makeFullUrl(fullShowAlt);
        String fullShowUrl = getShowUrlBySize(384, 216);
        fullShowUrl = makeFullUrl(fullShowUrl);
        String imgTitle = getImgTitle();
        String fullLink = makeFullUrl(link);
        String date = getDate();
        // 高清图片地址
        String fullHDDownUrl = getUrlByPixle(Pixels.PIX_1920X1200);
        fullHDDownUrl = makeFullUrl(fullHDDownUrl);
        // 超高清4k图片地址
        if (maxPixelUrlMap.isEmpty()) {
            maxPixelUrlMap = getMaxPixelUrl();
        }
        String name = maxPixelUrlMap.get("name");
        String fullUHDDownUrl = maxPixelUrlMap.get("maxPixelUrl");
        fullUHDDownUrl = makeFullUrl(fullUHDDownUrl);
        return String.format("[![%s](%s \"%s\")](%s)<br/><center>%s / [高清](%s) / [%s](%s)<center/>",
                fullShowAlt, fullShowUrl, imgTitle, fullLink, date, fullHDDownUrl, name, fullUHDDownUrl);
    }

    /**
     * 一些特殊字符在markdown上显示时需要转义
     *
     * @param str
     * @return
     */
    private String escape(String str) {
        // markdown 字符转义
        if (str.indexOf("\"") != -1 && str.indexOf("\\\"") == -1) {
            return str.replace("\"", "\\\"");
        }
        return str;
    }
}
