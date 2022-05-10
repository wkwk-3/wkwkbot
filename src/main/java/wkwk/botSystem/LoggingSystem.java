package wkwk.botSystem;


import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import wkwk.dao.DiscordDAO;
import wkwk.parameter.record.LoggingRecord;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LoggingSystem {
    DiscordApi api;

    public LoggingSystem(DiscordApi api) {
        this.api = api;
    }

    public void run() {
        DiscordDAO dao = new DiscordDAO();
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        api.addServerMemberJoinListener(event -> {
            System.out.println("1");
            ArrayList<LoggingRecord> logRecord = dao.getLogging("s", event.getServer().getIdAsString());
            logRecord.forEach(record -> {
                System.out.println("2");
                if (record.getLogType().equals("user") && api.getServerTextChannelById(record.getChannelId()).isPresent()) {
                    System.out.println("3");
                    ServerTextChannel textChannel = api.getServerTextChannelById(record.getChannelId()).get();
                    Date date = Date.from(event.getUser().getCreationTimestamp());
                    textChannel.sendMessage(new EmbedBuilder()
                            .setTitle(event.getUser().getDiscriminatedName() + " が加入")
                            .setAuthor(event.getUser())
                            .addInlineField("ID : " + event.getUser().getIdAsString(), event.getUser().getMentionTag())
                            .addInlineField("アカウント作成日時", sd.format(date))
                            .setColor(Color.BLACK)
                    ).join();
                    System.out.println("4");
                }
                System.out.println("5");
            });
        });
        api.addServerMemberLeaveListener(event -> {
            ArrayList<LoggingRecord> logRecord = dao.getLogging("s", event.getServer().getIdAsString());
            logRecord.forEach(record -> {
                if (record.getLogType().equals("user") && api.getServerTextChannelById(record.getChannelId()).isPresent()) {
                    ServerTextChannel textChannel = api.getServerTextChannelById(record.getChannelId()).get();
                    textChannel.sendMessage(
                            new EmbedBuilder()
                                    .setTitle(event.getUser().getDiscriminatedName() + "が脱退")
                                    .setAuthor(event.getUser())
                                    .addInlineField("@" + event.getUser().getIdAsString(), event.getUser().getMentionTag())
                                    .setColor(Color.BLACK)
                    ).join();
                }
            });
        });


        api.addMessageDeleteListener(event -> {
            dao.deleteMessage("m", event.getMessageId());
            if (event.getMessageAuthor().isPresent() && event.getMessageAuthor().get().asUser().isPresent() && !event.getMessageAuthor().get().asUser().get().isBot() && event.getServer().isPresent()) {
                TextChannel channel = event.getChannel();
                User user = event.getMessageAuthor().get().asUser().get();
                dao.getLogging("c", channel.getIdAsString()).forEach(record -> {
                    if (api.getTextChannelById(record.getChannelId()).isPresent() && record.getLogType().equals("chat") && channel.getIdAsString().equals(record.getTargetChannelId()) && event.getMessageContent().isPresent()) {
                        api.getTextChannelById(record.getChannelId()).get().sendMessage(
                                new EmbedBuilder()
                                        .setAuthor(user)
                                        .addField("削除LOG", event.getMessageContent().get())
                                        .setColor(Color.BLACK)
                        ).join();
                    }
                });
            }
        });
    }
}
