
package org.example.db;

import lombok.Data;
@Data
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

    public Wallpaper() {
    }

    public Wallpaper(String date, String url, String copyright, String title) {
        this.enddate = date;
        this.url = url;
        this.copyright = copyright;
        this.title = title;
    }
}
