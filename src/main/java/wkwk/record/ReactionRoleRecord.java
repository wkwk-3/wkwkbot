package wkwk.record;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ReactionRoleRecord {

    private final ArrayList<String> roleID = new ArrayList<>();
    private final ArrayList<String> emoji = new ArrayList<>();
    private String serverID;
    private String textChannelID;
    private String messageID;

}
