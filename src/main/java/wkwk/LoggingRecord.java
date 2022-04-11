package wkwk;

import lombok.Data;


@Data
public class LoggingRecord {
    private String serverId;
    private String channelId;
    private String logType;
    private String targetChannelId;
}
