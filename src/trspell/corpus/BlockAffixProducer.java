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
import java.util.Set;

public class BlockAffixProducer {
    int suffixSequence = 100;
    Map<String, String> suffixIdMap = Collects.newHashMap();
    Map<String, String> rootSuffixMap = Collects.newHashMap();
    Set<String> suffixes;

    Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());

    public BlockAffixProducer(String kelimeDosyasi, String affixFile, int affixLimit)
            throws IOException {
        System.out.println("Loading suffixes.");
        suffixes = Collects.newHashSet(new StringFrequencyHelper(affixFile, affixLimit).getPairSet().getSortedList());
        for (String suffix : suffixes) {
            suffixIdMap.put(suffix, String.valueOf(suffixSequence++));
        }

        System.out.println("Processing words.");
        int j = 0;
        for (String kelime : new SimpleFileReader(kelimeDosyasi, "utf-8").getIterableReader()) {
            List<String[]> sonuclar = zemberek.kelimeAyristir(kelime);

            if (sonuclar.size() == 0) {
                addRootSuffixMap(kelime, "");
            }

            if (j++ % 1000 == 0)
                System.out.print(".");
            if (j % 50000 == 0)
                System.out.println(j);
            for (String[] sonuc : sonuclar) {
                if (sonuc.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < sonuc.length; i++) {
                        sb.append(sonuc[i]);
                    }
                    String ekBlogu = sb.toString();
                    String kokAdayi = kelime.substring(0, kelime.length() - ekBlogu.length());
                    if (kokAdayi.endsWith("'"))
                        ekBlogu = "'" + ekBlogu;

                    if (!suffixes.contains(ekBlogu)) {
                        addRootSuffixMap(kelime, "");
                        continue;
                    }

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
        String suffixConcat = rootSuffixMap.get(root);
        if (suffixConcat == null) {
            rootSuffixMap.put(root, suffixBlock);
            return;
        }
        if (!Strings.hasText(suffixBlock))
            return;
        List<String> split = Collects.newArrayList(suffixConcat.split(","));
        if (!split.contains(suffixBlock)) {
            if (Strings.allHasText(suffixConcat))
                rootSuffixMap.put(root, suffixConcat + "," + suffixBlock);
            else
                rootSuffixMap.put(root, suffixBlock);
        }
    }


    private void generateHunspellAffixFile(String fileName) throws IOException {
        System.out.println("Generating affix file...");
        List<String> templateLines = new SimpleFileReader("kaynaklar/hunspell/affix-template.txt", "utf-8").asStringList();
        SimpleFileWriter sfw = new SimpleFileWriter.Builder(fileName).encoding("utf-8").keepOpen().build();
        sfw.writeLines(templateLines);
        sfw.writeLine("\n");
        for (Map.Entry<String, String> entry : suffixIdMap.entrySet()) {
            sfw.writeLine("SFX " + entry.getValue() + " N 1");
            sfw.writeLine("SFX " + entry.getValue() + " 0 " + entry.getKey() + " .");
        }
        sfw.close();
    }

    private String generateSuffixIdStringFromSuffixNames(String concatString) {
        if (!Strings.hasText(concatString))
            return "";
        String[] suffixNames = concatString.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < suffixNames.length; i++) {
            sb.append(suffixIdMap.get(suffixNames[i]));
            if (i < suffixNames.length - 1)
                sb.append(",");
        }
        return sb.toString();
    }

    private void generateDictSuffixNameFile(String fileName) throws IOException {
        SimpleFileWriter sfw = new SimpleFileWriter.Builder(fileName).encoding("utf-8").keepOpen().build();
        for (Map.Entry<String, String> entry : rootSuffixMap.entrySet()) {
            sfw.writeLine(entry.getKey() + "|" + entry.getValue());
        }
        sfw.close();
    }

    private void generateHunspellDictFile(String fileName) throws IOException {
        System.out.println("Generating dictionary file...");
        SimpleFileWriter sfw = new SimpleFileWriter.Builder(fileName).encoding("utf-8").keepOpen().build();
        sfw.writeLine(String.valueOf(rootSuffixMap.size()));
        for (Map.Entry<String, String> entry : rootSuffixMap.entrySet()) {
            String suffixIds = generateSuffixIdStringFromSuffixNames(entry.getValue());
            if (Strings.hasText(suffixIds))
                sfw.writeLine(entry.getKey() + "/" + suffixIds);
            else
                sfw.writeLine(entry.getKey());
        }
        sfw.close();
    }

    public static void main(String[] args) throws IOException {
        BlockAffixProducer bap = new BlockAffixProducer(
                "liste/frekans-sirali-liste.txt",
                "liste/suffix.txt",
                10000);
        //bap.generateDictSuffixNameFile("liste/test-dic.txt");
        bap.generateHunspellDictFile("kaynaklar/hunspell-win32/test-tr.dic");
        bap.generateHunspellAffixFile("kaynaklar/hunspell-win32/test-tr.aff");
    }
}
