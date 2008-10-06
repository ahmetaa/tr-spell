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
        WordListGenerator liste = new WordListGenerator(listeDosyasi);
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
                    String ekBlogu = sb.toString();
                    String kokAdayi = s.substring(0, s.length() - ekBlogu.length());
                    // ozel adlar icin ek kod
                    if (kokAdayi.endsWith("'"))
                        ekBlogu = "'" + ekBlogu;
                    ekListesi.add(ekBlogu);
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
        //new AffixBlockCounter("liste/test-kelimeler.txt").createSuffixCountFile("liste/test-suffix.txt");
        new AffixBlockCounter("liste/frekans-sirali-liste.txt").createSuffixCountFile("liste/suffix.txt");
    }


}
