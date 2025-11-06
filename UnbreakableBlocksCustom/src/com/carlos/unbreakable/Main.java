package com.carlos.unbreakable;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin implements Listener, TabExecutor {

    private final Set<Material> unbreakable = new HashSet<>();
    private final Map<Location, UUID> placedBlocks = new HashMap<>();

    @Override
    public void onEnable() {
        // Load from config
        getConfig().addDefault("unbreakable-blocks", new ArrayList<String>());
        getConfig().options().copyDefaults(true);
        saveConfig();

        for (String matName : getConfig().getStringList("unbreakable-blocks")) {
            try {
                unbreakable.add(Material.valueOf(matName));
            } catch (IllegalArgumentException ignored) {}
        }

        // default built-in entries (optional — you can remove these if you prefer only config-driven)
        Collections.addAll(unbreakable
        );

        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("unbreakable")).setExecutor(this);
        getLogger().info("UnbreakableBlocksCustom enabled with command support!");
    }

    @Override
    public void onDisable() {
        saveBlockList();
    }

    private void saveBlockList() {
        List<String> mats = new ArrayList<>();
        for (Material m : unbreakable) mats.add(m.name());
        getConfig().set("unbreakable-blocks", mats);
        saveConfig();
    }

    // --- EVENTS ---
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (unbreakable.contains(event.getBlock().getType())) {
            placedBlocks.put(event.getBlock().getLocation(), event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material type = event.getBlock().getType();

        if (!unbreakable.contains(type)) return;

        UUID owner = placedBlocks.get(event.getBlock().getLocation());
        if (player.hasPermission("unbreakable.bypass") || (owner != null && owner.equals(player.getUniqueId()))) {
            placedBlocks.remove(event.getBlock().getLocation());
            return;
        }

        event.setCancelled(true);
        player.sendMessage("§cThat block is unbreakable!");
    }

    // --- COMMAND HANDLER ---
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§e/unbreakable <add|remove|list> [block]");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add" -> handleAdd(player, args);
            case "remove" -> handleRemove(player, args);
            case "list" -> handleList(player);
            default -> player.sendMessage("§cUnknown subcommand!");
        }

        return true;
    }

    private void handleAdd(Player player, String[] args) {
        if (!player.hasPermission("unbreakable.add")) {
            player.sendMessage("§cYou don't have permission to add blocks!");
            return;
        }

        Material target;
        if (args.length >= 2) {
            try {
                target = Material.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid block type!");
                return;
            }
        } else {
            Block block = player.getTargetBlockExact(10);
            if (block == null) {
                player.sendMessage("§cYou're not looking at any block!");
                return;
            }
            target = block.getType();
        }

        if (unbreakable.contains(target)) {
            player.sendMessage("§eThat block is already unbreakable.");
            return;
        }

        unbreakable.add(target);
        saveBlockList();
        player.sendMessage("§aAdded §f" + target.name() + " §ato unbreakable blocks!");
    }

    private void handleRemove(Player player, String[] args) {
        if (!player.hasPermission("unbreakable.remove")) {
            player.sendMessage("§cYou don't have permission to remove blocks!");
            return;
        }

        Material target;
        if (args.length >= 2) {
            try {
                target = Material.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid block type!");
                return;
            }
        } else {
            Block block = player.getTargetBlockExact(10);
            if (block == null) {
                player.sendMessage("§cYou're not looking at any block!");
                return;
            }
            target = block.getType();
        }

        if (!unbreakable.contains(target)) {
            player.sendMessage("§eThat block is not unbreakable.");
            return;
        }

        unbreakable.remove(target);
        saveBlockList();
        player.sendMessage("§aRemoved §f" + target.name() + " §afrom unbreakable blocks!");
    }

    private void handleList(Player player) {
        if (!player.hasPermission("unbreakable.list")) {
            player.sendMessage("§cYou don't have permission to list blocks!");
            return;
        }

        player.sendMessage("§eUnbreakable blocks:");
        for (Material mat : unbreakable) {
            player.sendMessage("§7- §f" + mat.name());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1)
            return List.of("add", "remove", "list");
        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")))
            return Arrays.stream(Material.values()).map(Enum::name).toList();
        return Collections.emptyList();
    }
}
