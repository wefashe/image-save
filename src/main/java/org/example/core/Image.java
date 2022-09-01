package org.example.core;

import lombok.Data;
import org.example.api.BingApi;
import org.example.db.Wallpaper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Data
public class Image  implements Comparable<Image>{

    private LocalDate date;
    private String title;
    private String alt;
    private String author;
    private String coverstoryLink;
    private String smallImgLink;
    private String smallAltLink;
    private String hdImgLink;
    private String uhdImgLink;
    private String uhdName;
    private boolean isToday;

    public Image(Wallpaper wallpaper) {
        this.date = LocalDate.parse(wallpaper.getEnddate(), DateTimeFormatter.BASIC_ISO_DATE);
        this.title = wallpaper.getTitle();
        String copyright = wallpaper.getCopyright();
        this.alt = copyright.substring(0, copyright.indexOf(" "));
        int endIndex = copyright.lastIndexOf("/");
        if (endIndex == -1) {
            endIndex = copyright.lastIndexOf(")");
        }
        this.author = copyright.substring(copyright.indexOf("©") + 1, endIndex).trim();
        this.coverstoryLink = Deals.getCoverstoryLink(wallpaper);
        String url = BingApi.BING_URL_PREFIX + wallpaper.getUrl();
        this.smallImgLink = url.replace("3840", "384").replace("2160", "216");
        String urlbase = BingApi.BING_URL_PREFIX + wallpaper.getUrlbase();
        if (urlbase == null || urlbase.trim().isEmpty()) {
            urlbase = url.substring(0, url.indexOf("_UHD.jpg"));
        }
        this.smallAltLink = urlbase + "&w=384&h=216";
        this.hdImgLink = Deals.getPixelUrl(url, Pixels.PIX_1920X1080);
        Pixels maxPixel = getMaxPixel(url.replace("&w=3840&h=2160", ""));
        if (Pixels.PIX_UHD_7680X4320.equals(maxPixel)) {
            this.uhdName = "8K";
        } else if(Pixels.PIX_UHD_3840X2160.equals(maxPixel)) {
            this.uhdName = "4K";
        } else {
            this.uhdName = "";
        }
        this.uhdImgLink = url.replace("3840", String.valueOf(maxPixel.getWidth()))
                .replace("2160", String.valueOf(maxPixel.getHeight()));
        LocalDate now = LocalDate.now(ZoneId.of("UTC+8"));
        this.isToday = now.equals(date);
    }

    public static Pixels getMaxPixel(String url) {
        BufferedImage sourceImg = null;
        try {
            sourceImg = ImageIO.read(new URL(url));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return Pixels.PIX_UHD_3840X2160;
        }
        int width = sourceImg.getWidth();
        int height = sourceImg.getHeight();
        return Pixels.getPixel(width, height);
    }

    public String getDate(DateTimeFormatter formatter) {
        if (formatter == null) {
            formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        }
        return this.date.format(formatter);
    }

    /**
     * 一些特殊字符在markdown上显示时需要转义
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

    @Override
    public int compareTo(Image image) {
        return Math.negateExact(this.getDate().compareTo(image.getDate()));
    }

}
