package wkwk.Command;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.parameter.record.DeleteTimeRecord;
import wkwk.parameter.record.LoggingRecord;
import wkwk.parameter.record.ReactionRoleRecord;
import wkwk.parameter.record.ServerDataRecord;

import java.awt.*;

public class Show {
    DiscordDAO dao = new DiscordDAO();

    public EmbedBuilder create(String serverName, String serverId, User sendUser, DiscordApi api) {
        ServerDataRecord tempData = null;
        String bys = null;
        StringBuilder reacts = new StringBuilder("・リアクションロール設定\n");
        try {
            tempData = dao.getTempData(serverId);
            ReactionRoleRecord react = dao.getReactAllData(tempData.getServerId());
            String[] emojis = react.getEmoji().toArray(new String[0]);
            String[] roles = react.getRoleId().toArray(new String[0]);
            bys = "";
            if (tempData.getTempBy()) {
                bys += "・一時作成切り替え : 有効化\n";
            } else {
                bys += "・一時作成切り替え : 無効化\n";
            }
            if (tempData.getTextBy()) {
                bys += "・一時テキストチェンネル作成切り替え : 有効化\n";
            } else {
                bys += "・一時テキストチェンネル作成切り替え : 無効化\n";
            }
            String size = tempData.getDefaultSize();
            if (size.equals("0")) {
                bys += "・一時通話初期人数 : " + size + "(limitless)\n";
            } else {
                bys += "・一時通話初期人数 : " + size + "人\n";
            }
            if (api.getServerTextChannelById(react.getTextChannelId()).isPresent()) {
                reacts.append("　・リアクションロールメッセージ : ").append(api.getMessageById(react.getMessageId(), api.getServerTextChannelById(react.getTextChannelId()).get()).join().getLink()).append("\n");
            }
            for (int i = 0; i < emojis.length; i++) {
                if (api.getRoleById(roles[i]).isPresent()) {
                    reacts.append("　・リアクションロール : ").append("@").append(api.getRoleById(roles[i]).get().getName()).append(" >>>> ").append(emojis[i]).append("\n");
                }
            }
        } catch (SystemException | DatabaseException e) {
            e.printStackTrace();
        }
        StringBuilder namePreset = new StringBuilder("・チャンネル名変更候補設定\n");
        for (String name : dao.GetNamePreset(serverId)) {
            namePreset.append("　・ : ").append(name).append("\n");
        }
        StringBuilder channelLog = new StringBuilder("・メッセージログ設定\n");
        StringBuilder userLog = new StringBuilder("・ユーザーログ設定\n");
        for (LoggingRecord log : dao.getLogging("s", serverId)) {
            if (log.getLogType().equals("chat")) {
                channelLog.append("　・対象チャンネル : <#").append(log.getTargetChannelId()).append("> -> 出力チャンネル : <#").append(log.getChannelId()).append(">\n");
            } else if (log.getLogType().equals("user")) {
                userLog.append("　・出力チャンネル : <#").append(log.getChannelId()).append(">\n");
            }
        }
        StringBuilder autoDelete = new StringBuilder("・自動削除設定\n");
        for (DeleteTimeRecord delete : dao.getDeleteTimes(serverId)) {
            autoDelete.append("　・<#").append(delete.getTextChannelId()).append("> -> ").append(delete.getDeleteTime()).append(delete.getTimeUnit().replaceFirst("s", "秒後").replaceFirst("S", "秒後").replaceFirst("m", "分後").replaceFirst("M", "分後").replaceFirst("h", "時間後").replaceFirst("H", "時間後").replaceFirst("d", "日後").replaceFirst("D", "日後")).append("\n");
        }
        if (reacts.toString().equals("・リアクションロール設定\n")) reacts = new StringBuilder("・リアクションロール設定 : 無し\n");
        if (channelLog.toString().equals("・メッセージログ設定\n")) channelLog = new StringBuilder("・メッセージログ設定 : 無し\n");
        if (userLog.toString().equals("・ユーザーログ設定\n")) userLog = new StringBuilder("・ユーザーログ設定 : 無し\n");
        if (autoDelete.toString().equals("・自動削除設定\n")) autoDelete = new StringBuilder("・自動削除設定 : 無し\n");
        if (namePreset.toString().equals("・チャンネル名変更候補設定\n")) namePreset = new StringBuilder("・チャンネル名変更候補設定 : 無し\n");
        if (tempData != null) {
            return new EmbedBuilder()
                    .setTitle("一覧情報表示 With " + serverName)
                    .setAuthor(sendUser)
                    .addField("サーバー情報一覧", "・メンション送信チャンネルID : <#" + tempData.getMentionChannelId() + ">\n" +
                            "・一時作成チャネル : <#" + tempData.getFstChannelId() + ">\n" +
                            "・通話カテゴリ : <#" + tempData.getVoiceCategoryId() + ">\n" +
                            "・テキストカテゴリ : <#" + tempData.getTextCategoryId() + ">\n" +
                            "・カスタム募集 : " + tempData.getStereoTyped() + "\n" +
                            "・通話初期ネーム : " + tempData.getDefaultName() + "\n" +
                            bys + reacts + channelLog + userLog + namePreset + autoDelete)
                    .setColor(Color.cyan)
                    .setThumbnail("https://i.imgur.com/KHpjoiu.png");
        } else {
            return new EmbedBuilder()
                    .setTitle("一覧情報表示 With " + serverName)
                    .setAuthor(sendUser)
                    .addField("サーバー情報一覧", "サーバー情報が無い")
                    .setColor(Color.cyan)
                    .setThumbnail("https://i.imgur.com/KHpjoiu.png");
        }
    }
}
