package wkwk.paramater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BotDataParameters {

    BOT_TOKEN("BOTTOKEN"),
    BOT_CLIENT_ID("BOTCLIENTID"),
    CLIENT_SECRET("CLIENTSECRET");

    private final String parameter;

}
