package main.java;

import static main.java.Crawler.downloadPages;
import static main.java.Indexer.booleanSearch;
import static main.java.Indexer.index;
import static main.java.PagesTokenizer.tokenizeAndLemmatize;
import static main.java.TfIdfCalculator.calculateTfAndIdf;

public class Main {

    public static void main(String[] args) {
        downloadPages();
        tokenizeAndLemmatize();
        index();
        booleanSearch();
        calculateTfAndIdf();
    }
}
