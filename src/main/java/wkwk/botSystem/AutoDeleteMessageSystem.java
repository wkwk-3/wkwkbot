package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import wkwk.core.BotLogin;
import wkwk.dao.DiscordDAO;
import wkwk.parameter.record.DeleteMessageRecord;

import java.text.SimpleDateFormat;
import java.util.*;

public class AutoDeleteMessageSystem extends BotLogin {
    TimerTask task;
    Timer timer;

    DiscordApi api = getApi();
    DiscordDAO dao = new DiscordDAO();

    public AutoDeleteMessageSystem() {
        task = new TimerTask() {
            public void run() {
                Date date = new Date();
                String dates = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                ArrayList<DeleteMessageRecord> messages = dao.getDeleteMessage(dates);
                for (DeleteMessageRecord message : messages) {
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
