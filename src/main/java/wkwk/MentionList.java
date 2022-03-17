package wkwk;

import lombok.Data;

import java.util.ArrayList;

@Data
public class MentionList {

    private final ArrayList<String> messages = new ArrayList<>();
    private final ArrayList<String> textID = new ArrayList<>();

}
