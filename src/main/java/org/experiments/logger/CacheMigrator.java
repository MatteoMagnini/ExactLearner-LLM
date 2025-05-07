package org.experiments.logger;

import org.experiments.Result;

import java.sql.Connection;

import static org.utility.SHA256Hash.sha256;

public class CacheMigrator extends Cache {
    private final String model;
    private final String ontology;
    private final String task;
    private final String system;


    public CacheMigrator(Connection connection, Integer model_id, Integer system_id,
                         String model, String task, String system) {
        super(connection, model_id, system_id);
        this.model = model;
        this.ontology = "";
        this.task = task;
        this.system = system;
    }

    @Override
    public Boolean isStrictlyTrue(String query) {
        String filename = getFileName(query);
        Boolean isTure = super.isStrictlyTrue(query);
        if (isTure != null) {
            if (SmartLogger.isFileInCache(filename)) {
                SmartLogger.removeFileFromCache(filename);
            }
            return isTure;
        }
        if (SmartLogger.isFileInCache(filename)) {
            Result result = new Result(filename);
            super.storeQuery(query, result.getResponse(query));
            SmartLogger.removeFileFromCache(filename);
            return result.isStrictlyTrue(query);
        }
        return null;
    }

    private String SHA256Hash(String input) {
        return sha256(input);
    }

    private String getFileName(String query) {
        return SHA256Hash(task + model + ontology + query + system);
    }
}
