package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DAOParameters {

    CONNECT_STRING("jdbc:mysql://localhost:3306/WKWKDISCORD"),
    USERID("wkwk"),
    PASSWORD("horizonLuna"),
    TABLE_BOT_DATA("BOTDATA"),
    TABLE_SERVER_PROPERTY("SERVERPROPERTY"),
    TABLE_TEMP_CHANNEL("TEMPCHANNELS"),
    TABLE_MENTION_MESSAGE("MENTIONMESSAGES"),
    TABLE_REACT_MESSAGE("REACTMESSAGE"),
    TABLE_REACT_ROLE("REACTROLES"),
    TABLE_NAME_PRESET("NAMEPRESET"),
    TABLE_DELETE_TIMES("DELETETIMES"),
    TABLE_DELETE_MESSAGES("DELETEMESSAGES"),
    TABLE_LOGGING("LOGGING"),
    TABLE_BOT_SEND_MESSAGES("BOTSENDMESSAGES");

    private final String parameter;

}
