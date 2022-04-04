package main.java;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import lombok.SneakyThrows;
import main.java.utils.FileUtils;
import main.java.utils.TextUtils;

import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static main.java.utils.Constants.*;

/**
 * Данный класс выполняет токенизация и лемматизацию выгруженных HTML страниц
 */
public class PagesTokenizer {

    private static final List<String> TOKENS = new CopyOnWriteArrayList<>();
    private static final Map<String, List<String>> LEMMAS = new ConcurrentHashMap<>();

    @SneakyThrows
    public static void tokenizeAndLemmatize() {
        Properties props = new Properties();
        props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/english-left3words-distsim.tagger");
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        for (int i = 1; i <= DOCUMENTS_QUANTITY; i++) {
            System.out.printf("Start tokenize and lemmatize file: %s.txt%n", i);

            String text = FileUtils.readFromFile(OUTPUT_DIRECTORY + i + FILE_EXTENSION);
            text = TextUtils.removeHtmlFromText(text);
            text = TextUtils.removeSymbolsFromText(text);

            Annotation document = new Annotation(text);
            pipeline.annotate(document);

            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

            sentences.stream()
                    .map(sentence ->
                            sentence.get(CoreAnnotations.TokensAnnotation.class)
                    )
                    .flatMap(List::stream)
                    .forEach(token -> {
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                        putToken(word);
                        putLemma(lemma, word);
                    });
        }
        removeInvalidTokensAndLemmas();

        writeTokensToFile();
        writeLemmasToFile();
    }

    /**
     * Метод добавления токена в список токенов
     */
    private static void putToken(String word) {
        if (!TOKENS.contains(word)) {
            TOKENS.add(word);
        }
    }

    /**
     * Метод добавления леммы в словарь лемм
     */
    private static void putLemma(String lemma, String word) {
        if (LEMMAS.containsKey(lemma)) {
            List<String> words = LEMMAS.get(lemma);
            if (!words.contains(word)) {
                words.add(word);
                LEMMAS.put(lemma, words);
            }
        } else {
            LEMMAS.put(lemma, new ArrayList<>(List.of(word)));
        }
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
