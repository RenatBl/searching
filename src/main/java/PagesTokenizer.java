package main.java;

import lombok.SneakyThrows;
import main.java.utils.FileUtils;
import main.java.utils.TextUtils;

import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static main.java.utils.Constants.*;
import static main.java.utils.NlpUtils.putTokensAndLemmas;

/**
 * Данный класс выполняет токенизация и лемматизацию выгруженных HTML страниц
 */
public class PagesTokenizer {

    private static final List<String> TOKENS = new CopyOnWriteArrayList<>();
    private static final Map<String, List<String>> LEMMAS = new ConcurrentHashMap<>();

    @SneakyThrows
    public static void tokenizeAndLemmatize() {
        System.out.println("\n\n======================== 2) Pages tokenize and lemmatize ========================\n\n");
        for (int i = 1; i <= DOCUMENTS_QUANTITY; i++) {
            System.out.printf("Start tokenize and lemmatize file: %s.txt%n", i);

            String text = FileUtils.readFromFile(OUTPUT_DIRECTORY + i + FILE_EXTENSION);
            text = TextUtils.removeHtmlFromText(text);
            text = TextUtils.removeSymbolsFromText(text);

            putTokensAndLemmas(text, TOKENS, LEMMAS);
        }
        removeInvalidTokensAndLemmas();

        writeTokensToFile();
        writeLemmasToFile();
    }

    /**
     * Метод удаления невалидных токенов и лемм
     */
    private static void removeInvalidTokensAndLemmas() {
        TOKENS.forEach(token -> {
            if (token.length() <= 1) {
                TOKENS.remove(token);
            }
        });

        LEMMAS.forEach((key, value) -> {
            if (key.length() <= 1 || !value.contains(key)) {
                LEMMAS.remove(key);
            }
        });
    }

    /**
     * Метод записи токенов в файл
     */
    private static void writeTokensToFile() {
        for (String token : TOKENS) {
            FileUtils.writeToFile(WORDS_DIRECTORY + TOKENS_FILE, token, StandardOpenOption.APPEND);
        }
    }

    /**
     * Метод записи лемм в файл
     */
    private static void writeLemmasToFile() {
        for (Map.Entry<String, List<String>> entry : LEMMAS.entrySet()) {
            String lemma = entry.getKey() + " " + String.join(" ", entry.getValue());
            FileUtils.writeToFile(WORDS_DIRECTORY + LEMMAS_FILE, lemma, StandardOpenOption.APPEND);
        }
    }
}
