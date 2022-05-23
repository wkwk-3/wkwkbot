package wkwk.core;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;

public class BotLogin {
    static private DiscordApi api;
    public BotLogin() {
        DiscordDAO dao = new DiscordDAO();
        if (api == null) {
            try {
                api = new DiscordApiBuilder().setAllIntents().setToken(dao.BotGetToken()).login().join();
            } catch (DatabaseException | SystemException e) {
                System.exit(0);
            }
        }
    }
    public DiscordApi getApi() {
        return api;
    }
}
