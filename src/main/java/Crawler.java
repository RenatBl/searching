import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Crawler {

    private static final AtomicInteger i = new AtomicInteger(1);
    private static final CopyOnWriteArrayList<String> DOWNLOADED_PAGES = new CopyOnWriteArrayList<>();
    private static final String INPUT_FILE = "links.txt";
    private static final String OUTPUT_FILE = "index.txt";
    private static final String FILE_EXTENSION = ".txt";
    private static final String INPUT_DIRECTORY = "input\\";
    private static final String OUTPUT_DIRECTORY = "downloaded\\";

    public static void main(String[] args) {
        String url = readFromFile();
        downloadPages(url);
    }

    @SneakyThrows
    private static void downloadPages(final String url) {
        Document doc = Jsoup.connect(url)
                .get();

        DOWNLOADED_PAGES.add(url);
        writeLinkToFile(String.valueOf(i.get()), url);
        writePageToFile(doc, String.valueOf(i.get()));
        i.incrementAndGet();

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String linkUrl = link.attr("abs:href");
            if (i.get() <= 100 && !DOWNLOADED_PAGES.contains(linkUrl)) {
                try {
                    downloadPages(linkUrl);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private static void writeLinkToFile(String count, String url) {
        String str = count + ") " + url + "\n";

        writeToFile(OUTPUT_DIRECTORY + OUTPUT_FILE, str, StandardOpenOption.APPEND);
    }

    private static void writePageToFile(Document doc, String filename) {
        String page = doc.toString();

        writeToFile(OUTPUT_DIRECTORY + filename + FILE_EXTENSION, page, StandardOpenOption.CREATE);
    }

    @SneakyThrows
    private static void writeToFile(String fileName, String str, OpenOption openOption) {
        Path file = Paths.get(fileName);
        Files.write(file, List.of(str), StandardCharsets.UTF_8, openOption);
    }

    @SneakyThrows
    private static String readFromFile() {
        FileInputStream fis = new FileInputStream(INPUT_DIRECTORY + INPUT_FILE);

        return IOUtils.toString(fis, StandardCharsets.UTF_8);
    }
}
