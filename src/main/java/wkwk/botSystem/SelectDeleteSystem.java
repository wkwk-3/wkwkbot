package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;
import wkwk.core.BotLogin;
import wkwk.dao.DiscordDAO;
import wkwk.parameter.record.BotSendMessageRecord;

public class SelectDeleteSystem extends BotLogin {
    DiscordApi api = getApi();

    public SelectDeleteSystem() {
        DiscordDAO dao = new DiscordDAO();
        api.addReactionAddListener(event -> {
            if (!event.requestUser().join().isBot()) {
                Emoji emoji = event.getEmoji();
                if (emoji.asUnicodeEmoji().isPresent() && event.getServer().isPresent()) {
                    String messageId = event.requestMessage().join().getIdAsString();
                    if (emoji.asUnicodeEmoji().isPresent() && emoji.asUnicodeEmoji().get().equals("❌")) {
                        BotSendMessageRecord messageRecord = dao.getBotSendMessage(messageId);
                        if (!messageRecord.getMessageId().equalsIgnoreCase("NULL") && messageRecord.getMessageId().equalsIgnoreCase(messageId) && messageRecord.getUserId().equalsIgnoreCase(event.getUser().get().getIdAsString())) {
                            event.getMessage().get().delete();
                            dao.deleteBotSendMessage(messageId);
                            dao.deleteMentionMessage(messageId);
                            dao.deleteMessage("m", messageId);
                        } else if (!messageRecord.getMessageId().equalsIgnoreCase("NULL") && messageRecord.getMessageId().equalsIgnoreCase(messageId) && !messageRecord.getUserId().equalsIgnoreCase(event.getUser().get().getIdAsString())) {
                            event.removeReaction();
                        }
                    }
                }
            }
        });
    }
}
