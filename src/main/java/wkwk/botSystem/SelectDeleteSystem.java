package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;
import wkwk.dao.DiscordDAO;
import wkwk.parameter.record.BotSendMessageRecord;

public class SelectDeleteSystem {
    DiscordApi api;

    public SelectDeleteSystem(DiscordApi api) {
        this.api = api;
    }

    public void run() {
        DiscordDAO dao = new DiscordDAO();
        api.addReactionAddListener(event -> {
            if (!event.requestUser().join().isBot()) {
                Emoji emoji = event.getEmoji();
                if (emoji.asUnicodeEmoji().isPresent() && event.getServer().isPresent()) {
                    String messageId = event.requestMessage().join().getIdAsString();
                    if (emoji.asUnicodeEmoji().isPresent() && emoji.asUnicodeEmoji().get().equals("❌")) {
                        BotSendMessageRecord messageRecord = dao.getBotSendMessage(messageId);
                        if (!messageRecord.getMESSAGEID().equalsIgnoreCase("NULL") && messageRecord.getMESSAGEID().equalsIgnoreCase(messageId) && messageRecord.getUSERID().equalsIgnoreCase(event.getUser().get().getIdAsString())) {
                            event.getMessage().get().delete();
                            dao.deleteBotSendMessage(messageId);
                            dao.deleteMentionMessage(messageId);
                            dao.deleteMessage("m", messageId);
                        } else if (!messageRecord.getMESSAGEID().equalsIgnoreCase("NULL") && messageRecord.getMESSAGEID().equalsIgnoreCase(messageId) && !messageRecord.getUSERID().equalsIgnoreCase(event.getUser().get().getIdAsString())) {
                            event.removeReaction();
                        }
                    }
                }
            }
        });
    }
}
