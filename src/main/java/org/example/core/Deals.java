package org.example.core;

import org.example.api.BingApi;
import org.example.db.H2Db;
import org.example.db.Wallpaper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Deals {

    public static Wallpaper getTodayWallpaper(){
        LocalDate now = LocalDate.now(ZoneId.of("UTC+8"));
        Wallpaper todayWallpaper = H2Db.getDBWallpaper(now.format(DateTimeFormatter.BASIC_ISO_DATE));
        if (todayWallpaper != null) {
            return todayWallpaper;
        }
        todayWallpaper = BingApi.getTodayApiWallpaper();
        if (todayWallpaper != null) {
            H2Db.addWallpaper2DB(todayWallpaper);
        }
        return todayWallpaper;
    }

    public static List<Wallpaper> getWallpapers(int idx, int num){
        idx = Math.max(0, idx);
        idx = Math.min(7, idx);
        num = Math.max(1, num);
        num = Math.min(8, num);
        List<Wallpaper> wallpapers = H2Db.getDBWallpapers(idx, num);
        if (wallpapers != null && wallpapers.size() == num) {
            return wallpapers;
        }
        wallpapers = BingApi.getApiWallpapers(idx, num);
        if (wallpapers != null && wallpapers.size() == num) {
            H2Db.batchAddWallpaper2DB(wallpapers);
        }
        return wallpapers;
    }

    public static String getCoverstoryLink(Wallpaper wallpaper) {
        String fullstartdate = wallpaper.getFullstartdate();
        if (fullstartdate == null || fullstartdate.trim().isEmpty()) {
            fullstartdate = wallpaper.getEnddate() + "1600";
        }
        String copyrightlink = wallpaper.getCopyrightlink();
        if (copyrightlink == null || copyrightlink.trim().isEmpty()) {
            copyrightlink = "/search?q=%s&form=hpcapt&mkt=zh-cn";
            String copyright = wallpaper.getCopyright();
            copyright = copyright.substring(0, copyright.indexOf(" "));
            try {
                copyright = URLEncoder.encode(copyright, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.err.println("地址中文编码异常");
            }
            copyrightlink = String.format(copyrightlink, copyright);
        }
        String prefix = BingApi.BING_URL_PREFIX;
        String hpDate = fullstartdate.substring(0, 8) + "_" + fullstartdate.substring(8);
        return prefix + copyrightlink + "&filters=HpDate:\"" + hpDate + "\"";
    }

    public static String getCoverstory(Wallpaper wallpaper){
        String coverstoryLink = getCoverstoryLink(wallpaper);
        String coverstory = "";
        try {
            Element body = Jsoup.connect(coverstoryLink).get().body();
            Elements select = body.select("#encycloCanvas .tc_content .ency_desc");
            coverstory = Jsoup.clean(select.html(), "", Safelist.none(), new Document.OutputSettings().prettyPrint(false));
        } catch (IOException e) {
            System.err.println("获取壁纸对应的故事失败！");
        }
        if (coverstory.isEmpty()) {
            coverstory = BingApi.getApiCoverstory(wallpaper.getEnddate());
        }
        return coverstory;
    }

    public static String getPixelUrl(String url, Pixels pixel) {
        url = url.replace("UHD", pixel.getResolution());
        url = url.replace("3840", String.valueOf(pixel.getWidth()));
        url = url.replace("2160", String.valueOf(pixel.getHeight()));
        return url;
    }

}
