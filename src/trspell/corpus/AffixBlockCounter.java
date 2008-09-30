package trspell.corpus;

import net.zemberek.erisim.Zemberek;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;
import org.jmate.SimpleFileWriter;

import java.io.IOException;
import java.util.List;

public class AffixBlockCounter {

    CountingSet<String> ekListesi = new CountingSet<String>();
    List<String> kelimeler;

    public AffixBlockCounter(String listeDosyasi) {
        System.out.println("liste okunuyor");
        KelimeListesi liste = new KelimeListesi(listeDosyasi);
        kelimeler = liste.getAsList();
    }

    Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());

    public void createSuffixCountFile(String file) throws IOException {
        System.out.println("suffix listesi olusturuluyor..");
        for (String s : kelimeler) {
            List<String[]> sonuclar = zemberek.kelimeAyristir(s);
            for (String[] sonuc : sonuclar) {
                if (sonuc.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < sonuc.length; i++) {
                        sb.append(sonuc[i]);
                    }
                    ekListesi.add(sb.toString());
                }
            }
        }
        SimpleFileWriter sfw = new SimpleFileWriter.Builder(file).encoding("utf-8").keepOpen().build();
        List<CountingSet.Pair<String>> sortedPairs = ekListesi.getSortedPairList();

        int ilkBin = 0;
        for (int i = 0; i < sortedPairs.size() && i < 1000; i++) {
            ilkBin += sortedPairs.get(i).count();
        }
        System.out.println("ilk bin = " + ilkBin);

        for (CountingSet.Pair<String> pair : sortedPairs)
            sfw.writeLine(pair.object() + "|" + pair.count());
        sfw.close();
    }

    public static void main(String[] args) throws IOException {
        new AffixBlockCounter("liste/test-kelimeler.txt").createSuffixCountFile("liste/test-suffix.txt");
    }


}
