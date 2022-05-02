package wkwk.parameter.record;

import lombok.Data;

import java.util.ArrayList;

@Data
public class MentionMessageRecord {

    private final ArrayList<String> messages = new ArrayList<>();
    private final ArrayList<String> textID = new ArrayList<>();

    private String serverId;
    private String messageId;
    private String TextChannelId;

}
