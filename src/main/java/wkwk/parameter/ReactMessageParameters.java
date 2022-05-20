package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReactMessageParameters {

    SERVER_ID(ServerPropertyParameters.SERVER_ID.getParameter()),
    TEXT_CHANNEL_ID("TEXT_CHANNEL_ID"),
    MESSAGE_ID("MESSAGE_ID");

    private final String parameter;

}
