package main.java;

import main.java.utils.FileUtils;
import main.java.utils.TextUtils;

import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static main.java.utils.Constants.*;
import static main.java.utils.NlpUtils.getInvertedLemmas;

/**
 * Данный класс является индексатором выгруженных страниц по полученным леммам
 */
public class Indexer {

    private static final Map<String, List<Integer>> INDEXES = new ConcurrentHashMap<>();
    private static final Map<Integer, String> LINKS = new ConcurrentHashMap<>();

    /**
     * Метод постоения инвертированных индексов
     */
    public static void index() {
        System.out.println("\n\n======================== 3) Indexing ========================\n\n");
        Map<String, List<String>> lemmasMap = getLemmasFromFile();
        Map<String, String> invertedLemmas = getInvertedLemmas(lemmasMap);

        for (int i = 1; i <= DOCUMENTS_QUANTITY; i++) {
            System.out.printf("Start index file: %s.txt%n", i);

            String text = FileUtils.readFromFile(OUTPUT_DIRECTORY + i + FILE_EXTENSION);
            text = TextUtils.removeHtmlFromText(text);
            text = TextUtils.removeSymbolsFromText(text);
            List<String> words = TextUtils.getWordsFromText(text);
            for (String word : words) {
                if (invertedLemmas.containsKey(word)) {
                    addIndex(invertedLemmas.get(word), i);
                }
            }
        }

        writeIndexesToFile();
        getPageLinksFromFile();
    }

    public static void booleanSearch() {
        System.out.println();
        Scanner in = new Scanner(System.in);
        try {
            while (true) {
                System.out.println("Enter search query by format: word1 <operator> word2 <operator> ...\n" +
                        "Where <operator> is & or |\n(enter 0 to exit)");
                String query = in.nextLine();
                if (query.equals("0")) {
                    return;
                }
                String[] words = query.trim().split(" ");
                List<Integer> pageIndexes = INDEXES.get(words[0]);
                List<Integer> searchQueryPageIndexes = new ArrayList<>();
                for (int i = 1; i < words.length; i += 2) {
                    String operator = words[i];
                    String word = words[i + 1];
                    if (INDEXES.containsKey(word)) {
                        searchQueryPageIndexes = INDEXES.get(word);
                    }
                    if (operator.equals("&")) {
                        pageIndexes = getMatchingElements(pageIndexes, searchQueryPageIndexes);
                    }
                    if (operator.equals("|")) {
                        pageIndexes = addAllElements(pageIndexes, searchQueryPageIndexes);
                    }
                }

                System.out.println("Result:");
                pageIndexes
                        .forEach(index -> System.out.println(LINKS.get(index)));
            }
        } catch (Exception e) {
            System.out.println("Something went wrong, please try again");
            booleanSearch();
        }
    }

    private static List<Integer> getMatchingElements(List<Integer> pageIndexes,
                                                     List<Integer> searchQueryPageIndexes) {
        Set<Integer> set = new HashSet<>();
        for (Integer pageIndex : pageIndexes) {
            for (Integer searchQueryPageIndex : searchQueryPageIndexes) {
                if (pageIndex.equals(searchQueryPageIndex)) {
                    set.add(pageIndex);
                }
            }
        }

        return new ArrayList<>(set);
    }

    private static List<Integer> addAllElements(List<Integer> pageIndexes,
                                                List<Integer> searchQueryPageIndexes) {
        Set<Integer> allElements = new HashSet<>();
        allElements.addAll(pageIndexes);
        allElements.addAll(searchQueryPageIndexes);

        return new ArrayList<>(allElements);
    }

    /**
     * Метод получения словаря мз пар имя файла - ссылка из файла index.txt
     */
    private static void getPageLinksFromFile() {
        String text = FileUtils.readFromFile(OUTPUT_DIRECTORY + OUTPUT_FILE);
        String[] lines = text.trim().split("\n");
        for (String line : lines) {
            String[] words = line.trim().split("\\) ");
            LINKS.put(Integer.parseInt(words[0]), words[1]);
        }
    }

    /**
     * Метод получения лемм из файла lemmas.txt
     */
    public static Map<String, List<String>> getLemmasFromFile() {
        Map<String, List<String>> lemmasMap = new HashMap<>();
        String text = FileUtils.readFromFile(WORDS_DIRECTORY + LEMMAS_FILE);
        String[] lines = text.split("\n");
        for (String line : lines) {
            String[] words = line.trim().split(" ");
            List<String> lemmas = new ArrayList<>(Arrays.asList(words).subList(1, words.length));
            lemmasMap.put(words[0], lemmas);
        }

        return lemmasMap;
    }

    /**
     * Метод добавления индекса в словарь
     */
    private static void addIndex(String lemma, Integer index) {
        if (INDEXES.containsKey(lemma)) {
            List<Integer> indexes = INDEXES.get(lemma);
            if (!indexes.contains(index)) {
                indexes.add(index);
                INDEXES.put(lemma, indexes);
            }
        } else {
            INDEXES.put(lemma, new ArrayList<>(List.of(index)));
        }
    }

    /**
     * Метод записи индексов в файл
     */
    private static void writeIndexesToFile() {
        for (Map.Entry<String, List<Integer>> entry : INDEXES.entrySet()) {
            String lemma = entry.getKey() + " " + entry.getValue().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(" "));
            FileUtils.writeToFile(WORDS_DIRECTORY + INDEXES_FILE, lemma, StandardOpenOption.APPEND);
        }
    }
}
