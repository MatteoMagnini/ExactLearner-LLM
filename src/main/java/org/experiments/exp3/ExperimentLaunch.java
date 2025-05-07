package org.experiments.exp3;

import org.configurations.Configuration;
import org.experiments.exp3.experiment.AxiomExperiment;
import org.experiments.exp3.experiment.ClassExperiment;
import org.experiments.exp3.render.axiom.*;
import org.experiments.exp3.render.concept.ClassName;
import org.experiments.exp3.result.ResultManagerDB;
import org.experiments.exp3.result.SettingResult;
import org.experiments.logger.Cache;
import org.experiments.logger.CacheManager;
import org.experiments.logger.SmartLogger;
import org.utility.YAMLConfigLoader;

import java.util.ArrayList;
import java.util.List;

public class ExperimentLaunch {
    public static void main(String[] args) {
        // Read the configuration file passed by the user as an argument
        var config = new YAMLConfigLoader().getConfig(args[0], Configuration.class);
        // For each model in the configuration file and for each ontology in the configuration file, run the experiment
        SmartLogger.checkCachedFiles();
        CacheManager cacheManager = new CacheManager();
        ResultManagerDB resultManagerDB = new ResultManagerDB();

        int testId = resultManagerDB.createTest(config.getType(), args[0]);

        List<DBModelQuery> queries = new ArrayList<>();

        for (String model : config.getModels()) {
            for (String system : config.getSystems()) {
                for (Configuration.QueryFormat queryFormat : config.getQueryFormats()) {
                    // AxiomRenderer renderer = getAxiomRender(queryFormat, ontology);
                    try {
                        Cache cache = cacheManager.getCache(model, system);
                        SettingResult setting = resultManagerDB.getSetting(model, system, queryFormat.getName());
                        DBModelQuery modelQuery = new DBModelQuery(cache, model, system, queryFormat, config.getMaxTokens(), setting);
                        queries.add(modelQuery);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        for (String ontologyName : config.getOntologies()) {
            switch (config.getType()) {
                case "axiomQuerying" ->
                        new AxiomExperiment(queries, resultManagerDB, new ManchesterRender(new ClassName()), ontologyName, testId).runExperiment();
                case "classQuerying" ->
                        new ClassExperiment(queries, resultManagerDB, new ManchesterRender(new ClassName()), ontologyName, testId).runExperiment();
                default -> throw new IllegalStateException("Unexpected value: " + config.getType());
            }
        }
    }
}
