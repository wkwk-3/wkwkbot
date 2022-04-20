package wkwk;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WkwkSlashCommand {
    DiscordApi api;

    public WkwkSlashCommand(DiscordApi api) {
        this.api = api;
    }

    public void createCommand() {
        if (api.getServerById(953794221985366076L).isPresent()) {
            SlashCommand rePrivateChat =
                    SlashCommand.with("r", "個人チャットの返信",
                            Arrays.asList(
                                    SlashCommandOption.create(SlashCommandOptionType.STRING,"ID","返信先ユーザーID"),
                                    SlashCommandOption.create(SlashCommandOptionType.STRING,"TEXT","本文"),
                                    SlashCommandOption.create(SlashCommandOptionType.STRING,"IMAGE","画像")
                            )
                            ).setDefaultPermission(false).createForServer(api.getServerById(953794221985366076L).get()).join();
            new ApplicationCommandPermissionsUpdater(api.getServerById(953794221985366076L).get())
                .setPermissions(Collections.singletonList(
                        ApplicationCommandPermissions.create(422099698388697108L, ApplicationCommandPermissionType.USER, true)))
            .update(rePrivateChat.getId())
            .join();
        }
        SlashCommand ping =
                SlashCommand.with("ping", "BOTの回線速度を計測").createGlobal(api).join();
        SlashCommand invite =
                SlashCommand.with("invite", "BOT招待リンクを表示").createGlobal(api).join();
        SlashCommand guild =
                SlashCommand.with("guild", "サポートサーバー招待リンクを表示").createGlobal(api).join();
        SlashCommand setup =
                SlashCommand.with("setup", "一時通話作成に必要な要素を生成").createGlobal(api).join();
        SlashCommand set =
                SlashCommand.with("set", "サーバー設定変更",
                                Arrays.asList(
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "vcat", "一時通話チャンネル作成先カテゴリ変更",
                                                Collections.singletonList(
                                                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "category", "対象カテゴリ", true)
                                                )),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "tcat", "一時テキストチャンネル作成先カテゴリ変更",
                                                Collections.singletonList(
                                                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "category", "対象カテゴリ", true)
                                                )),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "first", "一時チャンネル作成時に入る通話チャンネル変更",
                                                Collections.singletonList(
                                                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "voiceChannel", "対象チャンネル", true)
                                                )),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "mention", "メンション送信先変更",
                                                Collections.singletonList(
                                                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "textChannel", "対象チャンネル", true)
                                                )),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND_GROUP, "enable", "機能有効化切り替え",
                                                Arrays.asList(
                                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "temp", "一時チャンネル作成",
                                                                Collections.singletonList(
                                                                        SlashCommandOption.create(SlashCommandOptionType.BOOLEAN, "enable", "有効化切り替え", true)
                                                                )),
                                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "text", "一時テキスト作成",
                                                                Collections.singletonList(
                                                                        SlashCommandOption.create(SlashCommandOptionType.BOOLEAN, "enable", "有効化切り替え", true)
                                                                ))
                                                )),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "size", "通話初期制限人数変更",
                                                Collections.singletonList(
                                                        SlashCommandOption.create(SlashCommandOptionType.LONG, "num", "人数", true)
                                                )),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "role", "リアクションロール",
                                                Arrays.asList(
                                                        SlashCommandOption.create(SlashCommandOptionType.ROLE, "role", "付与ロール", true),
                                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "emoji", "対象絵文字", true)
                                                )),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "mess", "リアクションロール対象メッセージ",
                                                Arrays.asList(
                                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "messageId", "メッセージID", true),
                                                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "textChannel", "対象チャンネル", true)
                                                )),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "namePreset", "チャンネルネーム変更候補追加",
                                                Collections.singletonList(
                                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "name", "チャンネル名候補", true)
                                                )),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND_GROUP, "logging", "ログ設定",
                                                Arrays.asList(
                                                        SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "USER", "ユーザー履歴"),
                                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "CHAT", "チャット履歴",
                                                                Collections.singletonList(
                                                                        SlashCommandOption.create(SlashCommandOptionType.CHANNEL, "textChannel", "対象チャンネル", true)
                                                                ))
                                                )),
                                        SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "stereo", "募集分のテンプレ",
                                                Collections.singletonList(
                                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "template", "募集テンプレート")
                                                ))
                                ))
                        .createGlobal(api)
                        .join();
        SlashCommand remove =
                SlashCommand.with("remove", "設定削除",
                        Arrays.asList(
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "role", "リアクションロール削除表示"),
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "namepreset", "名前候補削除表示"),
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "logging", "ログ表示削除表示")
                        )).createGlobal(api).join();
        SlashCommand start =
                SlashCommand.with("start", "処理開始",
                        Arrays.asList(
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "delete", "オートデリート開始",
                                        Arrays.asList(
                                                SlashCommandOption.create(SlashCommandOptionType.LONG, "time", "削除までの時間"),
                                                SlashCommandOption.create(SlashCommandOptionType.STRING, "unit", "時間の単位 s m h d ")
                                        ))
                        )).createGlobal(api).join();
        SlashCommand stop =
                SlashCommand.with("stop", "処理終了",
                        Arrays.asList(
                                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "delete", "オートデリート終了")
                        )).createGlobal(api).join();
        SlashCommand help =
                SlashCommand.with("help", "ヘルプ").createGlobal(api).join();
        SlashCommand show =
                SlashCommand.with("show", "サーバー情報表示").createGlobal(api).join();
        SlashCommand mess =
                SlashCommand.with("mess", "メッセージ送信",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "text", "メッセージ内容"),
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "url", "添付画像リンク")
                        )
                ).createGlobal(api).join();
        SlashCommand name =
                SlashCommand.with("name", "チャンネル名前変更",
                        Collections.singletonList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "name", "チャンネル名")
                        )
                ).createGlobal(api).join();
        SlashCommand size =
                SlashCommand.with("size", "チャンネル人数変更",
                        Collections.singletonList(
                                SlashCommandOption.create(SlashCommandOptionType.LONG, "size", "チャンネル最大人数")
                        )
                ).createGlobal(api).join();
        SlashCommand men =
                SlashCommand.with("men", "チャンネル募集送信",
                        Collections.singletonList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "text", "募集内容")
                        )
                ).createGlobal(api).join();
        SlashCommand nameS =
                SlashCommand.with("n", "チャンネル名前変更",
                        Collections.singletonList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "name", "チャンネル名")
                        )
                ).createGlobal(api).join();
        SlashCommand sizeS =
                SlashCommand.with("s", "チャンネル人数変更",
                        Collections.singletonList(
                                SlashCommandOption.create(SlashCommandOptionType.LONG, "size", "チャンネル最大人数")
                        )
                ).createGlobal(api).join();
        SlashCommand menS =
                SlashCommand.with("m", "チャンネル募集送信",
                        Collections.singletonList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "text", "募集内容")
                        )
                ).createGlobal(api).join();
    }

    public void allDeleteCommands() {
        List<SlashCommand> commands = api.getGlobalSlashCommands().join();
        for (SlashCommand command : commands) {
            command.deleteGlobal();
        }
    }

    public void commandShow() {
        List<SlashCommand> commands = api.getGlobalSlashCommands().join();
        for (SlashCommand command : commands) {
            System.out.println(command.getName());
        }
    }
}
