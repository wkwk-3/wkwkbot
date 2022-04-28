package wkwk.record;

import lombok.Data;

@Data
public class DeleteMessageRecord {

    private String serverId;
    private String messageId;
    private String channelId;
    private String deleteTime;

}
