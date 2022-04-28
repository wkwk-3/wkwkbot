package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import wkwk.dao.DiscordDAO;
import wkwk.record.DeleteMessageRecord;
import wkwk.record.DeleteTimeRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AutoDeleteRegisterSystem {
    DiscordApi api;

    public AutoDeleteRegisterSystem(DiscordApi api) {
        this.api = api;
    }

    public void run() {
        DiscordDAO dao = new DiscordDAO();
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        api.addMessageCreateListener(e -> {
            if (e.getMessageAuthor().asUser().isPresent() && !e.getMessageAuthor().asUser().get().isBot() && e.getServer().isPresent()) {
                String serverId = e.getServer().get().getIdAsString();
                TextChannel channel = e.getChannel();
                ArrayList<DeleteTimeRecord> deleteList = dao.getDeleteTimes(serverId);
                for (DeleteTimeRecord record : deleteList) {
                    if (record.getTextChannelId().equals(channel.getIdAsString())) {
                        DeleteMessageRecord message = new DeleteMessageRecord();
                        message.setServerId(serverId);
                        message.setChannelId(channel.getIdAsString());
                        message.setMessageId(e.getMessage().getIdAsString());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(Date.from(e.getMessage().getCreationTimestamp()));
                        if ("s".equals(record.getTimeUnit()) || "S".equals(record.getTimeUnit())) {
                            calendar.add(Calendar.SECOND, record.getDeleteTime());
                        } else if ("m".equals(record.getTimeUnit()) || "M".equals(record.getTimeUnit())) {
                            calendar.add(Calendar.MINUTE, record.getDeleteTime());
                        } else if ("h".equals(record.getTimeUnit()) || "H".equals(record.getTimeUnit())) {
                            calendar.add(Calendar.HOUR, record.getDeleteTime());
                        } else if ("d".equals(record.getTimeUnit()) || "D".equals(record.getTimeUnit())) {
                            calendar.add(Calendar.DAY_OF_MONTH, record.getDeleteTime());
                        }
                        message.setDeleteTime(sd.format(calendar.getTime()));
                        dao.addDeleteMessage(message);
                        break;
                    }
                }
            }
        });
    }
}
