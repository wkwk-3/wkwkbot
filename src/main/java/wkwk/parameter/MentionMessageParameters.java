package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MentionMessageParameters {

    SERVER_ID(ServerPropertyParameters.SERVER_ID.getParameter()),
    MESSAGE_ID("MESSAGE_ID"),
    TEXT_CHANNEL_ID("TEXT_CHANNEL_ID");

    private final String parameter;

}
