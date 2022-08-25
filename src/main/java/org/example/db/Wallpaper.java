
package org.example.db;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
public class Wallpaper {

    private String startdate;
    private String fullstartdate;
    private String enddate;
    private String url;
    private String urlbase;
    private String copyright;
    private String copyrightlink;
    private String title;
    private String quiz;
    private String hsh;

    public Wallpaper(JSONObject obj) {
        this.startdate = (String) obj.get("startdate");
        this.fullstartdate = (String) obj.get("fullstartdate");
        this.enddate = (String) obj.get("enddate");
        this.url = (String) obj.get("url");
        this.urlbase = (String) obj.get("urlbase");
        this.copyright = (String) obj.get("copyright");
        this.copyrightlink = (String) obj.get("copyrightlink");
        this.title = (String) obj.get("title");
        this.quiz = (String) obj.get("quiz");
        this.hsh = (String) obj.get("hsh");
    }

    public Wallpaper(String date, String url, String copyright, String title) {
        this.enddate = date;
        this.url = url;
        this.copyright = copyright;
        this.title = title;
    }

    public void addDesc(String prefix) {
        String hpDate = this.fullstartdate.substring(0, 8) + "_" + this.fullstartdate.substring(8);
        String link = prefix + this.copyrightlink + "&filters=HpDate:\"" + hpDate + "\"";
        Element body = null;
        try {
            body = Jsoup.connect(link).get().body();
            Elements select = body.select("#encycloCanvas .tc_content .ency_desc");
            // this.desc = Jsoup.clean(select.html(), "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        } catch (IOException e) {
            System.err.println("获取壁纸对应的故事失败！");
        }
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getFullstartdate() {
        return fullstartdate;
    }

    public void setFullstartdate(String fullstartdate) {
        this.fullstartdate = fullstartdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlbase() {
        return urlbase;
    }

    public void setUrlbase(String urlbase) {
        this.urlbase = urlbase;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getCopyrightlink() {
        return copyrightlink;
    }

    public void setCopyrightlink(String copyrightlink) {
        this.copyrightlink = copyrightlink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuiz() {
        return quiz;
    }

    public void setQuiz(String quiz) {
        this.quiz = quiz;
    }

    public String getHsh() {
        return hsh;
    }

    public void setHsh(String hsh) {
        this.hsh = hsh;
    }
}
