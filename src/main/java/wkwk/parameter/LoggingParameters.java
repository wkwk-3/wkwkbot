package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LoggingParameters {

    SERVER_ID("SERVERID"),
    CHANNEL_ID("CHANNELID"),
    LOG_TYPE("LOGTYPE"),
    TARGET_CHANNEL_ID("TARGETCHANNELID");

    private final String parameter;

}
