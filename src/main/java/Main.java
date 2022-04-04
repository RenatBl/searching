package main.java;

import static main.java.Crawler.downloadPages;
import static main.java.Indexer.booleanSearch;
import static main.java.Indexer.index;
import static main.java.PagesTokenizer.tokenizeAndLemmatize;

public class Main {

    public static void main(String[] args) {
        downloadPages();
        tokenizeAndLemmatize();
        index();
        booleanSearch();
    }
}
