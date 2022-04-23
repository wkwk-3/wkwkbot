package wkwk;

import org.javacord.api.DiscordApi;
import wkwk.dao.DiscordDAO;

public class MessageDelete {
    public MessageDelete(DiscordApi api, DiscordDAO dao, DeleteMessage message) {
        try {
            dao.deleteMessage("m", message.getMessageId());
            if (api.getTextChannelById(message.getChannelId()).isPresent()) {
                api.getMessageById(message.getMessageId(), api.getTextChannelById(message.getChannelId()).get()).join().delete();
            }
        } catch (Exception ignored) {
        }
    }
}
