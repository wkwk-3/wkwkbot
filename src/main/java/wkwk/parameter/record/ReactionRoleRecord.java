package wkwk.parameter.record;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ReactionRoleRecord {

    private final ArrayList<String> roleId = new ArrayList<>();
    private final ArrayList<String> emoji = new ArrayList<>();
    private String serverId;
    private String textChannelId;
    private String messageId;

}
