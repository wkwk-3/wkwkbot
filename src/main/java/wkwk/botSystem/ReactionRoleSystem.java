package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;
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
        api.addReactionAddListener(event -> {
            if (!event.requestUser().join().isBot()) {
                Emoji emoji = event.getEmoji();
                if (emoji.asUnicodeEmoji().isPresent() && event.getServer().isPresent()) {
                    String serverId = event.getServer().get().getIdAsString();
                    String textChannel = event.getChannel().getIdAsString();
                    String messageId = event.requestMessage().join().getIdAsString();
                    ReactionRoleRecord record = dao.getReactAllData(serverId);
                    if (record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equals(textChannel) && record.getMessageID().equals(messageId)) {
                        ArrayList<String> emojis = record.getEmoji();
                        ArrayList<String> roles = record.getRoleID();
                        for (int i = 0; i < record.getEmoji().size(); i++) {
                            if (emoji.asUnicodeEmoji().get().equals(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent()) {
                                event.requestUser().join().addRole(api.getRoleById(roles.get(i)).get()).join();
                            }
                        }
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
                    if (record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equals(textChannel) && record.getMessageID().equals(messageId)) {
                        ArrayList<String> emojis = record.getEmoji();
                        ArrayList<String> roles = record.getRoleID();
                        for (int i = 0; i < record.getEmoji().size(); i++) {
                            if (emoji.asUnicodeEmoji().get().equals(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent()) {
                                event.requestUser().join().removeRole(api.getRoleById(roles.get(i)).get()).join();
                            }
                        }
                    }
                }
            }
        });
    }
}
