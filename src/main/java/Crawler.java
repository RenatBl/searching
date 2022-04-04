package main.java;

import lombok.SneakyThrows;
import main.java.utils.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static main.java.utils.Constants.*;

/**
 * Данный класс представляет из себя поискового робота для перебора страниц и их выгрузки в файлы
 */
public class Crawler {

    private static final AtomicInteger i = new AtomicInteger(1);
    private static final CopyOnWriteArrayList<String> DOWNLOADED_PAGES = new CopyOnWriteArrayList<>();
    private static final String URL;
    private static final String PAGE_PATTERN;

    static {
        try {
            URL = FileUtils.readFromFile(INPUT_DIRECTORY + INPUT_FILE);
            PAGE_PATTERN = new URL(URL).getHost();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    @SneakyThrows
    public static void downloadPages() {
        FileUtils.clearFiles();

        downloadPages(URL);
    }

    /**
     * Метод рекурсивной выгрузки страниц
     * На переданной странице парсятся все ссылки,
     * по которым происходит дальнейшая выгрузка страниц
     */
    @SneakyThrows
    private static void downloadPages(final String url) {
        System.out.printf("Start download %s page %s%n", i.get(), url);
        Document doc = Jsoup.connect(url)
                .get();

        DOWNLOADED_PAGES.add(url);
        writeLinkToFile(String.valueOf(i.get()), url);
        writePageToFile(doc, String.valueOf(i.get()));
        i.incrementAndGet();

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String linkUrl = link.attr("abs:href");
            if (i.get() <= DOCUMENTS_QUANTITY && !DOWNLOADED_PAGES.contains(linkUrl) && linkUrl.contains(PAGE_PATTERN)) {
                try {
                    downloadPages(linkUrl);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    /**
     * Метод записи ссылки на страницу в файл
     */
    private static void writeLinkToFile(String count, String url) {
        String str = count + ") " + url;

        FileUtils.writeToFile(OUTPUT_DIRECTORY + OUTPUT_FILE, str, StandardOpenOption.APPEND);
    }

    /**
     * Метод записи HTML страницы в файл
     */
    private static void writePageToFile(Document doc, String filename) {
        String page = doc.toString();

        FileUtils.writeToFile(OUTPUT_DIRECTORY + filename + FILE_EXTENSION, page, StandardOpenOption.CREATE);
    }
}
