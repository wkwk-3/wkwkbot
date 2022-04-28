package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;
import wkwk.dao.DiscordDAO;
import wkwk.record.BotSendMessageRecord;

public class SelectDeleteSystem {
    DiscordApi api;

    public SelectDeleteSystem(DiscordApi api) {
        this.api = api;
    }

    public void run() {
        DiscordDAO dao = new DiscordDAO();
        api.addReactionAddListener(e -> {
            if (!e.requestUser().join().isBot()) {
                Emoji emoji = e.getEmoji();
                if (emoji.asUnicodeEmoji().isPresent() && e.getServer().isPresent()) {
                    String messageId = e.requestMessage().join().getIdAsString();
                    if (emoji.asUnicodeEmoji().isPresent() && emoji.asUnicodeEmoji().get().equals("❌")) {
                        BotSendMessageRecord messageRecord = dao.getBotSendMessage(messageId);
                        if (!messageRecord.getMESSAGEID().equalsIgnoreCase("NULL") && messageRecord.getMESSAGEID().equalsIgnoreCase(messageId) && messageRecord.getUSERID().equalsIgnoreCase(e.getUser().get().getIdAsString())) {
                            e.getMessage().get().delete();
                            dao.deleteBotSendMessage(messageId);
                            dao.deleteMentionMessage(messageId);
                            dao.deleteMessage("m", messageId);
                        } else if (!messageRecord.getMESSAGEID().equalsIgnoreCase("NULL") && messageRecord.getMESSAGEID().equalsIgnoreCase(messageId) && !messageRecord.getUSERID().equalsIgnoreCase(e.getUser().get().getIdAsString())) {
                            e.removeReaction();
                        }
                    }
                }
            }
        });
    }
}
