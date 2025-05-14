package org.experiments.exp3.result;

import org.experiments.exp3.render.axiom.ManchesterRender;
import org.utility.BaseDBHandler;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultManagerDB extends BaseDBHandler {

    public ResultManagerDB() {
        super("results.sqlite3");
    }

    public ResultSaver getResultSaver(String model, String system, String ontology,
                                      String queryFormat, String testType, String testName) {
        try {
           int testId = getTestId(testType, testName);
           int settingId = getSettingId(model, system, queryFormat);
           return new ResultSaverOld(settingId, testId, ontology, connection, new ManchesterRender());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized int createTest(String testType, String testName) {
        try {
            return getTestId(testType, testName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void saveResult(QueryResult query, SettingResult setting, String text, String result) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                INSERT INTO tbl_result (query_id, setting_id, query_text, result)
                VALUES (?, ?, ?, ?)""");
            preparedStatement.setInt(1, query.getQueryId());
            preparedStatement.setInt(2, setting.getSystemId());
            preparedStatement.setString(3, text);
            preparedStatement.setString(4, result);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public SettingResult getSetting(String model, String system, String queryFormat) throws SQLException {
        return new SettingResult(connection, model, system, queryFormat);
    }

    public SettingResult getSetting() {
        try {
            return new SettingResult(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public QueryResult getQuery(String query, String ontology, int testId) {
        try {
            return new QueryResult(connection, query, ontology, testId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized int getSettingId(String model, String system, String queryFormat) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("""
                SELECT ROWID FROM tbl_setting
                    WHERE model = ?
                    AND system_text = ?
                    AND query_format = ?;""");
        preparedStatement.setString(1, model);
        preparedStatement.setString(2, system);
        preparedStatement.setString(3, queryFormat);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getInt(1);
        }

        preparedStatement = connection.prepareStatement("""
                INSERT INTO tbl_setting (model, system_text, query_format, answer)
                    VALUES (?, ?, ?, false);""");
        preparedStatement.setString(1, model);
        preparedStatement.setString(2, system);
        preparedStatement.setString(3, queryFormat);

        preparedStatement.executeUpdate();

        ResultSet keys = preparedStatement.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        throw new SQLException("Failed find an id");
    }

    private synchronized int getTestId(String testType, String testName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("""
                INSERT INTO tbl_test (test_type, test_name)
                    VALUES (?, ?);""");
        preparedStatement.setString(1, testType);
        preparedStatement.setString(2, testName);

        preparedStatement.executeUpdate();

        ResultSet keys = preparedStatement.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        throw new SQLException("Failed find an id");
    }

    @Override
    protected File[] getUpdateFiles() {
        try {
            File[] files = new File("src/main/java/org/experiments/exp3/result/updates").listFiles();
            if (files == null) {
                throw new RuntimeException("Invalid update folder");
            }
            return files;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
