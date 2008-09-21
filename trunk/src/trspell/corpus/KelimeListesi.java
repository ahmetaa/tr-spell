package trspell.corpus;

import net.zemberek.erisim.Zemberek;
import net.zemberek.islemler.cozumleme.CozumlemeSeviyesi;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;
import net.zemberek.yapi.Kelime;
import org.jmate.*;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.lang.System.*;

public class KelimeListesi implements KelimeDenetleyici {

    private static Pattern p = Pattern.compile("[^ \\t\\n,.]+");

    CountingSet<String> kelimeFrekansKumesi = new CountingSet<String>();
    private String listFile;
    private int toplamKelime = 0;

    public KelimeListesi(String listFile) {
        this.listFile = listFile;
        if (new File(listFile).exists()) {
            try {
                oku(listFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        out.println("kelime sayisi:" + kelimeFrekansKumesi.size());
    }

    private void oku(String file) throws IOException {
        IterableLineReader ilr = new SimpleFileReader
                .Builder(file)
                .encoding("utf-8")
                .filters(StringFilters.PASS_ONLY_TEXT)
                .trim()
                .build()
                .getIterableReader();
        for (String line : ilr) {
            String[] strs = line.split("[|]");
            if (strs.length == 0)
                continue;
            if (strs.length == 1)
                kelimeFrekansKumesi.insert(strs[0], 1);
            if (strs.length == 2)
                kelimeFrekansKumesi.insert(strs[0], Integer.parseInt(strs[1]));
        }
        ilr.close();
    }

    public List<String> getAsList() {
        return Collects.newArrayList(kelimeFrekansKumesi.getSet());
    }

    private static Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());

    public void kelimeleriOkuVeEkle(String fileName) throws IOException {
        LineIterator li = new SimpleFileReader
                .Builder(fileName)
                .encoding("utf-8")
                .filters(StringFilters.PASS_ONLY_TEXT)
                .trim()
                .build()
                .getLineIterator();

        int kabulEdilen = 0, edilmeyen = 0, toplam = 0, baslangic = kelimeFrekansKumesi.size();
        while (li.hasNext()) {
            String line = Strings.whiteSpacesToSingleSpace(li.next().replaceAll("[1234567890(),.?—/\\-!:;#\"…|_\\[{}<>]", " "));
            line = line.replaceAll("’", "'");
            if (zemberek.dilTesti(line) < 2) {
                continue;
            }
            List<String> kelimeler = Collects.newArrayList();
            Matcher m = p.matcher(line);
            while (m.find()) {
                kelimeler.add(m.group());
            }

            for (String s : kelimeler) {
                s = s.replaceAll("(^['\"]+)|(['\"]+$)", "");
                toplamKelime++;
                if (toplam++ % 1000 == 0) out.print(".");
                if (toplam % 20000 == 0) out.println(toplam);
                if (!zemberek.kelimeDenetle(s)) {
                    edilmeyen++;
                    continue;
                }
                Kelime[] k = zemberek.kelimeCozumle(s, CozumlemeSeviyesi.TUM_KOKLER);
                kabulEdilen++;

                for (Kelime kelime : k) {
                    String kel = zemberek.kelimeUret(kelime.kok(), kelime.ekler());
                    // kelime uretici sonucunun dogrulugunu denetle.
                    if (zemberek.kelimeDenetle(kel))
                        kelimeFrekansKumesi.add(kel);
                }
            }
        }
        out.println("\n");
        out.println("dosya islenen kelime   :" + toplam);
        out.println("kabul edilen           :" + kabulEdilen);
        out.println("kabul edilmeyen        :" + edilmeyen);
        out.println("yeni tekil kelime      :" + (kelimeFrekansKumesi.size() - baslangic));
        out.println("toplam islenen kelime  :" + toplamKelime);
        out.println("toplam tekil kelime    :" + kelimeFrekansKumesi.size());
        out.println("\n");
    }

    public void createList(String... files) throws IOException {
        kelimeFrekansKumesi = new CountingSet<String>();
        updateList(files);
    }

    public void updateList(String... files) throws IOException {
        for (String file : files) {
            out.println("isleniyor:" + file);
            kelimeleriOkuVeEkle(file);
        }
        kaydet();
    }

    public void mergeList(String listFile) throws IOException {
        KelimeListesi bc = new KelimeListesi(listFile);
        for (String s : bc.kelimeFrekansKumesi) {
            kelimeFrekansKumesi.merge(s, bc.kelimeFrekansKumesi.getCount(s));
        }
        kaydet();
    }

    public void generateLetterOrderedFile(String filename) throws IOException {
        List<String> list = kelimeFrekansKumesi.getSortedList(new TurkceSiralamaKiyaslayici());
        new SimpleFileWriter(filename, "utf-8").writeToStringLines(list);
    }

    public void generateFrequencyOrderedWordFile(String filename) throws IOException {
        new SimpleFileWriter(filename, "utf-8").writeToStringLines(kelimeFrekansKumesi.getSortedList());
    }

    public void generateDicFile(String filename) throws IOException {
        List<String> list = Collects.newArrayList(String.valueOf(kelimeFrekansKumesi.size()));
        list.addAll(kelimeFrekansKumesi.getSet());
        Collections.sort(list, new TurkceSiralamaKiyaslayici());
        new SimpleFileWriter.Builder(filename).encoding("utf-8").build().writeToStringLines(list);
    }

    private void kaydet() throws IOException {
        List<CountingSet.Pair<String>> kelimeFreq = kelimeFrekansKumesi.getSortedPairList();

        out.println("--------------------------------");
        out.println("\ntoplam kelime:" + toplamKelime);
        out.println("\ntoplam tekil dogru kelime:" + kelimeFrekansKumesi.size());

        SimpleFileWriter sfw = new SimpleFileWriter.Builder(listFile).encoding("utf-8").keepOpen().build();
        for (CountingSet.Pair<String> pair : kelimeFreq)
            sfw.writeLine(pair.object() + "|" + pair.count());
        sfw.close();
    }

    @Override
    public boolean denetle(String s) {
        return kelimeFrekansKumesi.contains(s) || kelimeFrekansKumesi.contains(Words.uncapitalize(s));
    }

    private class TurkceSiralamaKiyaslayici implements Comparator<String> {
        public int compare(String o1, String o2) {
            Collator turkishCollator = Collator.getInstance(new Locale("tr"));
            return turkishCollator.compare(o1, o2);
        }
    }

    public void createListFromDir(String dir) throws IOException {
        List<File> kaynaklar = Files.crawlDirectory(new File(dir));
        String[] filez = new String[kaynaklar.size()];
        for (int i = 0; i < filez.length; i++) {
            filez[i] = kaynaklar.get(i).getAbsolutePath();
        }
        createList(filez);

    }

    public static void main(String[] args) throws IOException {
        new KelimeListesi("liste/kelime-frekans.txt").createListFromDir("C:/usr/projects/corpus/kaynaklar/utf-8");
        //new KelimeListesi("liste/kelime-frekans.txt").generateFrequencyOrderedWordFile("liste/frekans-sirali-liste.txt");
    }
}