package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DeleteMessagesParameters {

    SERVER(DeleteTimesParameters.SERVER_ID.getParameter()),
    MESSAGE_ID("MESSAGEID"),
    TEXT_CHANNEL_ID("TEXTCHANNELID"),
    DELETE_TIME("DELETETIME");

    private final String parameter;

}
