package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;

public class GuideSystem {
    DiscordApi api;

    public GuideSystem(DiscordApi api) {
        this.api = api;
    }

    public void run() {
        DiscordDAO dao = new DiscordDAO();
        api.addMessageCreateListener(e -> {
            if (e.getMessageAuthor().asUser().isPresent() && !e.getMessageAuthor().asUser().get().isBot() && e.getServer().isPresent()) {
                String serverId = e.getServer().get().getIdAsString();
                if (e.getServer().get().getOwner().isPresent() && e.getMessageContent().startsWith(">") && e.getMessageAuthor().asUser().isPresent() && e.getServer().get().getPermissions(e.getMessageAuthor().asUser().get()).getAllowedPermission().contains(PermissionType.ADMINISTRATOR) || e.getServer().get().getOwner().get().getId() == e.getMessageAuthor().asUser().get().getId()) {
                    if (dao.getNoSlashCommandServer(serverId)) {
                        StringBuilder response;
                        String[] cmd = e.getMessageContent().replaceFirst(">", "").split(" ");
                        if ("help".equals(cmd[0]) || "show".equals(cmd[0]) || "ping".equals(cmd[0]) || "setup".equals(cmd[0]) || "set".equals(cmd[0]) || "remove".equals(cmd[0]) || "start".equals(cmd[0]) || "stop".equals(cmd[0]) || "mess".equals(cmd[0]) || "name".equals(cmd[0]) || "size".equals(cmd[0]) || "men".equals(cmd[0]) || "n".equals(cmd[0]) || "s".equals(cmd[0]) || "m".equals(cmd[0])) {
                            e.getMessage().delete();
                            response = new StringBuilder().append("wkwkBOTはスラッシュコマンドのみの対応になりました。\n以下のリンクから").append(e.getServer().get().getName()).append("を選ばれますと、設定はそのままにすぐにスラッシュコマンドお使い頂けます。\n(https://wkb.page.link/bot)\n\nお手数ですが").append(e.getServer().get().getName()).append("でwkwkBOTに対して `/ping`をお使いください。\nすると以降このメッセージは表示されません");
                            new MessageBuilder().setContent(response.toString()).send(e.getMessageAuthor().asUser().get()).join();
                            if (api.getTextChannelById(966824808622993461L).isPresent()) {
                                new MessageBuilder().setContent(e.getServer().get().getName() + " : " + e.getServer().get().getIdAsString()).send(api.getTextChannelById(966824808622993461L).get()).join();
                            }
                        }
                    }
                }
            }
        });
        api.addServerJoinListener(e -> {
            Server server = e.getServer();
            try {
                if (server.getSystemChannel().isPresent()) {
                    server.getSystemChannel().get().sendMessage("/setup を打つと\nチャンネルとカテゴリを作成されます");
                    server.getSystemChannel().get().sendMessage("困ったことがありましたら、下記リンクからサポートサーバーに入り、お聞きください。\nhttps://discord.gg/6Z7jabh983");
                } else {
                    server.getOwner().get().sendMessage("/setup を打つと\nチャンネルとカテゴリを作成されます");
                    server.getOwner().get().sendMessage("困ったことがありましたら、下記リンクからサポートサーバーに入り、お聞きください。\nhttps://discord.gg/6Z7jabh983").join();
                }
                dao.TempNewServer(server.getIdAsString());
            } catch (DatabaseException ignored) {
            }
            api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");
        });
        api.addServerLeaveListener(e -> {
            dao.serverLeaveAllDataDelete(e.getServer().getIdAsString());
            api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");
        });
    }
}