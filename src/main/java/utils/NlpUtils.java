package main.java.utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class NlpUtils {

    private static final StanfordCoreNLP PIPELINE;

    static {
        Properties props = new Properties();
        props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/english-left3words-distsim.tagger");
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        PIPELINE = new StanfordCoreNLP(props);
    }

    /**
     * Метод инициализации словарей токенов и лемм
     */
    public static void putTokensAndLemmas(String text, List<String> tokens, Map<String, List<String>> lemmas) {
        Annotation document = new Annotation(text);
        PIPELINE.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        sentences.stream()
                .map(sentence ->
                        sentence.get(CoreAnnotations.TokensAnnotation.class)
                )
                .flatMap(List::stream)
                .forEach(token -> {
                    String word = token.get(CoreAnnotations.TextAnnotation.class);
                    String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

                    putToken(word, tokens);
                    putLemma(lemma, word, lemmas);
                });

        StanfordCoreNLP.clearAnnotatorPool();
    }

    /**
     * Метод добавления токена в список токенов
     */
    private static void putToken(String word, List<String> tokens) {
        if (!tokens.contains(word)) {
            tokens.add(word);
        }
    }

    /**
     * Метод добавления леммы в словарь лемм
     */
    private static void putLemma(String lemma, String word, Map<String, List<String>> lemmas) {
        if (lemmas.containsKey(lemma)) {
            List<String> words = lemmas.get(lemma);
            if (!words.contains(word)) {
                words.add(word);
                lemmas.put(lemma, words);
            }
        } else {
            lemmas.put(lemma, new ArrayList<>(List.of(word)));
        }
    }

    /**
     * Метод получения инвертированного словаря из пар токен - лемма
     */
    public static Map<String, String> getInvertedLemmas(Map<String, List<String>> lemmasMap) {
        Map<String, String> invertedLemmas = new HashMap<>();
        lemmasMap.forEach((key, value) -> {
            for (String s : value) {
                invertedLemmas.put(s, key);
            }
        });

        return invertedLemmas;
    }

    /**
     * Метод лемматизации поискового запроса
     */
    public static List<String> lemmatizeSearchQuery(String text) {
        Annotation annotation = new Annotation(text.toLowerCase(Locale.ROOT));
        PIPELINE.annotate(annotation);

        return annotation.get(CoreAnnotations.SentencesAnnotation.class).stream()
                .map(sentence -> sentence.get(CoreAnnotations.TokensAnnotation.class))
                .flatMap(List::stream)
                .map(token -> token.get(CoreAnnotations.LemmaAnnotation.class))
                .distinct()
                .collect(Collectors.toList());
    }
}
