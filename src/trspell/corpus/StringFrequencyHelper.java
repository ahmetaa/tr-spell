package trspell.corpus;

import org.jmate.LineIterator;
import org.jmate.SimpleFileReader;
import org.jmate.StringFilters;

import java.io.IOException;

/**
 * reads frequency files with text|count format.
 */
public class StringFrequencyHelper {
    private CountingSet<String> pairSet = new CountingSet<String>();

    public StringFrequencyHelper(String pairFile) throws IOException {
        this(pairFile, Long.MAX_VALUE);
    }

    public StringFrequencyHelper(String pairFile, long count) throws IOException {
        LineIterator li = new SimpleFileReader
                .Builder(pairFile)
                .encoding("utf-8")
                .filters(StringFilters.PASS_ONLY_TEXT)
                .trim()
                .build()
                .getLineIterator();
        int i = 0;
        while (li.hasNext() && i++ < count) {
            String[] strs = li.next().split("[|]");
            if (strs.length == 0)
                continue;
            if (strs.length == 1)
                pairSet.insert(strs[0], 1);
            if (strs.length == 2)
                pairSet.insert(strs[0], Integer.parseInt(strs[1]));
        }
        li.close();
    }

    public StringFrequencyHelper(CountingSet<String> pairSet) {
        this.pairSet = pairSet;
    }

    public CountingSet<String> getPairSet() {
        return pairSet;
    }

    public void setPairSet(CountingSet<String> pairSet) {
        this.pairSet = pairSet;
    }

}
