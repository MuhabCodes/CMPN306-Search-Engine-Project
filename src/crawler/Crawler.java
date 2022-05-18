// package crawler;
// This class is used to crawl the web.
// using the web crawler, we can crawl the web and get the links of the web pages.
// to make a search engine, we can use the web crawler to crawl the web and get the links of the web pages.
// the seed is the starting point of the web crawler.
// the seed is found in text file called seedURLs.txt

import java.io.*;
import javax.xml.parsers.*;
import java.net.*;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import java.util.*;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
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
            return url_object.getProtocol() + "://" + url_object.getHost() + url_object.getPath();
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    public static void parse_robot(String url) {
        try {
            URL link = new URL(url);
            String robots_txt_url =  normalize_url(url) + "robots.txt";
            boolean start_processing_flag = false;
            try {
                BufferedReader read = new BufferedReader(new InputStreamReader(new URL(robots_txt_url).openStream()));
                String line = "";
                while ((line = read.readLine()) != null) {
                    if (line.startsWith("User-Agent")) {
                        if (line.contains("*")) {
                            while ((line = read.readLine()) != null) {
                                if (line.startsWith("Disallow")) {
                                    String disallow_url = line.split(":")[1].trim();
                                    blockedUrls.add(link.getProtocol().replace("s", "")+ "://" + disallow_url);
                                }
                                else if (line.startsWith("user-agent")) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            catch (IOException e) {
                System.out.println("robots.txt does not exist for url: " + robots_txt_url.replace("/robots.txt", ""));
            }
        } catch (MalformedURLException e) {
            System.out.println("Wrong URL:" + url);
        }
    }

    public static boolean parse_xml(String url) {
        try {
            String sitemap = normalize_url(url) + "sitemap.xml";
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new URL(sitemap).openStream());
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("loc");
            for (int i = 0; i < nodeList.getLength(); i++) {
                String link = nodeList.item(i).getTextContent();
                if (!linksQueue.contains(link) && !is_url_blocked(link)) {
                    linksQueue.add(link.replace("/sitemap.xml", ""));
                }
            }
        }
        catch (ParserConfigurationException e) {
        }
        catch (MalformedURLException e) {
        }
        catch (IOException e) {
        }
        catch (SAXException e) {
        }
        return false;
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
        parse_robot("https://www.reddit.com/post/");
        parse_xml("https://www.google.com/");
        int x = 5;
    }
}
