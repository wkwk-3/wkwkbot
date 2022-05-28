package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.SelectMenuBuilder;
import org.javacord.api.entity.message.component.SelectMenuOptionBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import wkwk.core.BotLogin;

import java.util.*;

public class HelpSystem extends BotLogin {
    DiscordApi api = getApi();

    List<String> names = new ArrayList<>(){
        {
            add("サーバー設定");
            add("一時チャット");
            add("一時チャット内Command");
            add("自動チャット削除");
            add("リアクションロール");
            add("ログ出力");
            add("その他");
        }
    };

    List<String> urls = new ArrayList<>(){
        {
            add("https://www.wkwk.tech/wkwkbot/Function-Description/ServerSettings");
            add("https://www.wkwk.tech/wkwkbot/Function-Description/candidate");
            add("https://www.wkwk.tech/wkwkbot/Function-Description/Commands-in-temporary-chat");
            add("https://www.wkwk.tech/wkwkbot/Function-Description/Automatic-chat-deletion");
            add("https://www.wkwk.tech/wkwkbot/Function-Description/reaction-role");
            add("https://www.wkwk.tech/wkwkbot/Function-Description/logging");
            add("https://www.wkwk.tech/wkwkbot/Function-Description/Other");
        }
    };
    final Map<String, String> help = new HashMap<>();

    public HelpSystem() {

        for (int i = 0; i < urls.size(); i++) {
            help.put(names.get(i), urls.get(i));
        }

        api.addSelectMenuChooseListener(event -> {
            SelectMenuInteraction interaction = event.getSelectMenuInteraction();
            if (interaction.getCustomId().equals("selectHelp")) {
                StringBuilder response = new StringBuilder();
                interaction.getChosenOptions().forEach(selectMenuOption -> response.append(selectMenuOption.getValue()).append(" : ").append(help.get(selectMenuOption.getValue())).append("\n"));
                interaction.getMessage().delete();
                interaction.createImmediateResponder().setContent(response.toString()).setFlags(InteractionCallbackDataFlag.EPHEMERAL).respond();
            }
        });
    }

    public void sendHelp(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        User sendUser = interaction.getUser();
        SelectMenuBuilder helpMenuBuilder;
        helpMenuBuilder = new SelectMenuBuilder().setCustomId("selectHelp").setPlaceholder("どのヘルプが欲しいですか？").setMaximumValues(names.size()).setMinimumValues(1);
        for (String name : names) {
            helpMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(name).setValue(name).build());
        }
        new MessageBuilder()
                .setContent("ヘルプ選択")
                .addComponents(ActionRow.of(helpMenuBuilder.build())).send(sendUser);
    }
}
