package wkwk.Command;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;

public class Help {
    public EmbedBuilder Create(String serverName, User user, boolean admin) {
        String prefix = "/";
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("BOT情報案内 With " + serverName)
                .setAuthor(user);
        if (admin) {
            embed.addField("[ADMIN]確認用コマンド一覧", "・`" + prefix + "help` -> コマンド一覧を表示\n" +
                            "・`" + prefix + "show` -> サーバーの設定状況を確認\n" +
                            "・`" + prefix + "ping` -> サーバーの回線速度を表示します")
                    .addField("[ADMIN]設定コマンド一覧", "・`" + prefix + "setup` -> 必要なチャンネルとカテゴリを自動作成\n" +
                            "・`" + prefix + "set prefix <1~100文字>` -> コマンドの前に打つ文字を変更\n" +
                            "・`" + prefix + "set vcat <カテゴリ>` -> 一時通話の作成先を変更\n" +
                            "・`" + prefix + "set tcat <カテゴリ>` -> 一時チャットの作成先を変更\n" +
                            "・`" + prefix + "set first <チャンネル>` -> 通話作成用チャンネルを変更\n" +
                            "・`" + prefix + "set men <チャンネル>` -> 募集送信チャンネル変更\n" +
                            "・`" + prefix + "set enable temp <true or false>`↓\n　一時通話チャンネル作成切替\n" +
                            "・`" + prefix + "set enable text <true or false>`↓\n　一時テキストチャンネル作成切替\n" +
                            "・`" + prefix + "set size <0~99の数字>` -> 一時通話初期人数変更\n" +
                            "・`" + prefix + "set role <ロール> <絵文字>`↓\n　リアクションロールの付与ロールと絵文字を変更\n" +
                            "・`" + prefix + "set mess <メッセージID> <チャンネル>`↓\n　リアクションロールの対象メッセージを変更\n" +
                            "・`" + prefix + "set namepreset <100文字以内>`->　チャンネルネーム候補を追加\n" +
                            "・`" + prefix + "remove role`↓\n　リアクションロールを選んで削除\n" +
                            "・`" + prefix + "remove namepreset`->　名前を選んで削除\n" +
                            "・`" + prefix + "start delete <削除までの時間> <単位>`↓\nコマンドを打ったチャンネルで自動削除を有効化します \n単位には s m h d が使用できます\n" +
                            "・`" + prefix + "stop delete`->　コマンドを打ったチャンネルの自動削除を停止します\n")
                    .addField("[ADMIN]ログ設定コマンド一覧", "・`" + prefix + "set logging CHAT <ログを保存したいチャンネル>`↓\n　入力したチャンネルに対象のチャンネルで消された\n　メッセージのログを出力します\n" +
                            "・`" + prefix + "set logging USER`↓\n　入力したチャンネルにサーバーの\n　ユーザー入退室ログを出力します\n" +
                            "・`" + prefix + "remove logging` -> 選択したログ設定を削除します")
                    .addField("[ADMIN]募集テンプレ設定", "・`" + prefix + "set stereo <テンプレ内容>` : テンプレ内で使える置換！\n" +
                            "　　-`&user&` : 送信を選択したユーザーのメンションに置換\n" +
                            "　　-`&text&` : 募集コマンドの募集内容で入力した内容に置換\n" +
                            "　　-`&channel&` : 募集したい通話チャネルに置換\n" +
                            "　　-`&everyone&` : Everyoneメンションに置換\n" +
                            "　　-`&here&` : Hereメンションに置換\n" +
                            "　　-`/n` : 改行")
                    .addField("[ADMIN]ユーティリティ", "・`" + prefix + "mess <文字>` -> メッセージをBOTに送信させなおす\n");


        }
        embed.addField("[USER]一時チャネルコマンド一覧", "・`" + prefix + "name <文字>`or`" + prefix + "n <文字>` -> チャンネルの名前を変更\n" +
                        "・`" + prefix + "size <数字>`or`" + prefix + "s <数字>` -> 通話参加人数を変更\n" +
                        "・`" + prefix + "men <募集内容>`or`" + prefix + "m <募集内容>`↓\n　募集チャットの内容を書いて送信\n")
                .setColor(Color.BLUE)
                .setThumbnail("https://i.imgur.com/oRw9ePg.png");
        return embed;
    }
}
