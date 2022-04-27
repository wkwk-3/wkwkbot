package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BotSendMessageParameters {
    SERVER_ID("SERVERID"),
    MESSAGE_ID("MESSAGEID"),
    CHANNEL_ID("CHANNELID"),
    USER_ID("USERID");
    private final String parameter;

}
