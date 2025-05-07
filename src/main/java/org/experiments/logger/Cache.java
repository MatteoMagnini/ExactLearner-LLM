package org.experiments.logger;

import java.sql.*;
import java.util.Locale;

public class Cache {

    private final Connection connection;
    private final Integer model_id;
    private final Integer system_id;

    public Cache(Connection connection, Integer model_id, Integer system_id) {
        this.connection = connection;
        this.model_id = model_id;
        this.system_id = system_id;
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
                    WHERE model_id = ?
                    AND system_id = ?
                    AND query = ?""");

            ps.setInt(1, model_id);
            ps.setInt(2, system_id);
            ps.setString(3, query);

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
                    INSERT INTO tbl_cache(model_id, system_id, query, result)
                    VALUES (?, ?, ?, ?)""");

            ps.setInt(1, model_id);
            ps.setInt(2, system_id);
            ps.setString(3, query);
            ps.setString(4, result);
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
