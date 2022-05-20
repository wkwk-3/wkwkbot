package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DAOParameters {

    CONNECT_STRING("jdbc:mysql://localhost:3306/WKWKDISCORD"),
    USERID("wkwk"),
    PASSWORD("horizonLuna"),
    TABLE_BOT_DATA("BOT_DATA"),
    TABLE_SERVER_PROPERTY("SERVER_PROPERTY"),
    TABLE_TEMP_CHANNEL("TEMP_CHANNELS"),
    TABLE_MENTION_MESSAGE("MENTION_MESSAGES"),
    TABLE_REACT_MESSAGE("REACT_MESSAGE"),
    TABLE_REACT_ROLE("REACT_ROLES"),
    TABLE_NAME_PRESET("NAME_PRESET"),
    TABLE_DELETE_TIMES("DELETE_TIMES"),
    TABLE_DELETE_MESSAGES("DELETE_MESSAGES"),
    TABLE_LOGGING("LOGGING"),
    TABLE_BOT_SEND_MESSAGES("BOT_SEND_MESSAGES");

    private final String parameter;

}
