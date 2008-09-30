package trspell.corpus;

import net.zemberek.erisim.Zemberek;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;
import org.jmate.Collects;
import org.jmate.SimpleFileReader;
import org.jmate.SimpleFileWriter;
import org.jmate.Strings;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BlockAffixProducer {
    int suffixSequence;
    Map<String, String> suffixIdMap = Collects.newHashMap();
    Map<String, String> rootSuffixMap = Collects.newHashMap();
    List<String> kelimeler;
    List<String> suffixes;

    Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());

    public BlockAffixProducer(String kelimeDosyasi, String affixFile, int affixLimit) throws IOException {
        suffixes = new StringFrequencyHelper(affixFile, affixLimit).getPairSet().getSortedList();
        kelimeler = new SimpleFileReader(kelimeDosyasi).asStringList();

        for (String kelime : kelimeler) {
            List<String[]> sonuclar = zemberek.kelimeAyristir(kelime);

            if (sonuclar.size() == 0) {
                addRootSuffixMap(kelime, "");
            }

            for (String[] sonuc : sonuclar) {
                if (sonuc.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < sonuc.length; i++) {
                        sb.append(sonuc[i]);
                    }
                    String ekBlogu = sb.toString();
                    String kokAdayi = kelime.substring(0, kelime.length() - ekBlogu.length());

                    if (zemberek.kelimeDenetle(kokAdayi)) {
                        if (suffixes.contains(ekBlogu)) {
                            addRootSuffixMap(kokAdayi, ekBlogu);
                        }
                    } else {
                        addRootSuffixMap(kelime, "");
                    }

                } else {
                    addRootSuffixMap(kelime, "");
                }
            }

        }
    }

    private void addRootSuffixMap(String root, String suffixBlock) {

        if (!rootSuffixMap.containsKey(root) || !Strings.hasText(suffixBlock)) {
            rootSuffixMap.put(root, suffixBlock);
        } else {
            String suffixConcat = rootSuffixMap.get(root);
            List<String> split = Collects.newArrayList(suffixConcat.split(","));
            if (!split.contains(suffixBlock)) {
                if (Strings.hasText(suffixConcat))
                    rootSuffixMap.put(root, suffixConcat + "," + suffixBlock);
                else
                    rootSuffixMap.put(root, suffixBlock);
            }
        }
    }


    private void generateDictFile(String fileName) throws IOException {
        SimpleFileWriter sfw = new SimpleFileWriter.Builder(fileName).encoding("utf-8").keepOpen().build();
        for (Map.Entry<String, String> entry : rootSuffixMap.entrySet()) {
            sfw.writeLine(entry.getKey() + "|" + entry.getValue());
        }
        sfw.close();
    }

    public static void main(String[] args) throws IOException {
        BlockAffixProducer bap = new BlockAffixProducer(
                "liste/test-kelimeler.txt",
                "liste/test-suffix.txt",
                10000);
        bap.generateDictFile("liste/test-dic.txt");
    }
}
