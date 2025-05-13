package org.experiments.logger;

import org.utility.BaseDBHandler;

import java.io.File;
import java.sql.*;

public class CacheManager extends BaseDBHandler {

    public CacheManager() {
        this("cache.sqlite3");
    }


    public CacheManager(String filePath) {
        super(filePath);
    }

    @Override
    protected File[] getUpdateFiles() {
        try {
            File[] files = new File("src/main/java/org/experiments/logger/updates").listFiles();
            if (files == null) {
                throw new RuntimeException("Invalid update folder");
            }
            return files;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Cache getCache(String model, String system) {
        try {
            int settingId = getOrCreateSettingId(model, system);
            return new Cache(connection, settingId);
        } catch (Exception e) {
            System.out.println("Could not get cache: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }


    private int getOrCreateSettingId(String model, String system) throws SQLException {
        PreparedStatement query_ps = connection.prepareStatement("SELECT id FROM tbl_setting WHERE model_name = ? AND system_text = ?");
        query_ps.setString(1, model);
        query_ps.setString(2, system);
        ResultSet rs = query_ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        query_ps.close();

        PreparedStatement insert_ps = connection.prepareStatement("INSERT INTO tbl_setting (model_name, system_text) VALUES (?, ?)");
        insert_ps.setString(1, model);
        insert_ps.setString(2, system);
        insert_ps.executeUpdate();

        ResultSet keys = insert_ps.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        throw new SQLException("Failed find an id");
    }
}
