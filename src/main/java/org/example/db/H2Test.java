package org.example.db;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.example.image.Image;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class H2Test {

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        String bingImageApi = Image.getBingImageApi(10, 10);
        String jsonText = IOUtils.toString(URI.create(bingImageApi), StandardCharsets.UTF_8);
        JSONArray array = JSON.parseObject(jsonText).getJSONArray("images");
        if (Objects.isNull(array) || array.isEmpty()) {
            return;
        }
        for (JSONObject obj : array.toArray(new JSONObject[0])) {
            Wallpaper wallpaper = new Wallpaper(obj);
            wallpaper.addDesc(Image.BING_URL);
            H2Db.addWallpaper(wallpaper);
        }
    }

}
