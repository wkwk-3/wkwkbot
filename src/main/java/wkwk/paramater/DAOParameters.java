package wkwk.paramater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DAOParameters {

    CONNECT_STRING("jdbc:mysql://localhost:3306/WKWKDISCORD"),
    USERID("wkwk"),
    PASSWORD("Horizon"),
    TABLE_BOT_DATA("BOTDATA"),
    TABLE_SERVER_PROPERTY("SERVERPROPERTY"),
    TABLE_TEMP_CHANNEL("TEMPCHANNELS"),
    TABLE_MENTION_MESSAGE("MENTIONMESSAGES"),
    TABLE_REACT_MESSAGE("REACTMESSAGE"),
    TABLE_REACT_ROLE("REACTROLES");

    private final String parameter;

}
