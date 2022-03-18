package wkwk.paramater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReactMessageParameters {

    SERVER_ID(ServerPropertyParameters.SERVER_ID.getParameter()),
    TEXT_CHANNEL_ID("TEXTCHANNELID"),
    MESSAGE_ID("MESSAGEID");

    private final String parameter;

}
