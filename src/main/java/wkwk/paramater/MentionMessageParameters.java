package wkwk.paramater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MentionMessageParameters {

    SERVER_ID(ServerPropertyParameters.SERVER_ID.getParameter()),
    MESSAGE_ID("MESSAGEID"),
    TEXT_CHANNEL_ID("TEXTCHANNELID");

    private final String parameter;

}
