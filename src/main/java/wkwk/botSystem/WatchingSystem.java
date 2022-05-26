package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import wkwk.core.BotLogin;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.parameter.record.ServerDataRecord;

import java.awt.*;

public class WatchingSystem extends BotLogin {
    DiscordApi api = getApi();

    public WatchingSystem() {
        User wkwk = api.getYourself();
        DiscordDAO dao = new DiscordDAO();
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
                event.getMessage().getEmbeds().forEach(embed -> messageBuilder.addEmbed(embed.toBuilder()));
                for (MessageAttachment attachment : event.getMessageAttachments()) {
                    messageBuilder.addAttachment(attachment.getUrl());
                }
                messageBuilder.send(api.getServerTextChannelById(966824808622993462L).get()).join();
            }
        });

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            if (api.getServerTextChannelById(979304251358593034L).isPresent()) {
                ServerTextChannel textChannel = api.getServerTextChannelById(979304251358593034L).get();
                EmbedBuilder embed = new EmbedBuilder().setAuthor(interaction.getUser())
                        .setTitle("Command使用" + interaction.getCommandName());
                if (interaction.getServer().isPresent()) {
                    embed.setDescription(interaction.getServer().get().getName() + "\n" + interaction.getServer().get().getIdAsString());
                }
                embed.addField("userID",interaction.getUser().getDiscriminatedName());
                embed.setColor(Color.ORANGE);
                new MessageBuilder().setEmbed(embed).send(textChannel).join();
            }
        });

        api.addServerJoinListener(event -> {
            if (api.getServerTextChannelById(979304251358593034L).isPresent()) {
                ServerTextChannel textChannel = api.getServerTextChannelById(979308591645347870L).get();
                Server server = event.getServer();
                EmbedBuilder embed = new EmbedBuilder().setAuthor(server.getOwner().get())
                        .setTitle("サーバー追加").addField("サーバー情報",server.getName() + "\n" + server.getIdAsString());
                embed.setColor(Color.GREEN);
                new MessageBuilder().setEmbed(embed).send(textChannel).join();
            }
        });

        api.addServerLeaveListener(event -> {
            if (api.getServerTextChannelById(979304251358593034L).isPresent()) {
                ServerTextChannel textChannel = api.getServerTextChannelById(979308591645347870L).get();
                Server server = event.getServer();
                EmbedBuilder embed = new EmbedBuilder().setAuthor(server.getOwner().get())
                        .setTitle("サーバー脱退").addField("サーバー情報",server.getName() + "\n" + server.getIdAsString());
                embed.setColor(Color.RED);
                new MessageBuilder().setEmbed(embed).send(textChannel).join();
            }
        });

        api.addServerVoiceChannelMemberJoinListener(event -> {
            Server server = event.getServer();
            try {
                ServerDataRecord record = dao.getTempData(server.getIdAsString());
                if (event.getOldChannel().isPresent() && record.getFstChannelId().equals(event.getOldChannel().get().getIdAsString()) && api.getServerTextChannelById(979311369235079218L).isPresent()) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setAuthor(event.getUser());
                    embed.setTitle("一時通話新規作成");
                    embed.setDescription(event.getServer().getName() + "\n" + event.getServer().getIdAsString());
                    ServerTextChannel textChannel = api.getServerTextChannelById(979311369235079218L).get();
                    embed.setColor(Color.BLUE);
                    new MessageBuilder().setEmbed(embed).send(textChannel).join();
                }
            } catch (SystemException | DatabaseException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
