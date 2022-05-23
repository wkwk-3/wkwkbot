package wkwk.parameter.record;

import lombok.Data;

@Data
public class BotSendMessageRecord {
    private String servetId;
    private String messageId;
    private String channelId;
    private String userId;
}
