package main.java;

import lombok.SneakyThrows;

import java.util.*;
import java.util.stream.Collectors;

import static main.java.Indexer.getPageLinksFromFile;
import static main.java.utils.Constants.*;
import static main.java.utils.FileUtils.readFromFile;
import static main.java.utils.FileUtils.readLinesFromFile;
import static main.java.utils.NlpUtils.lemmatizeSearchQuery;
import static main.java.utils.PageUtils.getPageResult;

public class Searcher {

    private static final Map<Integer, String> LINKS = getPageLinksFromFile();

    public static void vectorSearch() {
        Scanner in = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("Enter search query\n(enter 0 to exit)");
                String query = in.nextLine();
                if (query.equals("0")) {
                    return;
                }

                List<String> result = searchPages(query);
                System.out.println("Search result:");
                for (int i = 0; i < result.size(); i++) {
                    System.out.println(getPageResult(result.get(i), i + 1));
                }
                System.out.println();
            } catch (Exception e) {
                System.out.println("Something went wrong, please try again");
            }
        }
    }

    @SneakyThrows
    private static List<String> searchPages(String query) {
        List<String> queryLemmas = lemmatizeSearchQuery(query);
        Map<String, List<Integer>> invertedIndexes = getIndexesFromFile();
        Map<String, Integer> lemmasQuantityMap = lemmasQuantity(queryLemmas);
        Map<Integer, Double> pageIndexToScore = new HashMap<>();

        for (int i = 1; i <= DOCUMENTS_QUANTITY; i++) {
            List<String> lemmasTfIdfInfo = getLemmasTfIdfInfo(i);
            List<Double> vector = new ArrayList<>();
            List<Double> lemmaVector = new ArrayList<>();
            for (String lemmaInfo : lemmasTfIdfInfo) {
                String[] lemmaInfoArr = lemmaInfo.split(" ");

                String lemma = lemmaInfoArr[0];
                if (queryLemmas.contains(lemma)) {
                    double idf = Double.parseDouble(lemmaInfoArr[3]);
                    lemmaVector.add(idf);

                    double score = calculateScore(invertedIndexes, lemmasQuantityMap, lemma, idf);
                    vector.add(score);
                } else {
                    lemmaVector.add(0.0);
                    vector.add(0.0);
                }
            }
            Double cosineSimilarity = cosineSimilarity(vector, lemmaVector);
            pageIndexToScore.put(i, cosineSimilarity);
        }
        List<Integer> pages = pageIndexToScore.entrySet().stream()
                .filter(entry ->
                        !entry.getValue().equals(0.0)
                )
                .sorted((firstEntry, secondEntry) ->
                        secondEntry.getValue().compareTo(firstEntry.getValue())
                )
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return pages.stream()
                .map(LINKS::get)
                .collect(Collectors.toList());
    }

    /**
     * Метод расчёта приоритета страницы в результирующей выборке
     */
    private static double calculateScore(Map<String, List<Integer>> invertedIndexes,
                                         Map<String, Integer> lemmaToCount,
                                         String lemma,
                                         double idf) {
        return ((double) lemmaToCount.get(lemma) / invertedIndexes.get(lemma).size()) * idf;
    }

    /**
     * Метод вычисления частоты лемм в поисковом запросе
     */
    private static Map<String, Integer> lemmasQuantity(List<String> searchQueryLemmas) {
        Map<String, Integer> lemmasQuantityMap = new HashMap<>();
        for (String lemma : searchQueryLemmas) {
            if (lemmasQuantityMap.containsKey(lemma)) {
                lemmasQuantityMap.put(lemma, lemmasQuantityMap.get(lemma) + 1);
            } else {
                lemmasQuantityMap.put(lemma, 1);
            }
        }

        return lemmasQuantityMap;
    }

    /**
     * Метод получения индексов из файла indexes.txt
     */
    public static Map<String, List<Integer>> getIndexesFromFile() {
        Map<String, List<Integer>> indexesMap = new HashMap<>();
        String text = readFromFile(WORDS_DIRECTORY + INDEXES_FILE);
        String[] lines = text.split("\n");
        for (String line : lines) {
            String[] words = line.trim().split(" ");
            List<Integer> indexes = Arrays.asList(words)
                    .subList(1, words.length).stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
            indexesMap.put(words[0], indexes);
        }

        return indexesMap;
    }

    /**
     * Формирование строки на вывод
     */
    private static List<String> getLemmasTfIdfInfo(int filename) {
        return readLinesFromFile(TF_IDF_LEMMAS_DIRECTORY + filename + FILE_EXTENSION);
    }

    /**
     * Метод расчёта косинусного сходства для векторов
     */
    public static Double cosineSimilarity(List<Double> vector1, List<Double> vector2) {
        Double num = 0.0;
        double firstDenom = 0.0;
        double secondDenom = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            num += vector1.get(i) * vector2.get(i);
            firstDenom += vector1.get(i) * vector1.get(i);
            secondDenom += vector2.get(i) * vector2.get(i);
        }

        Double denom = Math.sqrt(firstDenom) * Math.sqrt(secondDenom);

        return (num.equals(0.0) || denom.equals(0.0)) ? 0.0 : num / denom;
    }
}
