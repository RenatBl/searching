package main.java.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static main.java.utils.Constants.*;

@UtilityClass
public class FileUtils {

    @SneakyThrows
    public static void writeToFile(String fileName, String str, OpenOption openOption) {
        Path file = Paths.get(fileName);
        Files.write(file, List.of(str), StandardCharsets.UTF_8, openOption);
    }

    @SneakyThrows
    public static String readFromFile(String filePath) {
        FileInputStream fis = new FileInputStream(filePath);

        return IOUtils.toString(fis, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static List<String> readLinesFromFile(String filePath) {
        FileInputStream fis = new FileInputStream(filePath);

        return IOUtils.readLines(fis, StandardCharsets.UTF_8);
    }

    public static void clearFiles() {
        List<String> files = List.of(
                OUTPUT_DIRECTORY + OUTPUT_FILE,
                WORDS_DIRECTORY + TOKENS_FILE,
                WORDS_DIRECTORY + LEMMAS_FILE,
                WORDS_DIRECTORY + INDEXES_FILE
        );

        files.forEach(fileName -> {
            try {
                File file = new File(fileName);
                PrintWriter writer = new PrintWriter(file);
                writer.print("");
                writer.close();
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
