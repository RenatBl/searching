package main.java;

import main.java.utils.FileUtils;
import main.java.utils.TextUtils;

import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static main.java.utils.Constants.*;
import static main.java.utils.FileUtils.readFromFile;
import static main.java.utils.NlpUtils.getInvertedLemmas;
import static main.java.utils.NlpUtils.putTokensAndLemmas;
import static main.java.utils.TextUtils.getWordsFromText;

public class TfIdfCalculator {

    private static final Map<Integer, List<String>> PAGES = readDownloadedPages();

    public static void calculateTfAndIdf() {
        System.out.println("\n\n======================== 4) TF and IDF calculating ========================\n\n");

        for (int i = 1; i <= DOCUMENTS_QUANTITY; i++) {
            System.out.printf("Start calculate TF and IDF for file: %s.txt%n", i);

            List<String> tokens = new ArrayList<>();
            Map<String, List<String>> lemmas = new HashMap<>();

            String text = readFromFile(OUTPUT_DIRECTORY + i + FILE_EXTENSION);
            text = TextUtils.removeHtmlFromText(text);
            text = TextUtils.removeSymbolsFromText(text);
            List<String> words = getWordsFromText(text);

            putTokensAndLemmas(text, tokens, lemmas);
            Map<String, String> invertedLemmas = getInvertedLemmas(lemmas);

            List<String> tokensTfIdfInfo = new ArrayList<>();
            List<String> lemmasTfIdfInfo = new ArrayList<>();
            for (String word : words.stream().distinct().collect(Collectors.toList())) {
                if (tokens.contains(word)) {
                    double tokenTf = calculateTf(List.of(word), words);
                    double tokenIdf = calculateIdf(List.of(word));
                    double tokenTfIdf = tokenTf * tokenIdf;

                    tokensTfIdfInfo.add(word + " " + tokenTf + " " + tokenIdf + " " + tokenTfIdf);
                }

                if (invertedLemmas.containsKey(word)) {
                    String lemma = invertedLemmas.get(word);
                    double lemmaTf = calculateTf(lemmas.get(lemma), words);
                    double lemmaIdf = calculateIdf(lemmas.get(lemma));
                    double lemmaTfIdf = lemmaTf * lemmaIdf;

                    lemmasTfIdfInfo.add(word + " " + lemmaTf + " " + lemmaIdf + " " + lemmaTfIdf);
                }
            }
            writeWordTfIdfInfo(tokensTfIdfInfo, TF_IDF_TOKENS_DIRECTORY + i + FILE_EXTENSION);
            writeWordTfIdfInfo(lemmasTfIdfInfo, TF_IDF_LEMMAS_DIRECTORY + i + FILE_EXTENSION);
        }
    }

    private static double calculateTf(List<String> terms, List<String> words) {
        double result = 0;
        for (String word : words) {
            if (terms.stream().anyMatch(term -> term.equalsIgnoreCase(word)))
                result++;
        }

        return result / words.size();
    }

    private static double calculateIdf(List<String> terms) {
        double n = 0;
        for (Map.Entry<Integer, List<String>> pageEntry : PAGES.entrySet()) {
            for (String word : pageEntry.getValue()) {
                if (terms.stream().anyMatch(term -> term.equalsIgnoreCase(word))) {
                    n++;
                    break;
                }
            }
        }

        return Math.log(DOCUMENTS_QUANTITY / n);
    }

    private static Map<Integer, List<String>> readDownloadedPages() {
        Map<Integer, List<String>> pages = new HashMap<>();
        for (int i = 1; i <= DOCUMENTS_QUANTITY; i++) {
            String text = readFromFile(OUTPUT_DIRECTORY + i + FILE_EXTENSION);
            text = TextUtils.removeHtmlFromText(text);
            text = TextUtils.removeSymbolsFromText(text);
            List<String> words = getWordsFromText(text);

            pages.put(i, words);
        }

        return pages;
    }

    private static void writeWordTfIdfInfo(List<String> tfIdfInfo, String filepath) {
        FileUtils.writeToFile(filepath, String.join("\n", tfIdfInfo), StandardOpenOption.CREATE);
    }
}
