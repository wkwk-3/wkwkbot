package wkwk.record;

import lombok.Data;

@Data
public class DeleteTimeRecord {

    private String serverId;
    private String textChannelId;
    private int deleteTime;
    private String timeUnit;

}
