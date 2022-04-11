package main.java;

import java.util.Scanner;

import static main.java.Crawler.downloadPages;
import static main.java.Indexer.booleanSearch;
import static main.java.Indexer.index;
import static main.java.PagesTokenizer.tokenizeAndLemmatize;
import static main.java.Searcher.vectorSearch;
import static main.java.TfIdfCalculator.calculateTfAndIdf;

public class Main {

    public static void main(String[] args) {
        downloadPages();
        tokenizeAndLemmatize();
        index();
        calculateTfAndIdf();

        System.out.println("\n");
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("Choose searching type:");
            System.out.println("1) Boolean");
            System.out.println("2) Vector");
            System.out.println("(enter 0 to exit)");

            String type = in.nextLine();
            switch (type) {
                case "1":
                    booleanSearch();
                    break;
                case "2":
                    vectorSearch();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Something went wrong, please try again");
                    break;
            }
        }
    }
}
