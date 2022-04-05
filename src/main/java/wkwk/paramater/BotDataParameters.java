package wkwk.paramater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BotDataParameters {

    BOT_TOKEN("BOTTOKEN"),
    BOT_CLIENT_ID("BOTCLIENTID"),
    CLIENT_SECRET("CLIENTSECRET"),
    ACCESS_TOKEN("ACCESSTOKEN"),
    ACCESS_TOKEN_SECRET("ACCESSTOKENSECRET"),
    API_KEY("APIKEY"),
    API_SECRET_KEY("APISECRETKEY");

    private final String parameter;

}
