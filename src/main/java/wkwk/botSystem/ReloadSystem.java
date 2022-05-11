package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import wkwk.dao.ReloadDAO;
import wkwk.parameter.record.*;

public class ReloadSystem {
    DiscordApi api;
    public ReloadSystem(DiscordApi api) {
        this.api = api;
    }
    public void run() {
        ReloadDAO dao = new ReloadDAO();
        int botSend = 0;
        int deleteMessage = 0;
        int deleteTime = 0;
        int logging = 0;
        int mentionMessage = 0;
        int namePreset = 0;
        int reactMessage = 0;
        int reactRole = 0;
        int property = 0;
        int temp = 0;

        for (BotSendMessageRecord record : dao.getAllBotSendMessage()) {
            if (!api.getServerById(record.getSERVERID()).isPresent() || !api.getServerTextChannelById(record.getCHANNELID()).isPresent() || !api.getMessageById(record.getMESSAGEID(),api.getTextChannelById(record.getCHANNELID()).get()).isDone()) {
                botSend++;
                dao.deleteBotSendMessage(record);
            }
        }

        for (DeleteMessageRecord record : dao.getAllDeleteMessage()) {
            if (!api.getServerById(record.getServerId()).isPresent() || !api.getServerTextChannelById(record.getChannelId()).isPresent() || !api.getMessageById(record.getMessageId(),api.getTextChannelById(record.getChannelId()).get()).isDone()) {
                deleteMessage++;
                dao.deleteDeleteMessage(record);
            }
        }

        for (DeleteTimeRecord record : dao.getAllDeleteTime()) {
            if (!api.getServerById(record.getServerId()).isPresent() || !api.getServerTextChannelById(record.getTextChannelId()).isPresent()) {
                deleteTime++;
                dao.deleteDeleteTime(record);
            }
        }

        for (LoggingRecord record : dao.getAllLogging()) {
            switch (record.getLogType()) {
                case "chat":
                    if (!api.getServerById(record.getServerId()).isPresent() || !api.getServerTextChannelById(record.getChannelId()).isPresent() || !api.getServerTextChannelById(record.getTargetChannelId()).isPresent()) {
                        logging++;
                        dao.deleteLogging(record);
                    }
                    break;
                case "user":
                    if (!api.getServerById(record.getServerId()).isPresent() || !api.getServerTextChannelById(record.getChannelId()).isPresent()) {
                        logging++;
                        dao.deleteLogging(record);
                    }
                    break;
            }
        }

        for (MentionMessageRecord record : dao.getAllMentionMessage()) {
            if (!api.getServerById(record.getServerId()).isPresent() || !api.getServerTextChannelById(record.getTextChannelId()).isPresent() || !api.getMessageById(record.getMessageId(), api.getTextChannelById(record.getTextChannelId()).get()).isDone()) {
                mentionMessage++;
                dao.deleteMentionMessage(record);
            }
        }

        for (NamePresetRecord record : dao.getAllNamePreset()) {
            if (!api.getServerById(record.getServerId()).isPresent()) {
                namePreset++;
                dao.deleteNamePreset(record);
            }
        }

        for (ReactMessageRecord record : dao.getAllReactMessage()) {
            if (!api.getServerById(record.getServerId()).isPresent() || !api.getServerTextChannelById(record.getTextChannelId()).isPresent() || !api.getMessageById(record.getMessageId(), api.getTextChannelById(record.getTextChannelId()).get()).isDone()) {
                reactMessage++;
                dao.deleteReactMessage(record);
            }
        }

        for (ReactRoleRecord record : dao.getAllReactRole()) {
            if (!api.getServerById(record.getServerId()).isPresent() || !api.getRoleById(record.getRoleId()).isPresent()) {
                reactRole++;
                dao.deleteReactRole(record);
            }
        }

        for (ServerDataRecord record : dao.getAllServerProperty()) {
            if (!api.getServerById(record.getServer()).isPresent()) {
                property++;
                dao.deleteServerProperty(record);
            }
        }

        for (ChannelRecord record : dao.getAllTempChannel()) {
            if (!api.getServerById(record.getServerID()).isPresent() || !api.getServerVoiceChannelById(record.getVoiceID()).isPresent() && !api.getServerTextChannelById(record.getTextID()).isPresent() || !api.getServerVoiceChannelById(record.getVoiceID()).isPresent()) {
                temp++;
                dao.deleteTempChannel(record);
            }
        }

        String out =
                "リロード結果\n" + "ボット送信メッセージ" + botSend + "件削除しました\n" +
                "自動消去メッセージ" + deleteMessage + "件削除しました\n" +
                "自動消去チャンネル" + deleteTime + "件削除しました\n" +
                "ログ出力" + logging + "件削除しました\n" +
                "送信済みメンション" + mentionMessage + "件削除しました\n" +
                "名前変更候補" + namePreset + "件削除しました\n" +
                "リアクションメッセージ" + reactMessage + "件削除しました\n" +
                "リアクションロール" + reactRole + "件削除しました\n" +
                "サーバー設定" + property + "件削除しました\n" +
                "一時チャンネル" + temp + "件削除しました";
        System.out.println(out);
    }
}
