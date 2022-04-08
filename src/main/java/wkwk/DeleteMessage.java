package wkwk;

import lombok.Data;

@Data
public class DeleteMessage {

    private String serverId;
    private String messageId;
    private String channelId;
    private String deleteTime;

}
