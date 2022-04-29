package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import wkwk.dao.DiscordDAO;
import wkwk.record.ReactionRoleRecord;

public class PresetSystem {
    DiscordApi api;

    public PresetSystem(DiscordApi api) {
        this.api = api;
    }

    public void run() {
        DiscordDAO dao = new DiscordDAO();
        api.addSelectMenuChooseListener(event -> {
            SelectMenuInteraction menuInteraction = event.getSelectMenuInteraction();
            String cmd = menuInteraction.getCustomId();
            String response = null;
            User user = menuInteraction.getUser();
            if (menuInteraction.getChannel().isPresent()) {
                String serverId = menuInteraction.getServer().get().getIdAsString();
                if (api.getServerById(serverId).get().getPermissions(user).getAllowedPermission().contains(PermissionType.ADMINISTRATOR)) {
                    switch (cmd) {
                        case "removeName":
                            if (api.getServerById(serverId).isPresent() && api.getServerById(serverId).get().getPermissions(menuInteraction.getUser()).getAllowedPermission().contains(PermissionType.ADMINISTRATOR) || menuInteraction.getUser().isBotOwner()) {
                                String name = menuInteraction.getChosenOptions().get(0).getValue();
                                if (menuInteraction.getServer().isPresent()) {
                                    dao.deleteNamePreset(menuInteraction.getServer().get().getIdAsString(), name);
                                    response = name + "を削除しました";
                                } else response = "削除に失敗しました";
                            }
                            break;
                        case "removeLogging":
                            String[] inputs = menuInteraction.getChosenOptions().get(0).getValue().split(" ");
                            dao.deleteLogging(inputs[0], inputs[1], inputs[2]);
                            response = "削除しました";
                            break;
                        case "removeRole":
                            String selectEmoji = menuInteraction.getChosenOptions().get(0).getValue();
                            ReactionRoleRecord record = dao.getReactAllData(menuInteraction.getServer().get().getIdAsString());
                            for (String emoji : record.getEmoji()) {
                                if (selectEmoji.equals(emoji)) {
                                    dao.deleteRoles(emoji, record.getMessageID());
                                    response = emoji + "を削除しました";
                                    break;
                                }
                            }

                            break;
                    }
                }
                if (response != null) {
                    menuInteraction.getMessage().delete();
                    menuInteraction.createImmediateResponder().setFlags(InteractionCallbackDataFlag.EPHEMERAL).setContent(response).respond();
                }
            }
        });
    }
}
