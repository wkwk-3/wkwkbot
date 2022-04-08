package wkwk.paramater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DeleteTimesParameters {

    SERVER_ID("SERVERID"),
    TEXT_CHANNEL_ID("TEXTCHANNELID"),
    DELETE_TIME("DELETETIME"),
    TIME_UNIT("TIMEUNIT");

    private final String parameter;

}
