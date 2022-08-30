package org.example.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class BingApi {

    private final static String[] BING_URL_PREFIXS = {
            "https://cn.bing.com1",
            "https://s.cn.bing.net1",
            "https://global.bing.com",
            "https://www.bing.com",
    };

    public static String BING_URL_PREFIX = BING_URL_PREFIXS[0];

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
    private final static String BING_WALLPAPER_API = "/HPImageArchive.aspx?format=js&idx=%s&n=%s&nc=1612409408851&pid=hp&FORM=BEHPTB&uhd=1&uhdwidth=3840&uhdheight=2160&setmkt=zh-cn&cc=cn";

    static {
        for (String URLName : BING_URL_PREFIXS) {
            if (exists(URLName)) {
                BING_URL_PREFIX = URLName;
                break;
            }
        }
    }

    public static boolean exists(String URLName) {
        try {
            // 设置所有的http连接是否自动处理重定向
            // HttpURLConnection.setFollowRedirects(false);
            // 设置本次连接是否自动处理重定向
            // HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con = (HttpURLConnection) new URL(URLName)
                    .openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (IOException e) {
            return false;
        }
    }

    public static String getBingWallpaperApi(int idx, int num) {
        idx = Math.max(0, idx);
        idx = Math.min(7, idx);
        num = Math.max(1, num);
        num = Math.min(8, num);
        return BING_URL_PREFIX + String.format(BING_WALLPAPER_API, idx, num);
    }

    public static String getTodayBingWallpaperApi() {
        return getBingWallpaperApi(0, 1);
    }
}
