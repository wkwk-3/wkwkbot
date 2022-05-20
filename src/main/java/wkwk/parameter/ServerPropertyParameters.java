package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServerPropertyParameters {

    SERVER_ID("SERVER_ID"),
    MENTION_CHANNEL_ID("MENTION_CHANNEL_ID"),
    FIRST_CHANNEL_ID("FIRST_CHANNEL_ID"),
    VOICE_CATEGORY_ID("VOICE_CATEGORY_ID"),
    TEXT_CATEGORY_ID("TEXT_CATEGORY_ID"),
    TEMP_BY("TEMP_BY"),
    TEXT_BY("TEXT_BY"),
    DEFAULT_SIZE("DEFAULT_SIZE"),
    DEFAULT_NAME("DEFAULT_NAME"),
    STEREOTYPED("STEREO_TYPED");

    private final String parameter;

}
