package me.confuser.banmanager.common.commands.global;

import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.data.global.GlobalPlayerMuteRecordData;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class UnmuteAllCommand extends SingleCommand {

  public UnmuteAllCommand(LocaleManager locale) {
    super(CommandSpec.UNMUTEALL.localize(locale), "unmuteall", CommandPermission.UNMUTEALL, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 1) {
      return CommandResult.INVALID_ARGS;
    }

    // Check if UUID vs name
    final String playerName = args.get(0);
    final boolean isUUID = playerName.length() > 16;
    boolean isMuted;

    if (isUUID) {
      isMuted = plugin.getPlayerMuteStorage().isMuted(UUID.fromString(playerName));
    } else {
      isMuted = plugin.getPlayerMuteStorage().isMuted(playerName);
    }

    if (!isMuted) {
      Message.UNMUTE_ERROR_NOEXISTS.send(sender, "player", playerName);
      return CommandResult.SUCCESS;
    }

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      PlayerMuteData mute;

      if (isUUID) {
        mute = plugin.getPlayerMuteStorage().getMute(UUID.fromString(playerName));
      } else {
        mute = plugin.getPlayerMuteStorage().getMute(playerName);
      }

      if (mute == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      PlayerData actor;

      if (!sender.isConsole()) {
        try {
          actor = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(sender));
        } catch (SQLException e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }
      } else {
        actor = plugin.getPlayerStorage().getConsole();
      }

      GlobalPlayerMuteRecordData record = new GlobalPlayerMuteRecordData(mute.getPlayer(), actor);

      int unmuted;

      try {
        unmuted = plugin.getGlobalPlayerMuteRecordStorage().create(record);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (unmuted == 0) {
        return;
      }

      Message.UNMUTEALL_NOTIFY.send(sender,
             "actor", actor.getName(),
             "player", mute.getPlayer().getName(),
             "playerId", mute.getPlayer().getUUID().toString());
    });

    return CommandResult.SUCCESS;
  }
}
