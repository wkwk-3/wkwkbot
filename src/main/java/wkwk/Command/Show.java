package wkwk.Command;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.parameter.record.ReactionRoleRecord;
import wkwk.parameter.record.ServerDataRecord;

import java.awt.*;

public class Show {
    public EmbedBuilder create(String serverName, String serverId, User sendUser, DiscordDAO dao, DiscordApi api) {
        ServerDataRecord tempData = null;
        String bys = null;
        StringBuilder reacts = null;

        try {
            tempData = dao.TempGetData(serverId);
            ReactionRoleRecord react = dao.getReactAllData(tempData.getServer());
            String[] emojis = react.getEmoji().toArray(new String[0]);
            String[] roles = react.getRoleID().toArray(new String[0]);
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
            reacts = new StringBuilder();
            if (api.getServerTextChannelById(react.getTextChannelID()).isPresent()) {
                reacts.append("・リアクションロールメッセージ : ").append(api.getMessageById(react.getMessageID(), api.getServerTextChannelById(react.getTextChannelID()).get()).join().getLink()).append("\n");
            }
            for (int i = 0; i < emojis.length; i++) {
                if (api.getRoleById(roles[i]).isPresent()) {
                    reacts.append("・リアクションロール : ").append("@").append(api.getRoleById(roles[i]).get().getName()).append(" >>>> ").append(emojis[i]).append("\n");
                }
            }
        } catch (SystemException | DatabaseException e) {
            e.printStackTrace();
        }
        if (tempData != null) {
            return new EmbedBuilder()
                    .setTitle("一覧情報表示 With " + serverName)
                    .setAuthor(sendUser)
                    .addField("サーバー情報一覧", "・メンション送信チャンネルID : <#" + tempData.getMentionChannel() + ">\n" +
                            "・一時作成チャネル : <#" + tempData.getFstChannel() + ">\n" +
                            "・通話カテゴリ : <#" + tempData.getVoiceCategory() + ">\n" +
                            "・テキストカテゴリ : <#" + tempData.getTextCategory() + ">\n" +
                            "・カスタム募集 : " + tempData.getStereotyped() + "\n" + bys +
                            reacts)
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
