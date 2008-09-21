package trspell.oruncek;

import org.jmate.IOs;
import org.jmate.SimpleFileWriter;
import org.jmate.Strings;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HurriyetArsivOkuyucu {
    static String hurriyetUrl = "http://arama.hurriyet.com.tr/arsivnews.aspx?id=";
    static String notFound = "Haber görüntülenememektedir";
    static Pattern p = Pattern.compile("(haberdevambaslik)(.+?)(adnet)", Pattern.DOTALL);

    public static final ExecutorService service = Executors.newFixedThreadPool(50);

    public static void main(String[] args) throws IOException, InterruptedException {
        long st = System.currentTimeMillis();
        int total = 500000;
        final AtomicInteger ait = new AtomicInteger(0);
        final SimpleFileWriter sfw = new SimpleFileWriter.Builder("arsiv/-------.txt").keepOpen().build();
        for (int i = 0; i < total; i++) {

            final int id = i + 9100000;
            Future f = service.submit(new Runnable() {
                public void run() {
                    try {
                        String html = IOs.readAsString(IOs.getReader(new URL(hurriyetUrl + id).openStream()));
                        if (!html.contains(notFound)) {
                            {
                                Matcher m = p.matcher(html);
                                if (m.find()) {
                                    String content = m.group(2).replaceAll("\\<[^ ].+?\\>|&nbsp;", "");
                                    content = Strings.whiteSpacesToSingleSpace(content);
                                    sfw.writeString(content).writeString("\n");
                                    int a = ait.getAndIncrement();
                                    if (a % 20 == 0)
                                        System.out.println(a);
                                } else {
                                    System.out.println("not found");
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            f.isDone();

        }
        service.shutdown();
        while (!service.isTerminated()) {
            Thread.sleep(1000);
        }
        sfw.close();
        System.out.println("time:" + (System.currentTimeMillis() - st));

    }
}
