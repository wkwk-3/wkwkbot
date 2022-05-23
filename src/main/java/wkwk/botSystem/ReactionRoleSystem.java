package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.permission.Role;
import wkwk.Command.Processing;
import wkwk.core.BotLogin;
import wkwk.dao.DiscordDAO;
import wkwk.parameter.record.ReactionRoleRecord;

public class ReactionRoleSystem extends BotLogin {
    DiscordApi api = getApi();

    public ReactionRoleSystem() {
        DiscordDAO dao = new DiscordDAO();
        Processing processing = new Processing();
        api.addReactionAddListener(event -> {
            if (!event.requestUser().join().isBot()) {
                Emoji emoji = event.getEmoji();
                if (emoji.asUnicodeEmoji().isPresent() && event.getServer().isPresent()) {
                    String serverId = event.getServer().get().getIdAsString();
                    String textChannel = event.getChannel().getIdAsString();
                    String messageId = event.requestMessage().join().getIdAsString();
                    ReactionRoleRecord record = dao.getReactAllData(serverId);
                    Role targetRole = processing.getReactionRole(emoji, record);
                    if (targetRole != null && record.getTextChannelId() != null && record.getMessageId() != null && record.getTextChannelId().equals(textChannel) && record.getMessageId().equals(messageId)) {
                        event.requestUser().join().addRole(targetRole).join();
                    }
                }
            }
        });
        api.addReactionRemoveListener(event -> {
            if (!event.requestUser().join().isBot()) {
                Emoji emoji = event.getEmoji();
                if (emoji.asUnicodeEmoji().isPresent() && event.getServer().isPresent()) {
                    String serverId = event.getServer().get().getIdAsString();
                    String textChannel = event.getChannel().getIdAsString();
                    String messageId = event.requestMessage().join().getIdAsString();
                    ReactionRoleRecord record = dao.getReactAllData(serverId);
                    Role targetRole = processing.getReactionRole(emoji, record);
                    if (targetRole != null && record.getTextChannelId() != null && record.getMessageId() != null && record.getTextChannelId().equals(textChannel) && record.getMessageId().equals(messageId)) {
                        event.requestUser().join().removeRole(targetRole).join();
                    }
                }
            }
        });
    }
}
