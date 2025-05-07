package org.experiments.exp3.result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingResult {
    private final Connection connection;
    private final int systemId;

    public SettingResult(Connection connection, String model, String system, String queryFormat) throws SQLException {
        this.connection = connection;
        this.systemId = getOrCreateSystemId(model, system, queryFormat);
    }

    public SettingResult(Connection connection) throws SQLException {
        this.connection = connection;
        this.systemId = getOrCreateSystemId();
    }

    private synchronized int getOrCreateSystemId(String model, String system, String queryFormat) throws SQLException {
        PreparedStatement query_ps = connection.prepareStatement(
                "SELECT ROWID FROM tbl_setting WHERE model = ? AND system_text = ? AND query_format = ?");
        query_ps.setString(1, model);
        query_ps.setString(2, system);
        query_ps.setString(3, queryFormat);
        ResultSet rs = query_ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        query_ps.close();
        return createSystemId(model, system, queryFormat, false);
    }

    private synchronized int getOrCreateSystemId() throws SQLException {
        PreparedStatement query_ps = connection.prepareStatement(
                "SELECT ROWID FROM tbl_setting WHERE answer = ?");
        query_ps.setBoolean(1, true);
        ResultSet rs = query_ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        query_ps.close();
        return createSystemId(null, null, null, true);
    }

    private synchronized int createSystemId(String model, String system, String queryFormat, boolean answer) throws SQLException {
        PreparedStatement insert_ps = connection.prepareStatement(
                "INSERT INTO tbl_setting (model, system_text, query_format, answer) VALUES (?, ?, ?, ?)");
        insert_ps.setString(1, model);
        insert_ps.setString(2, system);
        insert_ps.setString(3, queryFormat);
        insert_ps.setBoolean(4, answer);
        insert_ps.executeUpdate();

        ResultSet keys = insert_ps.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        throw new SQLException("Failed find an id");
    }

    public int getSystemId() {
        return systemId;
    }
}
