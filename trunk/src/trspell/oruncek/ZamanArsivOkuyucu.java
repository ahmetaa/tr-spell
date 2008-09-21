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

public class ZamanArsivOkuyucu {
    static String hurriyetUrl = "http://www.zaman.com.tr/yazdir.do?haberno=";
    static String notFound = "500 Sayfada Hata";
    static Pattern p = Pattern.compile("(buyukbaslik\"\\>)(.+?)(</table>)", Pattern.DOTALL);

    private void batch(int basla, int end) throws InterruptedException, IOException {
        final ExecutorService service = Executors.newFixedThreadPool(20);
        long st = System.currentTimeMillis();
        final SimpleFileWriter sfw = new SimpleFileWriter.Builder("arsiv/zaman-" + basla + "-" + end + ".txt").keepOpen().build();
        final AtomicInteger ait = new AtomicInteger(0);
        for (int i = basla; i < end; i++) {

            final int id = i;

            Future f = service.submit(new Runnable() {
                public void run() {
                    try {
                        String html = IOs.readAsString(IOs.getReader(new URL(hurriyetUrl + id).openStream()));
                        if (!html.contains(notFound)) {
                            Matcher m = p.matcher(html);
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

            f.isDone();

        }
        service.shutdown();
        while (!service.isTerminated()) {
            Thread.sleep(1000);
        }
        sfw.close();
        System.out.println("time:" + (System.currentTimeMillis() - st));
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        ZamanArsivOkuyucu zao = new ZamanArsivOkuyucu();
        int total = 740000;
        int aralik = 1000;
        for (int i = 0; i < total - aralik; i += aralik) {
            System.out.println("i = " + i);
            zao.batch(i, i + aralik);
        }

    }
}
