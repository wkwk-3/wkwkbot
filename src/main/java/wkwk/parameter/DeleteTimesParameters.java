package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DeleteTimesParameters {

    SERVER_ID("SERVER_ID"),
    TEXT_CHANNEL_ID("TEXT_CHANNEL_ID"),
    DELETE_TIME("DELETE_TIME"),
    TIME_UNIT("TIME_UNIT");

    private final String parameter;

}
