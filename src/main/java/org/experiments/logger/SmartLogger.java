package org.experiments.logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SmartLogger {

    private static final Logger logger = Logger.getLogger(SmartLogger.class.getName());
    private static final String CACHE_DIR = "cache";
    private static final String FILE_EXTENSION = ".csv";
    private static final String WARNING_FILE = "warnings.txt";

    private static final Set<String> deletionSet = new HashSet<>();

    public static String getFullFileName(String filename) {
        return CACHE_DIR + System.getProperty("file.separator") + filename + FILE_EXTENSION;
    }

    public static String getFullFileName(String filename, boolean cache) {
        return cache ? getFullFileName(filename) : filename + FILE_EXTENSION;
    }

    public static void log(String message) {
        logger.info(message+"\n");
    }

    public static void removeFileFromCache(String filename) {
        File file = new File(getFullFileName(filename));
        if (file.exists()) {
            file.delete();
        }
    }

    public static void enableFileLogging(String filename) {
        enableFileLogging(filename, "");
    }

    public static void enableFileLogging(String filename, boolean cache) {
        enableFileLogging(filename, "", cache);
    }

    public static void enableFileLogging(String filename, String lineTerminator) {
        // If cache directory does not exist, then create it
        enableFileLogging(filename, lineTerminator, true);
    }

    public static void enableFileLogging(String filename, String lineTerminator, boolean cache) {
        // If cache directory does not exist, then create it
        if (!new File(CACHE_DIR).exists()) {
            new File(CACHE_DIR).mkdir();
        }
        try {
            // Create a new FileHandler with a custom formatter
            Handler fileHandler = new FileHandler(getFullFileName(filename, cache), true);
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(java.util.logging.LogRecord record) {
                    return record.getMessage() + lineTerminator;
                }
            });
            logger.addHandler(fileHandler);
            // Save in a temporary file the name of the file that is being logged
            java.nio.file.Files.write(java.nio.file.Paths.get("logging_file_name.txt"), filename.getBytes());
        } catch (IOException e) {
            System.err.println("Error setting up FileHandler: " + e.getMessage());
        }
    }

    public static void disableFileLogging() {
        if (logger.getHandlers().length > 0) {
            logger.getHandlers()[0].close();
            logger.removeHandler(logger.getHandlers()[0]);
            // Delete the temporary file that contains the name of the file that was being logged
            new File("logging_file_name.txt").delete();
        }
    }

    public static String getFilename() {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("logging_file_name.txt")));
        } catch (IOException e) {
            System.err.println("Error reading logging file name: " + e.getMessage());
            return "";
        }
    }

    /**
     * Check if the file is already present in the cache
     *
     * @param filename the name of the file to check
     * @return true if the file is present in the cache, false otherwise
     */
    public static boolean isFileInCache(String filename) {
        // If cache directory does not exist, then return false
        if (!new File(CACHE_DIR).exists()) {
            return false;
        } else {
            // If the file is present in the cache, then return true
            File file = new File(getFullFileName(filename));
            return file.exists();
        }
    }

    /**
     * Check if the query is already present in the cache
     *
     * @param filename the name of the file to check
     * @param query the query to check
     * @return true if the query is present in the cache, false otherwise
     */
    public static boolean isQueryInCache(String filename, String query) {
        try (BufferedReader reader = new BufferedReader(new FileReader(getFullFileName(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length == 2 && columns[0].trim().equals(query.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check cache's files integrity
     * If a file does not contain the answer, then remove it from the cache.
     * If the answer does not contain "True" or "False", then append the file name to the warning file.
     */
    public static void checkCachedFiles() {
        File cacheDir = new File(CACHE_DIR);
        if (cacheDir.exists()) {
            List<File> directories = Arrays.stream(Objects.requireNonNull(cacheDir.listFiles())).toList();
            if (!directories.isEmpty()) {
                directories.parallelStream().forEach(dir -> {
                    if (dir.isDirectory()) {
                        var files = Arrays.stream(Objects.requireNonNull(dir.listFiles())).toList();
                        files.parallelStream().forEach(SmartLogger::fixCachedFile);
                    } else {
                        fixCachedFile(dir);
                    }
                });
            }
        }
    }

    /**
     * Fix the file by removing empty lines and lines with empty second column
     * If the file is empty, then remove it
     *
     * @param file the file to fix
     */
    private static void fixCachedFile(File file) {
        Path path = file.toPath();

        try {
            if (Files.size(path) == 0) {
                Files.delete(path);
                System.out.println("Removed empty file: " + file.getName());
                return;
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        List<String> cleanedLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                if (columns.length >= 2 && !columns[1].trim().isEmpty()) {
                    cleanedLines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String cleanedLine : cleanedLines) {
                writer.write(cleanedLine);
                writer.newLine();
            }
            System.out.println("Fixed file: " + file.getName());
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

}