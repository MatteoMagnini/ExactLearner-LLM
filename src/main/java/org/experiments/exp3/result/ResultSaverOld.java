package org.experiments.exp3.result;

import org.experiments.exp3.render.axiom.AxiomRenderer;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSaverOld implements ResultSaver {
    private final Connection connection;
    private final AxiomRenderer axiomRenderer;
    private final int settingId;
    private final int testId;
    private final String ontology;

    public ResultSaverOld(int settingId, int testId, String ontology, Connection connection, AxiomRenderer axiomRenderer) {
        this.connection = connection;
        this.axiomRenderer = axiomRenderer;
        this.settingId = settingId;
        this.testId = testId;
        this.ontology = ontology;
    }

    @Override
    public synchronized void add(OWLSubClassOfAxiom axiom, String query, String result) {
        String queryIdent = axiomRenderer.render(axiom);
        try {
            int queryId = getQuery(ontology, queryIdent);
            PreparedStatement preparedStatement = connection.prepareStatement("""
                INSERT INTO tbl_result (query_id, setting_id, query_text, result)
                VALUES (?, ?, ?, ?)""");
            preparedStatement.setInt(1, queryId);
            preparedStatement.setInt(2, settingId);
            preparedStatement.setString(3, query);
            preparedStatement.setString(4, result);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized int getQuery(String ontology, String identifier) throws SQLException {
        PreparedStatement query_ps = connection.prepareStatement(
                "SELECT ROWID FROM tbl_query WHERE ontology = ? AND identifier = ? AND test_id = ?");
        query_ps.setString(1, ontology);
        query_ps.setString(2, identifier);
        query_ps.setInt(3, testId);
        ResultSet rs = query_ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        query_ps.close();

        PreparedStatement insert_ps = connection.prepareStatement(
                "INSERT INTO tbl_query (ontology, identifier, test_id) VALUES (?, ?, ?)");
        insert_ps.setString(1, ontology);
        insert_ps.setString(2, identifier);
        insert_ps.setInt(3, testId);
        insert_ps.executeUpdate();

        ResultSet keys = insert_ps.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }
        throw new SQLException("Failed find an id");
    }
}
