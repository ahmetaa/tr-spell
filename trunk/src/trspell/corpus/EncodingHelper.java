package trspell.corpus;

import net.zemberek.yapi.Alfabe;
import org.jmate.Files;
import org.jmate.IOs;
import org.jmate.SimpleFileReader;
import org.jmate.SimpleFileWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class EncodingHelper {

    // dandik bir bicimde tuhaf kodlanmis dosyalari utf=8'e donusturur. dosyalarin orjinal kodlamasi
    // bilinse bu sekil bir hack'a gerek olmazdi
    public static void convertTurkishUtf8(File kaynak, File hedef) throws IOException {
        if (possibleUtf8(kaynak)) {
            System.out.println("possible utf-8, copy directly." + kaynak.getName());
            Files.copy(kaynak, hedef);
        } else {
            SimpleFileWriter sfw = new SimpleFileWriter.Builder(hedef).encoding("utf-8").keepOpen().build();
            for (String s : new SimpleFileReader(kaynak).getIterableReader()) {
                s = s.replaceAll("ý", String.valueOf(Alfabe.CHAR_ii));
                s = s.replaceAll("ð", String.valueOf(Alfabe.CHAR_gg));
                s = s.replaceAll("þ", String.valueOf(Alfabe.CHAR_ss));
                s = s.replaceAll("Ý", String.valueOf(Alfabe.CHAR_II));
                s = s.replaceAll("Þ", String.valueOf(Alfabe.CHAR_SS));
                sfw.writeString(s).writeString("\n");
            }
            sfw.close();
        }
    }

    private static final byte[] bomBytes = new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf};

    static boolean possibleUtf8(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            byte[] bomRead = new byte[bomBytes.length];
            return is.read(bomRead, 0, bomBytes.length) != -1 && Arrays.equals(bomRead, bomBytes);
        } finally {
            IOs.closeSilently(is);
        }
    }


    public static void converDir(String kaynak, String hedef) throws IOException {
        List<File> files = Files.crawlDirectory(new File(kaynak), new Files.ExtensionFilter("txt"));
        for (File file : files) {
            System.out.println("Converting:" + file.getName());
            String fileName = file.getName().replace(".txt", "-utf8.txt");
            convertTurkishUtf8(file, new File(hedef, fileName));
        }
    }

    public static void main(String[] args) throws IOException {
/*        convertTurkishUtf8(new File("corpus/kaynaklar/test/tolstoy_dirilis.txt"),
                new File("corpus/kaynaklar/test/tolstoy-dirilis-utf8.txt")
        );*/
        converDir("C://usr/projects/corpus/kaynaklar/test", "C://usr/projects/corpus/kaynaklar/test-utf8");
    }

}
