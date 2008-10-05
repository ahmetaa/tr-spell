package trspell.oruncek;

import org.jmate.SimpleFileWriter;
import org.jmate.IOs;
import org.jmate.Strings;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;
import java.net.URL;

public class IdBasedSimpleSpider {

    private String baseUrl;
    private String wrongPageString;
    private Pattern contentPattern;
    private String fileRoot;
    private int threadCountPerBatch;


    public IdBasedSimpleSpider(String baseUrl, String wrongPageString, Pattern contentPattern, String fileRoot, int threadCountPerBatch) {
        this.baseUrl = baseUrl;
        this.wrongPageString = wrongPageString;
        this.contentPattern = contentPattern;
        this.fileRoot = fileRoot;
        this.threadCountPerBatch = threadCountPerBatch;
    }

    public void batch(int basla, int end) throws InterruptedException, IOException {
        final ExecutorService service = Executors.newFixedThreadPool(threadCountPerBatch);
        long st = System.currentTimeMillis();
        final SimpleFileWriter sfw = new SimpleFileWriter.Builder(fileRoot + basla + "-" + end + ".txt").keepOpen().build();
        final AtomicInteger ait = new AtomicInteger(0);
        for (int i = basla; i < end; i++) {

            final int id = i;
            service.submit(new Runnable() {
                public void run() {
                    try {
                        String html = IOs.readAsString(IOs.getReader(new URL(baseUrl + id).openStream()));
                        if (!html.contains(wrongPageString)) {
                            Matcher m = contentPattern.matcher(html);
                            if (m.find()) {
                                String content = m.group(2).replaceAll("\\<[^ ].+?\\>|&nbsp;|\\<p\\>", "");
                                content = Strings.whiteSpacesToSingleSpace(content);
                                sfw.writeString(content).writeString("\n");
                                int a = ait.getAndIncrement();
                                if (a % 20 == 0)
                                    System.out.println(a);
                            } else {
                                System.out.println("not found");
                            }
                        }
                    } catch (IOException e) {
                        //ignore. 505 messages.
                    }
                }
            });

        }
        service.shutdown();
        while (!service.isTerminated()) {
            Thread.sleep(1000);
        }
        sfw.close();
        System.out.println("time:" + (System.currentTimeMillis() - st));
    }

    public void doIt(int start, int total, int interval) throws IOException, InterruptedException {

        for (int i = start; i < total - interval; i += interval) {
            System.out.println("i = " + i);
            batch(i, i + interval);
        }

    }


    public static void main(String[] args) throws IOException, InterruptedException {

        IdBasedSimpleSpider ids = new IdBasedSimpleSpider(
                "http://www.radikal.com.tr/Default.aspx?aType=HaberYazdir&ArticleID=",
                "500 Sayfada Hata",
                Pattern.compile("(haberDetayYazi\"\\>)(.+?)(window\\.print)", Pattern.DOTALL),
                "C:\\usr\\projects\\corpus\\kaynaklar\\radikal\\radikal-",
                20);
        ids.doIt(600000, 900000, 1000);

/*        IdBasedSimpleSpider ids = new IdBasedSimpleSpider(
  "http://www.zaman.com.tr/yazdir.do?haberno=",
  "500 Sayfada Hata",
  Pattern.compile("(buyukbaslik\"\\>)(.+?)(</table>)", Pattern.DOTALL),
  "arsiv/zaman",
  20);
ids.doIt(0, 740000, 1000);    */


    }
}
