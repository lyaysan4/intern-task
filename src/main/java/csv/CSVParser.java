package csv;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.google.common.io.Files.getNameWithoutExtension;
import static java.util.stream.Collectors.*;


public class CSVParser implements Runnable {

    private final Path directoryPath;
    private final Path filePath;
    private final File outputDirectory;

    private int tryCounter = 0;

    public CSVParser(Path directoryPath, Path filePath, String outputDirectory) {
        this.directoryPath = directoryPath;
        this.filePath = filePath;
        this.outputDirectory = new File(outputDirectory);
    }

    @Override
    public void run() {
        tryCounter++;
        List<Output> outputs;
        try (FileReader fileReader = new FileReader(directoryPath.toString() + "\\" + filePath.toString())) {
            outputs = new CsvToBeanBuilder<Output>(fileReader)
                    .withType(Output.class).build().parse().stream()
                    .flatMap(output -> output.divideByDays().stream())
                    .collect(toList());

            Map<String, List<Output>> sortedAndGroupedByDay = outputs.stream().collect(groupingBy(Output::getShortDate, TreeMap::new, mapping(out -> out, toList())));
            Map<String, List<Output>> map = new TreeMap<>();

            for (Map.Entry<String, List<Output>> entry : sortedAndGroupedByDay.entrySet()) {
                for (Output output : entry.getValue()) {
                    map.putIfAbsent(entry.getKey(), new ArrayList<>());
                    map.get(entry.getKey()).add(Output.avgTime(output, output.getSame(entry.getValue())));
                }
                List<Output> list = map.get(entry.getKey()).stream().distinct().collect(toList());
                map.put(entry.getKey(), list);
            }

            try (FileWriter fileWriter = new FileWriter(outputDirectory + "\\" + getNameWithoutExtension(filePath.toString()) + ".txt")) {
                for (Map.Entry<String, List<Output>> entry : map.entrySet()) {
                    fileWriter.write(entry.getKey() + "\r\n\r\n" + Joiner.on("\n").join(entry.getValue()) + "\r\n\r\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (tryCounter < 5) {
                run();
            }
        }
    }
}
