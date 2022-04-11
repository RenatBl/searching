package main.java.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;

@UtilityClass
public class PageUtils {

    @SneakyThrows
    public static String getPageResult(String url, Integer number) {
        String title = Jsoup.connect(url).get().title();

        return number + ") " + title + " - " + url;
    }
}
