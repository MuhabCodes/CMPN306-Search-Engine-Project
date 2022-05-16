// package crawler;
// This class is used to crawl the web.
// using the web crawler, we can crawl the web and get the links of the web pages.
// to make a search engine, we can use the web crawler to crawl the web and get the links of the web pages.
// the seed is the starting point of the web crawler.
// the seed is found in text file called seedURLs.txt

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.net.*;

public class Crawler implements Runnable {
    private static final int MIN_PAGES_TO_CRAWL = 5000;
    private static final int MAX_LINK_DEPTH = 10;
    // These 2 path strings need to be updated to relative pathing
    private static final String SEED_FILE = "./seedURLs.txt";
    private static final String OUTPUT_DIRECTORY = "./html_docs/";
    private static final int NUM_THREADS = 1;
    // list of blocked extensions
    private static final List<String> BLOCKED_EXTENSIONS = new ArrayList<>(List.of(
            ".mp4",
            ".webm",
            ".gif",
            ".mov",
            ".flv"
            ));

    public static Queue<String> linksQueue = new LinkedList<>();

    public Crawler() {
    }

    private static boolean is_url_blocked(String url) {
        for (String extension : BLOCKED_EXTENSIONS)
            if (url.endsWith(extension))
                return true;

        return false;
    }

    public static @Nullable String normalize_url(String url) throws MalformedURLException {
        try {
            URL url_object = new URL(url.trim().toLowerCase());
            return "http://" + url_object.getHost() + url_object.getPath();
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    public void SeedCrawler() throws java.io.IOException {
        File seed = new File(SEED_FILE);
        Scanner scanner = new Scanner(seed);
        while (scanner.hasNextLine()) {
            Crawler.linksQueue.add(normalize_url(scanner.nextLine()));
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
    }

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        Crawler crawler = new Crawler();
        crawler.SeedCrawler();
    }
}
