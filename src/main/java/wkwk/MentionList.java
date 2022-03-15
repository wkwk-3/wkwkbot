package wkwk;

import java.util.ArrayList;

public class MentionList {
    private final ArrayList<String> messages = new ArrayList<>();
    private final ArrayList<String> textid = new ArrayList<>();

    public ArrayList<String> getMessages() {
        return messages;
    }

    public void addMessages(String message) {
        this.messages.add(message);
    }

    public boolean checkListSize() {
        return this.messages.size() >= 1;
    }

    public ArrayList<String> getTextid() {
        return textid;
    }

    public void addtextid(String textid) {
        this.textid.add(textid);
    }
}
