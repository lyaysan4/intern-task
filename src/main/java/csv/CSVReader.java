package csv;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CSVReader {

    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();
        properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("application.properties"));

        ExecutorService executor = Executors.newFixedThreadPool(10);

        File inputFiles = new File(properties.getProperty("input-directory"));
        File[] files = inputFiles.listFiles(file -> file.getPath().endsWith(".csv"));

        if (files != null) {
            for (File file : files) {
                executor.execute(new CSVParser(inputFiles.toPath(), file.toPath().getFileName(), properties.getProperty("output-directory")));
            }
        }

        new JavaDirectoryChangeListener(inputFiles.toPath(), executor, properties.getProperty("output-directory")).watchDirectory();
    }

}
