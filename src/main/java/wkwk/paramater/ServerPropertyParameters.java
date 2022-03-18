package wkwk.paramater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServerPropertyParameters {

    SERVER_ID("SERVERID"),
    MENTION_CHANNEL_ID("MENTIONCHANNELID"),
    FIRST_CHANNEL_ID("FIRSTCHANNELID"),
    VOICE_CATEGORY_ID("VOICECATEGORYID"),
    TEXT_CATEGORY_ID("TEXTCATEGORYID"),
    PREFIX("PREFIX"),
    DEFAULT_PREFIX(">");

    private final String parameter;

}
