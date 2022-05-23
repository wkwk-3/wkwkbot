package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import wkwk.Command.Processing;
import wkwk.core.BotLogin;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;

public class GuideSystem extends BotLogin {
    DiscordApi api = getApi();

    public GuideSystem() {
        Processing processing = new Processing();
        DiscordDAO dao = new DiscordDAO();
        api.addServerJoinListener(e -> {
            Server server = e.getServer();
            String newJoinMessage = "/setup を打つと\nチャンネルとカテゴリを作成されます。\n" +
                    "困ったことがありましたら、下記リンクからサポートサーバーに入り、お聞きください。\nhttp://wkwk.tech/guild";
            try {
                if (server.getSystemChannel().isPresent()) {
                    server.getSystemChannel().get().sendMessage(newJoinMessage);
                } else {
                    server.getOwner().get().sendMessage(newJoinMessage);
                }
                dao.TempNewServer(server.getIdAsString());
            } catch (DatabaseException ignored) {
            }
            processing.upDataBotActivity();
        });
        api.addServerLeaveListener(e -> {
            dao.serverLeaveAllDataDelete(e.getServer().getIdAsString());
            processing.upDataBotActivity();
        });
    }
}