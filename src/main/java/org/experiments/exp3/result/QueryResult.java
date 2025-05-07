package org.experiments.exp3.result;

import org.experiments.exp3.render.axiom.AxiomRenderer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryResult {
    private final Connection connection;
    private final int queryId;

    public QueryResult(Connection connection, String query, String ontology, int testId) throws SQLException {
        this.connection = connection;
        this.queryId = getOrCreateQueryId(query, ontology, testId);
    }

    private synchronized int getOrCreateQueryId(String query, String ontology, int testId) throws SQLException {
        PreparedStatement query_ps = connection.prepareStatement(
                "SELECT ROWID FROM tbl_query WHERE ontology = ? AND identifier = ? AND test_id = ?");
        query_ps.setString(1, ontology);
        query_ps.setString(2, query);
        query_ps.setInt(3, testId);
        ResultSet rs = query_ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        query_ps.close();

        PreparedStatement insert_ps = connection.prepareStatement(
                "INSERT INTO tbl_query (ontology, identifier, test_id) VALUES (?, ?, ?)");
        insert_ps.setString(1, ontology);
        insert_ps.setString(2, query);
        insert_ps.setInt(3, testId);
        insert_ps.executeUpdate();

        ResultSet keys = insert_ps.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        throw new SQLException("Failed find an id");
    }

    public int getQueryId() {
        return queryId;
    }
}
