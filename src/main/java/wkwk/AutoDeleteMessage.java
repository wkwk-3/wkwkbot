package wkwk;

import org.javacord.api.DiscordApi;
import wkwk.dao.DiscordDAO;

import java.text.SimpleDateFormat;
import java.util.*;

public class AutoDeleteMessage {
    TimerTask task;
    Timer timer;
    DiscordApi api = null;
    DiscordDAO dao = null;

    public void start(DiscordApi api, DiscordDAO dao) {
        this.api = api;
        this.dao = dao;
        task = new TimerTask() {
            public void run() {
                Date date = new Date();
                String dates = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                ArrayList<DeleteMessage> messages = dao.getDeleteMessage(dates);
                for (DeleteMessage message : messages) {
                    try {
                        if (api.getTextChannelById(message.getChannelId()).isPresent()) {
                            api.getMessageById(message.getMessageId(), api.getTextChannelById(message.getChannelId()).get()).join().delete();
                        }
                        dao.deleteMessage("m", message.getMessageId());
                    } catch (Exception ignored) {
                    }
                }
            }
        };
        timer = new Timer();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 0);
        timer.schedule(task, calendar.getTime(), 1000L);
    }
}
