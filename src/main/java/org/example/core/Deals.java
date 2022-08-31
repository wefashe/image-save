package org.example.core;

import org.example.api.BingApi;
import org.example.db.H2Db;
import org.example.db.Wallpaper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Deals {

    public static Wallpaper getTodayWallpaper(){
        LocalDate now = LocalDate.now(ZoneId.of("UTC+8"));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        Wallpaper todayWallpaper = H2Db.getDBWallpaper(now.format(fmt));
        if (todayWallpaper != null) {
            return todayWallpaper;
        }
        todayWallpaper = BingApi.getTodayApiWallpaper();
        if (todayWallpaper != null) {
            H2Db.addWallpaper2DB(todayWallpaper);
        }
        return todayWallpaper;
    }

    public static String getCoverstory(Wallpaper wallpaper){
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
        String link = prefix + copyrightlink + "&filters=HpDate:\"" + hpDate + "\"";
        String coverstory = "";
        try {
            Element body = Jsoup.connect(link).get().body();
            Elements select = body.select("#encycloCanvas .tc_content .ency_desc");
            coverstory = Jsoup.clean(select.html(), "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        } catch (IOException e) {
            System.err.println("获取壁纸对应的故事失败！");
        }
        if (coverstory.isEmpty()) {
            coverstory = BingApi.getApiCoverstory(wallpaper.getEnddate());
        }
        return coverstory;
    }

}
