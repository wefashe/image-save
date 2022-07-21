package org.example.image;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Image implements Comparable<Image> {

    /**
     * BING API
     * format	 返回的数据格式。hp为html格式；js为json格式；其他值为xml格式；缺省（或缺失）将默认返回 XML 文档数据格式
     * idx	     获取特定时间点的数据。如idx=1表示前一天（昨天），依此类推。经过测试最大值为7，缺省（或缺失）则默认为“0”，表示今天的美图。
     * n	     获取数据的条数。经测试，配合上idx最大可以获取到13天前的数据，即idx=7&n=7，必选	缺省（或缺失）将返回 null（空值），导致无法获取美图信息。表示从“idx”指定的日期开始往前推共“n”张美图信息。
     * pid	     未知。pid为hp时，copyrightlink返回的是相对地址。pid不为hp时，没有看到og信息
     * ensearch	 指定获取必应【国际版/国内版】的每日一图。当ensearch=1时，获取到的是必应国际版的每日一图数据。默认情况和其他值情况下，获取到的是必应国内版的每日一图数据
     * quiz	     当quiz=1时，返回必应小测验所需的相关数据。
     * og	     水印图相关的信息。包含了title、img、desc和hash等信息
     * uhd	     当uhd=1时，可以自定义图片的宽高。当uhd=0时，返回的是固定宽高（1920x1080）的图片数据
     * uhdwidth	 图片宽度。当uhd=1时生效。最大值为3840，超过这个值当作3840处理
     * uhdheight 图片高度。当uhd=1时生效。最大值为2592，超过这个值当作2592处理
     * setmkt	 指定图片相关的区域信息。如图片名中包含的EN-CN、EN-US或者ZH-CN等。参考值：en-us、zh-cn等
     * setlang	 指定返回数据所使用的语言。参考值：en-us、zh-cn等
     * cc	     可选	国家（含地区）代码（Country Code）的英文缩写，表示获取相应地区的必应美图（需要国外主机，国内主机请求一律返回中国区的必应美图），目前已知的可取值范围 {ar、at、au、be、br、ca、ch、cl、cn、de、dk、es、fi、fr、hk、ie、in、it、jp、kr、nl、no、nz、ph、pt、ru、se、sg、tw、uk}，对应的地区请对照此列表 → 传送门，缺省（或缺失）将自动根据请求源 IP 所在的地区返回相应地区的美图信息（划重点，并非每个地区都有属于自己独一无二的美图，未预设美图的地区将直接引用国际版 Bing 美图。另外在配合国外主机使用此参数时抓取信息时，需要使用国际版或其它地区的必应首页地址，例如“www.bing.com”，才能获取到相应“cc”地区的美图，否则一律返回中国区的美图信息。
     * video     可选	取值范围 [0, 1]，缺省（或缺失）则默认为“0”，则不返回相应的流媒体信息（音频/视频），并不是每天都有流媒体视音频的，需要根据返回的字段键值对做判断。
     */
    private static String BING_API = "https://cn.bing.com/HPImageArchive.aspx?format=js&idx=%s&n=%s&nc=1612409408851&pid=hp&FORM=BEHPTB&uhd=1&uhdwidth=3840&uhdheight=2160&setmkt=zh-cn&cc=cn";

    private static String BING_URL = "https://cn.bing.com";

    private String date;
    private String url;
    private String title;
    private String desc;
    private String alt;
    private String link;

    public Image(String date, String url, String title, String desc, String alt, String link) {
        this.date = date;
        if (!url.contains("http")) {
            url = BING_URL + url;
        }
        if (url.contains("&")) {
            url = url.substring(0, url.indexOf("&"));
        }
        this.url = url;
        this.title = escape(title);
        this.desc = escape(desc);
        if (!alt.contains("http")) {
            alt = BING_URL + alt;
        }
        this.alt = alt;
        if (!link.contains("http")) {
            link = BING_URL + link;
        }
        this.link = link;
    }

    public static String getBingApi(int idx, int num) {
        idx = Math.max(0, idx);
        idx = Math.min(7, idx);

        num = Math.max(1, num);
        num = Math.min(8, num);
        return String.format(BING_API, idx, num);
    }

    public String getDate() {
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getUrl() {
        // 手机高清 1080x1920
        // _UHD.jpg 超高清4K  即5187x2918
        return url;
    }

    public String getHDUrl() {
        // _1920x1080.jpg 高清
        return url.replace("_UHD.jpg", "_1920x1080.jpg");
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getWith1204Url() {
        return url + "&w=1204";
    }

    public String getSmallUrl() {
        return url + "&pid=hp&w=384&h=216&rs=1&c=4";
    }

    public String getWith1204Alt() {
        return alt + "&w=1204";
    }

    public String getSmallAlt() {
        return alt + "&pid=hp&w=384&h=216&rs=1&c=4";
    }

    public String getImgTitle() {
        return title + "&#10;" + desc;
    }

    public String getSummaryDesc() {
        return desc.substring(0, desc.indexOf(" (©"));
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
        return Objects.equals(desc, image.getDesc()) && Objects.equals(getDate(), image.getDate()) && Objects.equals(getUrl(), image.getUrl()) && Objects.equals(getTitle(), image.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(desc, date, url, title);
    }

    @Override
    public String toString() {
        return this.date + this.title + System.lineSeparator() + this.url + System.lineSeparator() + this.getDesc();
    }

    public static Image getImageByJson(JSONObject obj) {
        String date = (String) obj.get("enddate");
        String url = (String) obj.get("url");
        String title = (String) obj.get("title");
        String desc = (String) obj.get("copyright");
        String alt = (String) obj.get("urlbase");
        String copyrightlink = (String) obj.get("copyrightlink");
        String fullstartdate = (String) obj.get("fullstartdate");
        String hpDate = fullstartdate.substring(0, 8) + "_" + fullstartdate.substring(8);
        String link = copyrightlink + "&filters=HpDate:\"" + hpDate + "\"";
        return new Image(date, url, title, desc, alt, link);
    }

    /**
     * 获取今天的壁纸
     *
     * @return
     * @throws IOException
     */
    public static Image getTodayImage() throws IOException {
        String bingApi = Image.getBingApi(0, 1);
        String jsonText = IOUtils.toString(URI.create(bingApi), StandardCharsets.UTF_8);
        JSONObject obj = (JSONObject) JSON.parseObject(jsonText).getJSONArray("images").get(0);
        return getImageByJson(obj);
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
        String bingApi = Image.getBingApi(idx, num);
        String jsonText = IOUtils.toString(URI.create(bingApi), StandardCharsets.UTF_8);
        JSONArray array = JSON.parseObject(jsonText).getJSONArray("images");
        List<Image> images = new ArrayList<>();
        for (JSONObject obj : array.toArray(new JSONObject[0])) {
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
        String alt = getWith1204Alt();
        String img = getWith1204Url();
        String imgTitle = getImgTitle();
        return String.format("[![%s](%s \"%s\")](%s)<br/><center><sup>**新**</sup>&nbsp;%s，%s<center/>", alt, img, imgTitle, link, getTitle(), getSummaryDesc());
    }

    public String getMarkdownText() {
        String alt = getSmallAlt();
        String img = getSmallUrl();
        String imgTitle = getImgTitle();
        String date = getDate();
        String hdUrl = getHDUrl();
        String uhdUrl = getUrl();
        return String.format("[![%s](%s \"%s\")](%s)<br/><center>%s / [高清](%s) / [超高清4K](%s)<center/>", alt, img, imgTitle, link, date, hdUrl, uhdUrl);
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
