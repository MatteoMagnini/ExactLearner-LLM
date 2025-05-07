package org.experiments.workload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.FileSystems;

public class WorkLoadCounter {
    private final Writer writer;

    public WorkLoadCounter(String info, int limit, int base) {
        String sep = FileSystems.getDefault().getSeparator();
        String path = "analysis" + sep + "counter" + sep + info + "_" + limit + "_" + base + ".txt";

        // Create the file object
        File file = new File(path);

        // Ensure the directory exists
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); // Create the necessary directories
        }

        try {
            writer = new BufferedWriter(new FileWriter(file, true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void task(String task) {
        try {
            writer.append(task).append("\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
