package com.carlos.unbreakable;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Main extends JavaPlugin implements Listener, TabExecutor {

    // Set to track which block types are unbreakable globally
    private final Set<Material> unbreakable = new HashSet<>();
    // Map to track ownership: Location -> UUID of the player who placed it
    private final Map<Location, UUID> placedBlocks = new HashMap<>(); 
    
    // --- Lifecycle Methods ---

    @Override
    public void onEnable() {
        // Load configurations and set defaults
        getConfig().addDefault("unbreakable-blocks", new ArrayList<String>());
        getConfig().addDefault("messages.break-deny", "§cThat block is unbreakable!");
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Load unbreakable materials from config
        for (String matName : getConfig().getStringList("unbreakable-blocks")) {
            try {
                unbreakable.add(Material.valueOf(matName));
            } catch (IllegalArgumentException ignored) {}
        }

        // Load placed block ownership data
        loadPlacedBlocks();

        // Register events and command executor
        // FIX: Explicitly cast 'this' to the Bukkit Listener interface to resolve import conflict
        Bukkit.getPluginManager().registerEvents((org.bukkit.event.Listener) this, this);
        Objects.requireNonNull(getCommand("unbreakable")).setExecutor(this);
        getLogger().info("UnbreakableBlocksCustom enabled with full persistence and protection!");
    }

    @Override
    public void onDisable() {
        saveBlockList();
        savePlacedBlocks(); // Save placed block ownership data
    }

    // --- Persistence Handlers ---
    
    // Saves the list of unbreakable materials to config.yml
    private void saveBlockList() {
        List<String> mats = new ArrayList<>();
        for (Material m : unbreakable) mats.add(m.name());
        getConfig().set("unbreakable-blocks", mats);
        saveConfig();
    }

    // Gets the custom file for placed block data (placed_blocks.yml)
    private File getPlacedBlocksFile() {
        return new File(getDataFolder(), "placed_blocks.yml");
    }

    // Loads placed block data from placed_blocks.yml
    private void loadPlacedBlocks() {
        File placedFile = getPlacedBlocksFile();
        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(placedFile);
        
        if (dataConfig.contains("placed")) {
            ConfigurationSection placedSection = dataConfig.getConfigurationSection("placed");
            if (placedSection != null) {
                for (String key : placedSection.getKeys(false)) {
                    // Key format: "worldName;x;y;z"
                    String[] parts = key.split(";");
                    if (parts.length == 4) {
                        World world = Bukkit.getWorld(parts[0]);
                        if (world == null) continue;

                        try {
                            // Parsing block coordinates (X, Y, Z)
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            int z = Integer.parseInt(parts[3]);
                            
                            Location loc = new Location(world, x, y, z);
                            String uuidString = placedSection.getString(key);
                            
                            if (uuidString != null) {
                                // Parsing UUID
                                placedBlocks.put(loc, UUID.fromString(uuidString));
                            }
                        // FIX: Catching IllegalArgumentException covers both NumberFormatException 
                        // and IllegalArgumentException from UUID.fromString, resolving the multi-catch error.
                        } catch (IllegalArgumentException ignored) {
                            // Malformed entry ignored.
                        }
                    }
                }
            }
        }
        getLogger().info("Loaded " + placedBlocks.size() + " placed block ownership records.");
    }

    // Saves placed block data to placed_blocks.yml
    private void savePlacedBlocks() {
        File placedFile = getPlacedBlocksFile();
        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(placedFile);
        
        dataConfig.set("placed", null); // Clear old data

        ConfigurationSection placedSection = dataConfig.createSection("placed");
        for (Map.Entry<Location, UUID> entry : placedBlocks.entrySet()) {
            Location loc = entry.getKey();
            // Create a unique key for the location (World;X;Y;Z)
            String key = loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
            placedSection.set(key, entry.getValue().toString());
        }

        try {
            dataConfig.save(placedFile);
        } catch (IOException e) {
            getLogger().severe("Could not save placed blocks data: " + e.getMessage());
        }
    }


    // --- EVENTS ---
    
    // Track unbreakable blocks placed by players
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (unbreakable.contains(event.getBlock().getType())) {
            placedBlocks.put(event.getBlock().getLocation(), event.getPlayer().getUniqueId());
        }
    }

    // Handle player breaking unbreakable blocks
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        if (!unbreakable.contains(type)) return;

        UUID owner = placedBlocks.get(block.getLocation());
        
        // Bypass if: 1. Player has bypass permission, OR 2. Player is the owner
        if (player.hasPermission("unbreakable.bypass") || (owner != null && owner.equals(player.getUniqueId()))) {
            placedBlocks.remove(block.getLocation()); // Remove from tracked list if broken
            return;
        }

        event.setCancelled(true);
        String denyMessage = getConfig().getString("messages.break-deny", "§cThat block is unbreakable!");
        player.sendMessage(denyMessage);
    }
    
    // Handle blocks being destroyed by explosions (Explosion Resistance)
    @EventHandler
    public void onBlockExplode(EntityExplodeEvent event) {
        // Use removeIf to filter out any blocks that are in the 'unbreakable' set
        event.blockList().removeIf(block -> unbreakable.contains(block.getType()));
    }
    
    // Handle blocks being pushed/pulled by pistons (Piston Protection)
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        // Check the block being pushed and all adjacent blocks
        for (Block block : event.getBlocks()) {
            if (unbreakable.contains(block.getType())) {
                event.setCancelled(true);
                return; 
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        // Check the blocks being pulled
        for (Block block : event.getBlocks()) {
            if (unbreakable.contains(block.getType())) {
                event.setCancelled(true);
                return; 
            }
        }
    }


    // --- COMMAND HANDLER ---
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // General command usage permission check
        if (!sender.hasPermission("unbreakable.admin") && !(sender instanceof Player && args.length > 0 && args[0].equalsIgnoreCase("list") && sender.hasPermission("unbreakable.list"))) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§e/unbreakable <add|remove|list|check> [block]");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add" -> handleAdd(player, args);
            case "remove" -> handleRemove(player, args);
            case "list" -> handleList(player);
            case "check" -> handleCheck(player); 
            default -> player.sendMessage("§cUnknown subcommand! Use /unbreakable <add|remove|list|check>");
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
            if (block == null || block.getType().isAir()) {
                player.sendMessage("§cYou must specify a block or look at one!");
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
            if (block == null || block.getType().isAir()) {
                player.sendMessage("§cYou must specify a block or look at one!");
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

        player.sendMessage("§e--- Unbreakable Blocks (" + unbreakable.size() + ") ---");
        for (Material mat : unbreakable) {
            player.sendMessage("§7- §f" + mat.name());
        }
        player.sendMessage("§e-----------------------------");
    }
    
    // Check block ownership command
    private void handleCheck(Player player) {
        if (!player.hasPermission("unbreakable.check")) {
            player.sendMessage("§cYou don't have permission to check block ownership!");
            return;
        }

        Block block = player.getTargetBlockExact(10);
        if (block == null || block.getType().isAir()) {
            player.sendMessage("§cYou're not looking at any block!");
            return;
        }

        player.sendMessage("§eChecking Block: §f" + block.getType().name());

        if (!unbreakable.contains(block.getType())) {
            player.sendMessage("§7Status: §eBlock type is not set as unbreakable.");
            return;
        }

        UUID ownerId = placedBlocks.get(block.getLocation());

        if (ownerId != null) {
            // Use getOfflinePlayer to retrieve the name even if the player is offline
            String ownerName = Bukkit.getOfflinePlayer(ownerId).getName();
            player.sendMessage("§6Status: §aUNBREAKABLE (PLACED)");
            player.sendMessage("§6Owner: §f" + (ownerName != null ? ownerName : "Unknown Player") + " §7(" + ownerId + ")");
        } else {
            player.sendMessage("§6Status: §aUNBREAKABLE (NATURAL/UNTRACKED)");
            player.sendMessage("§7Owner data not found. Only players with §cunbreakable.bypass§7 can break it.");
        }
    }


    // --- TAB COMPLETION ---
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1)
            return List.of("add", "remove", "list", "check");
        
        // Tab complete Material names for 'add' and 'remove'
        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")))
            return Arrays.stream(Material.values())
                .map(Enum::name)
                .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                .toList();
        
        return Collections.emptyList();
    }
}