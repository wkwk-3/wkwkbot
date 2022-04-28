package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;
import wkwk.dao.DiscordDAO;
import wkwk.record.ReactionRoleRecord;

import java.util.ArrayList;

public class ReactionRoleSystem {
    DiscordApi api;

    public ReactionRoleSystem(DiscordApi api) {
        this.api = api;
    }

    public void run() {
        DiscordDAO dao = new DiscordDAO();
        api.addReactionAddListener(e -> {
            if (!e.requestUser().join().isBot()) {
                Emoji emoji = e.getEmoji();
                if (emoji.asUnicodeEmoji().isPresent() && e.getServer().isPresent()) {
                    String serverId = e.getServer().get().getIdAsString();
                    String textChannel = e.getChannel().getIdAsString();
                    String messageId = e.requestMessage().join().getIdAsString();
                    ReactionRoleRecord record = dao.getReactAllData(serverId);
                    if (record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equals(textChannel) && record.getMessageID().equals(messageId)) {
                        ArrayList<String> emojis = record.getEmoji();
                        ArrayList<String> roles = record.getRoleID();
                        for (int i = 0; i < record.getEmoji().size(); i++) {
                            if (emoji.asUnicodeEmoji().get().equals(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent()) {
                                e.requestUser().join().addRole(api.getRoleById(roles.get(i)).get()).join();
                            }
                        }
                    }
                }
            }
        });
        api.addReactionRemoveListener(e -> {
            if (!e.requestUser().join().isBot()) {
                Emoji emoji = e.getEmoji();
                if (emoji.asUnicodeEmoji().isPresent() && e.getServer().isPresent()) {
                    String serverId = e.getServer().get().getIdAsString();
                    String textChannel = e.getChannel().getIdAsString();
                    String messageId = e.requestMessage().join().getIdAsString();
                    ReactionRoleRecord record = dao.getReactAllData(serverId);
                    if (record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equals(textChannel) && record.getMessageID().equals(messageId)) {
                        ArrayList<String> emojis = record.getEmoji();
                        ArrayList<String> roles = record.getRoleID();
                        for (int i = 0; i < record.getEmoji().size(); i++) {
                            if (emoji.asUnicodeEmoji().get().equals(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent()) {
                                e.requestUser().join().removeRole(api.getRoleById(roles.get(i)).get()).join();
                            }
                        }
                    }
                }
            }
        });
    }
}
