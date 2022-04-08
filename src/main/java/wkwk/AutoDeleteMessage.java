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
    public void start(DiscordApi api, DiscordDAO dao){
        task = new TimerTask() {
            public void run() {
                if (api != null && dao != null) {
                    Date date = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.set(Calendar.MILLISECOND,0);

                    ArrayList<DeleteMessage> messages = dao.getDeleteMessage(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
                    for (DeleteMessage message : messages) {
                        if (api.getTextChannelById(message.getChannelId()).isPresent()) {
                            api.getMessageById(message.getMessageId(),api.getTextChannelById(message.getChannelId()).get()).join().delete();
                            dao.deleteMessage(message.getMessageId());
                        }
                    }
                }
            }
        };
        this.api = api;
        this.dao = dao;
        timer = new Timer();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND,1);
        calendar.set(Calendar.MILLISECOND,0);
        timer.schedule(task, calendar.getTime() ,1000L);
    }
}
