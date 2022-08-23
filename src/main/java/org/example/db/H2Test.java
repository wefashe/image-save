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
    private static final String USER = "wefashe";
    private static final String PASSWORD = "wefashe";
    private static final String DRIVER_CLASS = "org.h2.Driver";
    private static final String JDBC_URL = "jdbc:h2:./db/images;AUTO_SERVER=TRUE";

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        Connection conn = H2Db.getConnection();
        Statement statement = conn.createStatement();
        String bingImageApi = Image.getBingImageApi(0, 10);
        String jsonText = IOUtils.toString(URI.create(bingImageApi), StandardCharsets.UTF_8);
        JSONArray array = JSON.parseObject(jsonText).getJSONArray("images");
        if (Objects.isNull(array) || array.isEmpty()) {
            return;
        }
        for (JSONObject obj : array.toArray(new JSONObject[0])) {
            Wallpaper wallpaper = new Wallpaper(obj);
            wallpaper.addDesc(Image.BING_URL);
            statement.execute(wallpaper.toSql());
        }
        H2Db.close(statement, conn);
    }

}
