// package crawler;
// This class is used to crawl the web.
// using the web crawler, we can crawl the web and get the links of the web pages.
// to make a search engine, we can use the web crawler to crawl the web and get the links of the web pages.
// the seed is the starting point of the web crawler.
// the seed is found in text file called seedURLs.txt

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jetbrains.annotations.Nullable;



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
    public static Queue<String> blockedUrls = new LinkedList<>();
    public static HashMap<String, Boolean> parsedRobots = new HashMap<>();
    public static Hashtable<String, String> urlToHtml = new Hashtable<>();

    public Crawler() {
    }

    public static @Nullable String normalize_url(String url) throws MalformedURLException {
        try {
            URL url_object = new URL(url.trim());
            String query = (url_object.getQuery()) == null ? "" : "?" + url_object.getQuery();
            return url_object.getProtocol() + "://" + url_object.getHost() + url_object.getPath() + query;
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    public static void parse_robot(String url) {
        try {
            URL link = new URL(url);
            String robots_txt_url =  link.getProtocol() + "://" + link.getHost() + "/robots.txt";
            if (parsedRobots.containsKey(robots_txt_url))
                return;
            BufferedReader read = new BufferedReader(new InputStreamReader(new URL(robots_txt_url).openStream()));
            String line = "";
            while ((line = read.readLine()) != null) {
                if (line.toLowerCase().startsWith("user-agent")) {
                    if (line.contains("*")) {
                        while ((line = read.readLine()) != null) {
                            if (line.toLowerCase().startsWith("disallow")) {
                                String disallow_url = line.split(":")[1].trim();
                                blockedUrls.add(link.getProtocol() + "://" + link.getHost() + disallow_url);
                            }
                            else if (line.toLowerCase().startsWith("user-agent")) {
                                break;
                            }
                            else if (line.toLowerCase().startsWith("sitemap")) {
                                parse_sitemap_xml(line.split(":")[1].trim());
                            }
                        }
                    }
                }
                else if (line.toLowerCase().startsWith("sitemap")) {
                    parse_sitemap_xml(line.split(":")[1].trim());
                }
                parsedRobots.put(robots_txt_url, true);
            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            // System.out.println("Couldn't parse robot.txt for: " + robots_txt_url.replace("/robots.txt", ""));
        }
    }

    public static boolean is_url_allowed(String url) {
        parse_robot(url);
        String extension = FilenameUtils.getExtension(url);
        for (String blocked_extension : BLOCKED_EXTENSIONS)
            if (extension.equals(blocked_extension))
                return false;
        for (String blocked_url : blockedUrls)
        {
            try {
                if ((new URL(url).getHost().equals(new URL(blocked_url).getHost()) && url.endsWith(extension)) || url.equals(blocked_url))
                    return false;
            }
            catch (MalformedURLException e) {
                System.out.println("Wrong URL:" + url);
            }
        }
        return true;
    }

    public static boolean parse_sitemap_xml(String url) {
        try {
            URL temp = new URL(url);
            String sitemap = url + "sitemap.xml";
            Document doc = Jsoup.connect(sitemap).get();
            for (Element loc : doc.select("loc")) {
                String absolute_link = loc.attr("abs:href");
                absolute_link.replace("/sitemap.xml", "");
                absolute_link.replace("/sitemaps.xml", "");
                if (is_url_allowed(absolute_link) && !linksQueue.contains(absolute_link)) {
                    linksQueue.add(normalize_url(absolute_link));
                    System.out.println("Crawl Added: " + absolute_link);
                    System.out.println("Current Size: " + linksQueue.size());
                }
            }
            return true;
        }
        catch (IOException e) {
            System.out.println("Couldn't parse sitemap.xml for url:" + url);
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static boolean parse_url_html(String url) {
        try {
            URL temp = new URL(url);
            String html = url + "/";

            Document document = Jsoup.connect(html).get();
            Elements links = document.select("a[href]");
            for (Element link : links) {
                String absolute_link = link.attr("abs:href");
                if (is_url_allowed(absolute_link)) {
                    if (!linksQueue.contains(absolute_link) && is_url_allowed(absolute_link)) {
                        linksQueue.add(normalize_url(absolute_link));
                        System.out.println("Crawl Added: " + absolute_link);
                        System.out.println("Current Size: " + linksQueue.size());
                    }
                }
            }

            return true;
        }
        catch (IOException e) {
            System.out.println("couldn't parse url:" + url);
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static void download_html(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            String html = doc.html();
            String file_name = doc.title() + ".html";
            File file = new File(OUTPUT_DIRECTORY + file_name);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(html);
            urlToHtml.put(url, file_name);
            writer.close();
        }
        catch (IOException e) {
            System.out.println("Couldn't download html for url:" + url);
            System.out.println(e.getMessage());
        }
    }

    public static void SeedCrawler() throws java.io.IOException {
        File directory = new File(OUTPUT_DIRECTORY);
        if (!directory.exists())
            directory.mkdirs();
        File seed = new File(SEED_FILE);
        Scanner scanner = new Scanner(seed);
        while (scanner.hasNextLine()) {
            Crawler.linksQueue.add(normalize_url(scanner.nextLine()));
            System.out.println("Crawl Added: " + normalize_url(scanner.nextLine()));
            System.out.println("Current Size: " + linksQueue.size());
        }
    }

    @Override
    public void run() {
        while (!linksQueue.isEmpty()) {
            String url = linksQueue.remove();
            if (url.contains("sitemap.xml") || url.contains("sitemaps.xml")) {
                parse_sitemap_xml(url);
                continue;
            }
            parse_url_html(url);
            download_html(url);
        }
    }

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        SeedCrawler();
        Thread [] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(new Crawler());
            threads[i].start();
        }
        try{
            for (int i = 0; i < NUM_THREADS; i++) {
                threads[i].join();
            }
        }
        catch (InterruptedException e) {
            System.out.println("Thread interrupted");
        }
    }
}
