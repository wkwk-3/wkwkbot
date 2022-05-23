package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.user.User;
import wkwk.core.BotLogin;

public class WatchingSystem extends BotLogin {
    DiscordApi api = getApi();

    public WatchingSystem() {
        User wkwk = api.getYourself();
        api.addMessageCreateListener(event -> {
            if (event.isPrivateMessage() && event.getMessageAuthor().asUser().isPresent() && api.getServerTextChannelById(966824808622993462L).isPresent()) {
                MessageBuilder messageBuilder = new MessageBuilder();
                User privateUser = null;
                if (event.getMessageAuthor().asUser().get().getId() == wkwk.getId() && event.getPrivateChannel().isPresent() && event.getPrivateChannel().get().getRecipient().isPresent()) {
                    privateUser = event.getPrivateChannel().get().getRecipient().get();
                }
                StringBuilder builder = new StringBuilder();
                if (privateUser != null) {
                    builder.append(privateUser.getDiscriminatedName()).append("のDM\n");
                }
                builder.append(event.getMessageAuthor().asUser().get().getDiscriminatedName()).append("\n ID : ").append(event.getMessageAuthor().asUser().get().getIdAsString()).append("\n本文:\n").append(event.getMessageContent());
                messageBuilder.setContent(builder.toString());
                event.getMessage().getEmbeds().forEach(embed -> {
                    messageBuilder.addEmbed(embed.toBuilder());
                });
                for (MessageAttachment attachment : event.getMessageAttachments()) {
                    messageBuilder.addAttachment(attachment.getUrl());
                }
                messageBuilder.send(api.getServerTextChannelById(966824808622993462L).get()).join();
            }
        });
    }
}
