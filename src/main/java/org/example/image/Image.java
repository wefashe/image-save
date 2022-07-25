package org.example.image;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Image implements Comparable<Image> {

    // 必应主域名
    private final static String BING_MASTER_URL = "https://cn.bing.com";
    // 必应备用域名
    private final static String BING_BACKIP_URL = "https://s.cn.bing.net";
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
    private final static String BING_IMAGE_API = "/HPImageArchive.aspx?format=js&idx=%s&n=%s&nc=1612409408851&pid=hp&FORM=BEHPTB&uhd=1&uhdwidth=3840&uhdheight=2160&setmkt=zh-cn&cc=cn";
    /**
     * 获取当日的壁纸故事
     * 已经不维护了，现只能获取21年前历史壁纸的内容
     * 例如：https://cn.bing.com/cnhp/coverstory?d=20181118
     *
     * @deprecated
     */
    private final static String BING_COVERSTORY_API = "/cnhp/coverstory?d=%s";

    public static String BING_URL = BING_MASTER_URL;

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

    public static String getBingImageApi(int idx, int num) {
        idx = Math.max(0, idx);
        idx = Math.min(7, idx);

        num = Math.max(1, num);
        num = Math.min(8, num);
        return String.format(makeFullUrl(BING_IMAGE_API), idx, num);
    }

    public static String getBingCoverstoryApi(String date) {
        return String.format(makeFullUrl(BING_COVERSTORY_API), date);
    }

    public String getDate() {
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE).format(DateTimeFormatter.ISO_LOCAL_DATE);
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
        return BING_URL + url;
    }

    public Map<String, String> getMaxPixelUrl() {
        String fullMaxPixelUrl = url.replaceAll("[^_]+.jpg", "UHD.jpg");
        fullMaxPixelUrl = makeFullUrl(fullMaxPixelUrl);
        Map<String, String> pixelUrlMap = new HashMap<>();
        BufferedImage image = null;
        try {
            URL imageURL = URI.create(fullMaxPixelUrl).toURL();
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
        return Objects.equals(desc, image.getDesc()) && Objects.equals(getDate(), image.getDate()) && Objects.equals(getPrefixUrl(), image.getPrefixUrl()) && Objects.equals(getTitle(), image.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(desc, date, getPrefixUrl(), title);
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

    private static JSONObject[] getImageJSONObject(int idx, int num) throws IOException {
        boolean flag = true;
        String jsonText = null;
        while (flag) {
            try {
                String bingImageApi = Image.getBingImageApi(idx, num);
                jsonText = IOUtils.toString(URI.create(bingImageApi), StandardCharsets.UTF_8);
                flag = false;
            } catch (UnknownHostException e) {
                if (BING_BACKIP_URL.equals(BING_URL)) {
                    flag = false;
                } else {
                    BING_URL = BING_BACKIP_URL;
                }
            }
        }
        if (Objects.isNull(jsonText) || jsonText.isEmpty()) {
            throw new IOException(BING_IMAGE_API + "接口获取数据失败！");
        }
        JSONArray array = JSON.parseObject(jsonText).getJSONArray("images");
        if (Objects.isNull(array) || array.isEmpty()) {
            throw new IOException(BING_IMAGE_API + "接口获取图片数据失败！");
        }
        return array.toArray(new JSONObject[0]);
    }

    /**
     * 获取今天的壁纸
     *
     * @return
     * @throws IOException
     */
    public static Image getTodayImage() throws IOException {
        JSONObject[] imageJSONObjects = getImageJSONObject(0, 1);
        return getImageByJson(imageJSONObjects[0]);
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
        JSONObject[] imageJSONObjects = getImageJSONObject(idx, num);
        List<Image> images = new ArrayList<>();
        for (JSONObject obj : imageJSONObjects) {
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
