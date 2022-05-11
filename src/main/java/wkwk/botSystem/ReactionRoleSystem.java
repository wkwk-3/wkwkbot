package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.permission.Role;
import wkwk.Command.Processing;
import wkwk.dao.DiscordDAO;
import wkwk.parameter.record.ReactionRoleRecord;

import java.util.ArrayList;

public class ReactionRoleSystem {
    DiscordApi api;

    public ReactionRoleSystem(DiscordApi api) {
        this.api = api;
    }

    public void run() {
        DiscordDAO dao = new DiscordDAO();
        Processing processing = new Processing(api);
        api.addReactionAddListener(event -> {
            if (!event.requestUser().join().isBot()) {
                Emoji emoji = event.getEmoji();
                if (emoji.asUnicodeEmoji().isPresent() && event.getServer().isPresent()) {
                    String serverId = event.getServer().get().getIdAsString();
                    String textChannel = event.getChannel().getIdAsString();
                    String messageId = event.requestMessage().join().getIdAsString();
                    ReactionRoleRecord record = dao.getReactAllData(serverId);
                    Role targetRole = processing.getReactionRole(emoji, record);
                    if (targetRole != null && record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equals(textChannel) && record.getMessageID().equals(messageId)) {
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
                    if (targetRole != null && record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equals(textChannel) && record.getMessageID().equals(messageId)) {
                        event.requestUser().join().removeRole(targetRole).join();
                    }
                }
            }
        });
    }
}
