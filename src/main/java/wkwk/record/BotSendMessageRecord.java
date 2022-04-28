package wkwk.record;

import lombok.Data;

@Data
public class BotSendMessageRecord {
    private String SERVERID;
    private String MESSAGEID;
    private String CHANNELID;
    private String USERID;
}
