package de.flattze.mapsync.commands;

import de.flattze.mapsync.MapSyncPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapSyncCommand implements CommandExecutor {

    private final MapSyncPlugin plugin;

    public MapSyncCommand(MapSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl verwenden.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§7Verfügbare Befehle: /mapsync upload | gui | migrate | mymaps");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "upload" -> plugin.getGuiManager().uploadCurrentMap(player);
            case "gui" -> plugin.getGuiManager().openPlayerMapGUI(player);
            case "mymaps" -> plugin.getGuiManager().openOwnedMaps(player, 1);
            default -> player.sendMessage("§cUnbekannter Subbefehl.");
        }
        return true;
    }
}
