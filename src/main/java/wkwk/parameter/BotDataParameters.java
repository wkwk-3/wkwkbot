package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BotDataParameters {

    BOT_TOKEN("BOT_TOKEN"),
    BOT_CLIENT_ID("BOT_CLIENT_ID"),
    CLIENT_SECRET("CLIENT_SECRET"),
    ACCESS_TOKEN("ACCESS_TOKEN"),
    ACCESS_TOKEN_SECRET("ACCESS_TOKEN_SECRET"),
    API_KEY("API_KEY"),
    API_SECRET_KEY("API_SECRET_KEY");

    private final String parameter;

}
