package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TempChannelsParameters {

    VOICE_CHANNEL_ID("VOICE_CHANNEL_ID"),
    TEXT_CHANNEL_ID("TEXT_CHANNEL_ID"),
    SERVER_ID(ServerPropertyParameters.SERVER_ID.getParameter()),
    HIDE_BY("HIDE_BY"),
    LOCK_BY("LOCK_BY");

    private final String parameter;

}
