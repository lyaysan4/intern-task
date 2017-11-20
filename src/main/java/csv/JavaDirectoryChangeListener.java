package csv;

import java.nio.file.*;
import java.util.concurrent.ExecutorService;

public class JavaDirectoryChangeListener {

    private final Path directoryPath;
    private final ExecutorService executorService;
    private final String outputDirectory;

    public JavaDirectoryChangeListener(Path directoryPath, ExecutorService executorService, String outputDirectory) {
        this.directoryPath = directoryPath;
        this.executorService = executorService;
        this.outputDirectory = outputDirectory;
    }

    public void watchDirectory() {
        try (WatchService watchService = directoryPath.getFileSystem().newWatchService()) {
            directoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            while (true) {
                WatchKey watchKey = watchService.take();
                for (final WatchEvent<?> event : watchKey.pollEvents()) {
                    takeActionOnChangeEvent(event);
                }
                if (!watchKey.reset()) {
                    watchKey.cancel();
                    watchService.close();
                    System.out.println("Watch directory was deleted. Stop watching it.");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void takeActionOnChangeEvent(WatchEvent<?> event) {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            Path entryCreated = (Path) event.context();
            executorService.execute(new CSVParser(directoryPath, entryCreated, outputDirectory));
            System.out.println("New entry created: " + entryCreated);
        }
    }

}
