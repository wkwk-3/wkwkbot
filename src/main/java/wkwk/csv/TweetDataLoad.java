package wkwk.csv;

import java.util.Arrays;
import java.util.List;

public class TweetDataLoad extends CsvReader {
    public String getTweetTemplate() {
        this.csvLoad();
        return this.lines == null ? null : this.lines.get(0).replaceAll(",", "\n");
    }

}
