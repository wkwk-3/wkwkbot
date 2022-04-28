package wkwk.record;

import lombok.Data;

import java.util.ArrayList;

@Data
public class MentionRecord {

    private final ArrayList<String> messages = new ArrayList<>();
    private final ArrayList<String> textID = new ArrayList<>();

}
