package trspell.corpus;

import net.zemberek.erisim.Zemberek;
import net.zemberek.islemler.cozumleme.CozumlemeSeviyesi;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;
import net.zemberek.yapi.Kelime;
import org.jmate.SimpleFileReader;
import org.jmate.SimpleFileWriter;

import java.io.IOException;
import java.util.Collection;

public class SuffixStatsGenerator {
    CountingSet<String> ekFrekansi = new CountingSet<String>();

    Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());

    public SuffixStatsGenerator(String wordsFile) throws IOException {
        long i = 0;
        for (String str : new SimpleFileReader(wordsFile, "utf-8").getIterableReader()) {
            Kelime[] cozumler = zemberek.kelimeCozumle(str, CozumlemeSeviyesi.TUM_KOKLER);
            if (i++ % 1000 == 0)
                System.out.print(".");
            if (i % 20000 == 0)
                System.out.println(i);
            for (Kelime kelime : cozumler) {
                if (!kelime.gercekEkYok())
                    ekFrekansi.add(concatWithString(kelime.ekler().subList(1, kelime.ekSayisi()), "+"));
            }
        }
    }

    public void writeToFile(String fileName) throws IOException {
        new SimpleFileWriter(fileName, "utf-8").writeLines(ekFrekansi.getSortedList());
    }

    private static String concatWithString(Collection<?> coll, String str) {
        if (coll == null) return null;
        if (coll.isEmpty()) return "";
        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (Object o : coll) {
            if (o != null)
                sb.append(o.toString());
            if (i < coll.size() - 1) {
                sb.append(str);
            }
            i++;
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        new SuffixStatsGenerator("liste/frekans-sirali-liste.txt").writeToFile("liste/ek-listesi.txt");
        // new SuffixStatsGenerator("liste/test-kelimeler.txt").writeToFile("liste/ek-listesi.txt");
    }
}
