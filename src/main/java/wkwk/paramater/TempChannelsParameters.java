package wkwk.paramater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TempChannelsParameters {

    VOICE_CHANNEL_ID("VOICECHANNELID"),
    TEXT_CHANNEL_ID("TEXTCHANNELID"),
    SERVER_ID(ServerPropertyParameters.SERVER_ID.getParameter()),
    HIDE_BY("HIDEBY"),
    LOCK_BY("LOCKBY");

    private final String parameter;

}
