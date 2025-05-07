package org.experiments;

import org.experiments.logger.Cache;
import org.experiments.logger.SmartLogger;
import org.experiments.task.Task;
import static org.experiments.logger.SmartLogger.isFileInCache;
import static org.experiments.logger.SmartLogger.isQueryInCache;

public class Environment {

    public static void run(Task task, Cache cache) {
        if (cache == null) {
            run(task);
            return;
        }
        Boolean isTrue = cache.isStrictlyTrue(task.getQuery());
        if (isTrue == null) {
            task.run();
        }
    }

    public static void run(Task task) {
        // Setup logging
        String filename = task.getFileName();
        String query = task.getQuery();

        // If filename is already present in the cache, then skip the task
        if (isFileInCache(filename) && isQueryInCache(filename, query)) {
            System.out.println("Task " + task + " is already present in the cache.");
        } else {
            // Enable file logging
            try {
                SmartLogger.enableFileLogging(filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Run the task
            task.run();
            // Disable file logging
            SmartLogger.disableFileLogging();
            SmartLogger.log(task.getTaskName() + " is completed.");
        }
    }
}
