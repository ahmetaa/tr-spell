package trspell.corpus;

import net.zemberek.erisim.Zemberek;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;

import java.io.IOException;

public class ZemberekVersusCorpusDenetleyici {

    WordListGenerator corp = new WordListGenerator("corpus/corpus.txt");
    Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());
    int zemberekHit, zemberekMiss, corpusHit, corpusMiss;

    public void kiyaslamaRaporu(String... files) throws IOException {
        for (String file : files) {
/*            //corp.kelimeleriOkuVeEkle(file);
            for (String s : kelimeler) {
                if (zemberek.kelimeDenetle(s)) {
                    zemberekHit++;
                } else {
                    zemberekMiss++;
                }
                if (corp.denetle(s))
                    corpusHit++;
                else corpusMiss++;
            }*/
            System.out.println("-------------------------------------");
            System.out.println("Dosya:" + file);
            System.out.println("Zemberek dogru:" + zemberekHit);
            System.out.println("Zemberek yanlis:" + zemberekMiss);
            System.out.println("Corpus dogru:" + corpusHit);
            System.out.println("Corpus yanlis:" + corpusMiss);
        }
    }

    public void verifyCorpus() {
        int hata=0;
        for (String s : corp.getAsList()) {
            if (!zemberek.kelimeDenetle(s)) {
                System.out.println(s);
                hata++;
            }
        }
        System.out.println("hata = " + hata);
    }


    public static void main(String[] args) throws IOException {
        new ZemberekVersusCorpusDenetleyici().verifyCorpus();
        // new ZemberekVersusCorpusDenetleyici().kiyaslamaRaporu("corpus/kaynaklar/test/tolstoy-dirilis-utf8.txt");
        // new ZemberekVersusCorpusDenetleyici().kiyaslamaRaporu("corpus/kaynaklar/test/efendi-ile-usagi.txt");
        // new ZemberekVersusCorpusDenetleyici().kiyaslamaRaporu("corpus/kaynaklar/test/bu-iste-bir-yalnizlik-var.txt");
    }


}
