package org.experiments.exp3;

import org.experiments.Environment;
import org.experiments.logger.Cache;
import org.experiments.task.ExperimentTask;
import org.experiments.task.Task;
import org.experiments.workload.FalseWorkload;
import org.experiments.workload.OllamaWorkload;
import org.experiments.workload.OpenAIWorkload;

public class ModelQuery {
    private final Cache cache;
    private final String model;
    private final String system;
    private final String queryFormat;
    private final int maxTokens;

    public ModelQuery(Cache cache, String model, String system, String queryFormat, int maxTokens) {
        this.cache = cache;
        this.model = model;
        this.system = system;
        this.queryFormat = queryFormat;
        this.maxTokens = maxTokens;
    }


    public String result(String message) {
        message = message.replace("  ", " ");
        Runnable work;
        if (OllamaWorkload.supportedModels.contains(model)) {
            work = new OllamaWorkload(model, system, message, maxTokens, cache);
        } else if (OpenAIWorkload.supportedModels.contains(model)) {
            work = new OpenAIWorkload(model, system, message, maxTokens, cache);
        } else if (model.equals("false")) {
            work = new FalseWorkload(cache, message);
        } else {
            throw new IllegalStateException("Invalid model " + model);
        }
        Task task = new ExperimentTask("statementsQuerying", model, queryFormat, "", message, system, work);
        Environment.run(task, cache);

        return cache.resultString(message);
    }
}
