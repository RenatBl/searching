package main.java.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;
import org.jsoup.Jsoup;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class TextUtils {


    public static String removeHtmlFromText(String text) {
        return Jsoup.parse(text).text();
    }

    public static String removeSymbolsFromText(String text) {
        return text.replaceAll("[^A-Za-z]", " ").trim();
    }

    public static List<String> getWordsFromText(String text) {
        return Arrays.stream(text.split(" "))
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toList());
    }
}
