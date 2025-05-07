package org.utility;

import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

public abstract class BaseDBHandler {
    protected Connection connection;

    protected BaseDBHandler(String filePath) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:%s".formatted(filePath));
            setupSchema();
        } catch (Exception e) {
            System.out.println("Could not connect to database: " + e.getMessage());
            System.exit(1);
        }
    }

    protected abstract File[] getUpdateFiles();

    protected void setupSchema() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS tbl_updates (update_title TEXT NOT NULL UNIQUE);");
        statement.close();

        ResultSet rs = connection.prepareStatement("SELECT update_title FROM tbl_updates;").executeQuery();
        Set<String> updated = new HashSet<>();
        while (rs.next()) {
            updated.add(rs.getString(1));
        }
        File[] updateFiles = getUpdateFiles();
        boolean vacuum = false;
        for (File file : Arrays.stream(updateFiles).sorted().toList()) {
            if (updated.contains(file.getName())) {
                continue;
            }
            vacuum = true;
            try {
                statement = connection.createStatement();
                List<String> updates = Arrays.stream(Files.readString(file.toPath()).split(";"))
                        .map(String::strip)
                        .filter(s -> !s.isEmpty())
                        .map(s -> s + ";")
                        .toList();
                for (String update : updates) {
                    statement.executeUpdate(update);
                }
                statement.close();
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tbl_updates (update_title) VALUES (?);");
                preparedStatement.setString(1, file.getName());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (vacuum) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("VACUUM;");
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
