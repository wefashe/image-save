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
public class ShowDetail {

    private LocalDate date;
    private String title;
    private String alt;
    private String author;
    private String coverstoryLink;
    private String smallLink;
    private String hdLink;
    private String uhdLink;
    private String uhdName;
    private boolean isToday;

    public ShowDetail(Wallpaper wallpaper) {
        this.date = LocalDate.parse(wallpaper.getEnddate(), DateTimeFormatter.BASIC_ISO_DATE);
        this.title = wallpaper.getTitle();
        String copyright = wallpaper.getCopyright();
        this.alt = copyright.substring(0, copyright.indexOf(" "));
        this.author = copyright.substring(copyright.indexOf("©")+1, copyright.lastIndexOf("/")).trim();
        this.coverstoryLink = Deals.getCoverstoryLink(wallpaper);
        String url = BingApi.BING_URL_PREFIX + wallpaper.getUrl();
        this.smallLink = url.replace("3840", "384").replace("2160", "216");
        this.hdLink = Deals.getPixelUrl(url, Pixels.PIX_1920X1080);
        Pixels maxPixel = getMaxPixel(url.replace("&w=3840&h=2160", ""));
        if (Pixels.PIX_UHD_7680X4320.equals(maxPixel)) {
            this.uhdName = "8K";
        } else if(Pixels.PIX_UHD_3840X2160.equals(maxPixel)) {
            this.uhdName = "4K";
        } else {
            this.uhdName = "";
        }
        this.uhdLink = url.replace("3840", String.valueOf(maxPixel.getWidth()))
                .replace("2160", String.valueOf(maxPixel.getHeight()));
        LocalDate now = LocalDate.now(ZoneId.of("UTC+8"));
        this.isToday = now.equals(date);
    }

    public static Pixels getMaxPixel(String url) {
        BufferedImage sourceImg = null;
        try {
            sourceImg = ImageIO.read(new URL(url));
        } catch (IOException e) {
            System.out.println(e.getMessage());
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

}
