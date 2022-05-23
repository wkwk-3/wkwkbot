package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.SelectMenuBuilder;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.message.component.SelectMenuOptionBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.interaction.SlashCommandInteraction;
import wkwk.core.BotLogin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HelpSystem extends BotLogin {
    DiscordApi api = getApi();
    ArrayList<String> subCommandsAdmin = new ArrayList<>(
            Arrays.asList(
                    "ping", "invite", "guild", "setup", "add", "delete", "set",
                    "remove", "start", "stop", "help", "show", "mess", "name",
                    "size", "men", "n", "s", "m"
            ));

    ArrayList<String> subCommandsAdminGuide = new ArrayList<>(
            Arrays.asList(
                    "・`/ping` -> サーバー回線速度表示",
                    "・`/invite` -> BOT招待URL取得",
                    "・`/guild` -> サポートサーバー招待URL取得",
                    "・`/setup` -> 必要なチャンネルとカテゴリを自動作成",
                    "・`/add user` -> 非表示状態の一時通話を特定のユーザーに表示させる",
                    "・`/delete user` -> 表示状態の一時通話を特定のユーザーに非表示させる",
                    "NONE",
                    "NONE",
                    "・`/start delete {time} {unit}` -> ",
                    "・`/stop delete` -> ",
                    "・`/help` -> コマンド情報を表示",
                    "・`/show` -> サーバーの設定状況を確認",
                    "・`/mess` -> メッセージをBOTに送信させなおす",
                    "・`/name {文字}` -> チャンネルの名前を変更",
                    "・`/size {数字}` -> 通話参加人数を変更",
                    "・`/men {文字}` -> 募集チャットの内容を書いて送信",
                    "・`/n {文字}` -> チャンネルの名前を変更",
                    "・`/s {数字}` -> 通話参加人数を変更",
                    "・`/m {文字}` -> 募集チャットの内容を書いて送信"
            ));
    ArrayList<String> subCommandsUser = new ArrayList<>(
            Arrays.asList(
                    "ping", "invite", "guild", "help",
                    "name", "size", "men",
                    "n", "s", "m"
            ));
    ArrayList<String> subCommandsSet = new ArrayList<>(
            Arrays.asList(
                    "vcat", "tcat", "first", "mention",
                    "enable", "size", "role",
                    "mess", "namepreset", "logging", "stereo"
            ));
    ArrayList<String> subCommandsSetGuide = new ArrayList<>(
            Arrays.asList(
                    "・`/set vcat {カテゴリ}` -> 一時通話の作成先を変更",
                    "・`/set tcat {カテゴリ}` -> 一時チャットの作成先を変更",
                    "・`/set first {チャンネル}` -> 通話作成用チャンネルを変更",
                    "・`/set mention {チャンネル}` -> 募集送信チャンネル変更",
                    "NONE",
                    "・`/set size {0~99の数字}` -> 一時通話初期人数変更",
                    "・`/set role {ロール} {絵文字}` -> リアクションロールの付与ロールと絵文字を変更",
                    "・`/set mess {メッセージID} {チャンネル}` -> リアクションロールの対象メッセージを変更",
                    "・`/set namepreset {100文字以内}` ->　チャンネルネーム候補を追加",
                    "logging NONE",
                    "・`/set stereo {テンプレ内容}` -> テンプレ内で使える置換！\n" +
                            "　　-`&user&` : 送信を選択したユーザーのメンションに置換\n" +
                            "　　-`&text&` : 募集コマンドの募集内容で入力した内容に置換\n" +
                            "　　-`&channel&` : 募集したい通話チャネルに置換\n" +
                            "　　-`&everyone&` : Everyoneメンションに置換\n" +
                            "　　-`&here&` : Hereメンションに置換\n" +
                            "　　-`/n` : 改行"
            ));
    ArrayList<String> subCommandsRemove = new ArrayList<>(
            Arrays.asList(
                    "role", "namepreset", "logging"
            ));
    ArrayList<String> subCommandsRemoveGuide = new ArrayList<>(
            Arrays.asList(
                    "・`/remove role` ->　リアクションロールを選んで削除",
                    "・`/remove namepreset`->　名前を選んで削除",
                    "・`/remove logging` -> 選択したログ設定を削除します"
            ));

    ArrayList<String> subCommandsEnable = new ArrayList<>(
            Arrays.asList(
                    "temp", "text"
            ));
    ArrayList<String> subCommandsEnableGuide = new ArrayList<>(
            Arrays.asList(
                    "・`/set enable temp {true or false}` -> 一時通話チャンネル作成切替",
                    "・`/set enable text {true or false}` -> 一時テキストチャンネル作成切替"
            ));
    final Map<String, String> enableGuide = new HashMap<String, String>() {
        {
            for (int i = 0; i < subCommandsEnable.size(); i++) {
                put(subCommandsEnable.get(i), subCommandsEnableGuide.get(i));
            }
        }
    };
    ArrayList<String> subCommandsLogging = new ArrayList<>(
            Arrays.asList(
                    "chat", "user"
            ));
    ArrayList<String> subCommandsLoggingGuide = new ArrayList<>(
            Arrays.asList(
                    "・`/set logging chat {ログを保存したいチャンネル}`↓\n 入力したチャンネルに対象のチャンネルで消された\n メッセージのログを出力します\n",
                    "・`/set logging user` -> 入力したチャンネルにサーバーの\n ユーザー入退室ログを出力します\n"
            ));
    Map<String, String> allGuide = new HashMap<String, String>() {
        {
            for (int i = 0; i < subCommandsAdmin.size(); i++) {
                put(subCommandsAdmin.get(i), subCommandsAdminGuide.get(i));
            }
        }
    };
    Map<String, String> setGuide = new HashMap<String, String>() {
        {
            for (int i = 0; i < subCommandsSet.size(); i++) {
                put(subCommandsSet.get(i), subCommandsSetGuide.get(i));
            }
        }
    };
    Map<String, String> removeGuide = new HashMap<String, String>() {
        {
            for (int i = 0; i < subCommandsRemove.size(); i++) {
                put(subCommandsRemove.get(i), subCommandsRemoveGuide.get(i));
            }
        }
    };
    Map<String, String> loggingGuide = new HashMap<String, String>() {
        {
            for (int i = 0; i < subCommandsLogging.size(); i++) {
                put(subCommandsLogging.get(i), subCommandsLoggingGuide.get(i));
            }
        }
    };

    public HelpSystem() {
        api.addSelectMenuChooseListener(event -> {
            SelectMenuInteraction menuInteraction = event.getSelectMenuInteraction();
            String cmd = menuInteraction.getCustomId();
            User sendUser = menuInteraction.getUser();
            switch (cmd) {
                case "selectHelp":
                    if (menuInteraction.getChannel().isPresent() && menuInteraction.getChannel().get().asPrivateChannel().isPresent()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        boolean set = false;
                        boolean remove = false;
                        for (SelectMenuOption option : menuInteraction.getChosenOptions()) {
                            set = option.getValue().equals("set");
                            remove = option.getValue().equals("remove");
                            if (!option.getValue().equals("set") && !option.getValue().equals("remove")) {
                                stringBuilder.append(allGuide.get(option.getValue())).append("\n");
                            }
                        }
                        if (!stringBuilder.toString().equals("")) {
                            sendUser.sendMessage(
                                    new EmbedBuilder().setTitle("ヘルプ")
                                            .setDescription(stringBuilder.toString())
                            );
                        }
                        if (set) {
                            SelectMenuBuilder helpMenuBuilder;
                            helpMenuBuilder = new SelectMenuBuilder().setCustomId("selectSet").setPlaceholder("どのSetヘルプが欲しいですか？").setMaximumValues(subCommandsSet.size()).setMinimumValues(1);
                            for (String subs : subCommandsSet) {
                                helpMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(subs).setValue(subs).build());
                            }
                            new MessageBuilder()
                                    .setContent("Setヘルプ選択")
                                    .addComponents(ActionRow.of(helpMenuBuilder.build())).send(sendUser);
                        }
                        if (remove) {
                            SelectMenuBuilder helpMenuBuilder;
                            helpMenuBuilder = new SelectMenuBuilder().setCustomId("selectRemove").setPlaceholder("どのRemoveヘルプが欲しいですか？").setMaximumValues(subCommandsRemove.size()).setMinimumValues(1);
                            for (String subs : subCommandsRemove) {
                                helpMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(subs).setValue(subs).build());
                            }
                            new MessageBuilder()
                                    .setContent("Removeヘルプ選択")
                                    .addComponents(ActionRow.of(helpMenuBuilder.build())).send(sendUser);
                        }
                    }
                    menuInteraction.getMessage().delete();
                    break;
                case "selectSet": {
                    StringBuilder stringBuilder = new StringBuilder();
                    boolean enable = false;
                    boolean logging = false;
                    for (SelectMenuOption option : menuInteraction.getChosenOptions()) {
                        enable = option.getValue().equals("enable");
                        logging = option.getValue().equals("logging");
                        if (!option.getValue().equals("enable") && !option.getValue().equals("logging")) {
                            stringBuilder.append(setGuide.get(option.getValue())).append("\n");
                        }
                    }
                    if (!stringBuilder.toString().equals("")) {
                        sendUser.sendMessage(
                                new EmbedBuilder().setTitle("Setヘルプ")
                                        .setDescription(stringBuilder.toString())
                        );
                    }
                    if (enable) {
                        SelectMenuBuilder helpMenuBuilder;
                        helpMenuBuilder = new SelectMenuBuilder().setCustomId("selectEnable").setPlaceholder("どのEnableヘルプが欲しいですか？").setMaximumValues(subCommandsEnable.size()).setMinimumValues(1);
                        for (String subs : subCommandsEnable) {
                            helpMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(subs).setValue(subs).build());
                        }
                        new MessageBuilder()
                                .setContent("Enableヘルプ選択")
                                .addComponents(ActionRow.of(helpMenuBuilder.build())).send(sendUser);
                    }
                    if (logging) {
                        SelectMenuBuilder helpMenuBuilder;
                        helpMenuBuilder = new SelectMenuBuilder().setCustomId("selectLogging").setPlaceholder("どのLoggingヘルプが欲しいですか？").setMaximumValues(subCommandsLogging.size()).setMinimumValues(1);
                        for (String subs : subCommandsLogging) {
                            helpMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(subs).setValue(subs).build());
                        }
                        new MessageBuilder()
                                .setContent("Loggingヘルプ選択")
                                .addComponents(ActionRow.of(helpMenuBuilder.build())).send(sendUser);
                    }
                    menuInteraction.getMessage().delete();
                    break;
                }
                case "selectRemove": {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (SelectMenuOption option : menuInteraction.getChosenOptions()) {
                        stringBuilder.append(removeGuide.get(option.getValue())).append("\n");
                    }
                    if (!stringBuilder.toString().equals("")) {
                        sendUser.sendMessage(
                                new EmbedBuilder().setTitle("Removeヘルプ")
                                        .setDescription(stringBuilder.toString())
                        );
                    }
                    menuInteraction.getMessage().delete();
                    break;
                }
                case "selectEnable": {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (SelectMenuOption option : menuInteraction.getChosenOptions()) {
                        stringBuilder.append(enableGuide.get(option.getValue())).append("\n");
                    }
                    if (!stringBuilder.toString().equals("")) {
                        sendUser.sendMessage(
                                new EmbedBuilder().setTitle("Enableヘルプ")
                                        .setDescription(stringBuilder.toString())
                        );
                    }
                    menuInteraction.getMessage().delete();
                    break;
                }
                case "selectLogging": {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (SelectMenuOption option : menuInteraction.getChosenOptions()) {
                        stringBuilder.append(loggingGuide.get(option.getValue())).append("\n");
                    }
                    if (!stringBuilder.toString().equals("")) {
                        sendUser.sendMessage(
                                new EmbedBuilder().setTitle("Loggingヘルプ")
                                        .setDescription(stringBuilder.toString())
                        );
                    }
                    menuInteraction.getMessage().delete();
                    break;
                }
            }
        });
    }

    public void sendHelp(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        String cmd = interaction.getCommandName();
        Server server = null;
        if (interaction.getServer().isPresent()) {
            server = interaction.getServer().get();
        }
        if (server != null) {
            if (interaction.getChannel().isPresent()) {
                User sendUser = interaction.getUser();
                boolean isAdmin = server.getAllowedPermissions(sendUser).contains(PermissionType.ADMINISTRATOR);
                if (cmd.equals("help")) {
                    SelectMenuBuilder helpMenuBuilder;
                    if (isAdmin) {
                        helpMenuBuilder = new SelectMenuBuilder().setCustomId("selectHelp").setPlaceholder("どのヘルプが欲しいですか？").setMaximumValues(subCommandsAdmin.size()).setMinimumValues(1);
                        for (String subs : subCommandsAdmin) {
                            helpMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(subs).setValue(subs).build());
                        }
                    } else {
                        helpMenuBuilder = new SelectMenuBuilder().setCustomId("selectHelp").setPlaceholder("どのヘルプが欲しいですか？").setMaximumValues(subCommandsUser.size()).setMinimumValues(1);
                        for (String subs : subCommandsUser) {
                            helpMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(subs).setValue(subs).build());
                        }
                    }
                    new MessageBuilder()
                            .setContent("ヘルプ選択")
                            .addComponents(ActionRow.of(helpMenuBuilder.build())).send(sendUser);
                }
            }
        } else {
            User sendUser = interaction.getUser();
            SelectMenuBuilder helpMenuBuilder;
            helpMenuBuilder = new SelectMenuBuilder().setCustomId("selectHelp").setPlaceholder("どのヘルプが欲しいですか？").setMaximumValues(subCommandsAdmin.size()).setMinimumValues(1);
            for (String subs : subCommandsAdmin) {
                helpMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(subs).setValue(subs).build());
            }
            new MessageBuilder()
                    .setContent("ヘルプ選択")
                    .addComponents(ActionRow.of(helpMenuBuilder.build())).send(sendUser);
        }
    }
}
