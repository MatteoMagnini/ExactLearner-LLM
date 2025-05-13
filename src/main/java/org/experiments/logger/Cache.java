package org.experiments.logger;

import java.sql.*;
import java.util.Locale;

public class Cache {

    private final Connection connection;
    private final Integer settingId;

    public Cache(Connection connection, Integer settingId) {
        this.connection = connection;
        this.settingId = settingId;
    }

    public synchronized Boolean isStrictlyTrue(String query) {
        String res = resultString(query);
        if (res == null) {
            return null;
        }
        Boolean test = getIsStrictlyTrue(res);
        return (test != null) && test;
    }

    public synchronized String resultString(String query) {
        try {
            PreparedStatement ps = connection.prepareStatement("""
                SELECT result FROM tbl_cache
                    WHERE setting_id = ?
                    AND query = ?""");

            ps.setInt(1, settingId);
            ps.setString(2, query);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            String result = rs.getString(1);
            ps.close();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void storeQuery(String query, String result){
        try {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO tbl_cache(setting_id, query, result)
                    VALUES (?, ?, ?)""");

            ps.setInt(1, settingId);
            ps.setString(2, query);
            ps.setString(3, result);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not insert result");
        }
    }

    private Boolean getIsStrictlyTrue(String result) {
        if (result.toLowerCase(Locale.ROOT).contains("true")) return true;
        if (result.toLowerCase(Locale.ROOT).contains("false")) return false;
        return null;
    }
}
