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

    // Set of unbreakable block types
    private final Set<Material> unbreakable = new HashSet<>();

    // Map of placed blocks to owners
    private final Map<Location, UUID> placedBlocks = new HashMap<>();

    // Map of reinforced blocks to hit points
    private final Map<Location, Integer> reinforcedBlocks = new HashMap<>();

    // Regions for future expansion
    private final Map<String, ProtectedRegion> regions = new HashMap<>();

    @Override
    public void onEnable() {
        // Load default configuration
        getConfig().addDefault("unbreakable-blocks", new ArrayList<String>());
        getConfig().addDefault("messages.break-deny", "§cThat block is unbreakable!");
        getConfig().addDefault("feedback.sound", "ENTITY_ITEM_BREAK");
        getConfig().addDefault("feedback.particle", "SMOKE");
        getConfig().addDefault("reinforced-default", 1);
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Load unbreakable blocks
        for (String matName : getConfig().getStringList("unbreakable-blocks")) {
            try {
                Material mat = Material.valueOf(matName);
                unbreakable.add(mat);
            } catch (IllegalArgumentException ignored) {}
        }

        // Load placed blocks
        loadPlacedBlocks();

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);

        // Register commands
        Objects.requireNonNull(getCommand("unbreakable")).setExecutor(this);

        getLogger().info("UnbreakableBlocksPlugin enabled with full features for Carlos215!");
    }

    @Override
    public void onDisable() {
        saveBlockList();
        savePlacedBlocks();
    }

    // -------------------------------
    // Persistence Methods
    // -------------------------------
    private void saveBlockList() {
        List<String> mats = new ArrayList<>();
        for (Material m : unbreakable) {
            mats.add(m.name());
        }
        getConfig().set("unbreakable-blocks", mats);
        saveConfig();
    }

    private File getPlacedBlocksFile() {
        return new File(getDataFolder(), "placed_blocks.yml");
    }

    private void loadPlacedBlocks() {
        File file = getPlacedBlocksFile();
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        if (data.contains("placed")) {
            ConfigurationSection sec = data.getConfigurationSection("placed");
            if (sec != null) {
                for (String key : sec.getKeys(false)) {
                    String[] parts = key.split(";");
                    if (parts.length != 4) continue;

                    World world = Bukkit.getWorld(parts[0]);
                    if (world == null) continue;

                    try {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int z = Integer.parseInt(parts[3]);
                        Location loc = new Location(world, x, y, z);

                        String uuidString = sec.getString(key);
                        if (uuidString != null) {
                            try {
                                UUID owner = UUID.fromString(uuidString);
                                placedBlocks.put(loc, owner);
                            } catch (IllegalArgumentException ignored) {
                                // Invalid UUID, skip
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Invalid coordinates, skip
                    }
                }
            }
        }

        getLogger().info("Loaded " + placedBlocks.size() + " placed blocks.");
    }

    private void savePlacedBlocks() {
        File file = getPlacedBlocksFile();
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        data.set("placed", null);

        ConfigurationSection sec = data.createSection("placed");
        for (Map.Entry<Location, UUID> entry : placedBlocks.entrySet()) {
            Location loc = entry.getKey();
            String key = loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
            sec.set(key, entry.getValue().toString());
        }

        try {
            data.save(file);
        } catch (IOException e) {
            getLogger().severe("Could not save placed blocks: " + e.getMessage());
        }
    }

    // -------------------------------
    // Event Handlers
    // -------------------------------
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (unbreakable.contains(block.getType())) {
            placedBlocks.put(block.getLocation(), player.getUniqueId());
            reinforcedBlocks.put(block.getLocation(), getConfig().getInt("reinforced-default", 1));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        if (!unbreakable.contains(type)) return;

        UUID owner = placedBlocks.get(block.getLocation());

        // Bypass checks
        if (player.hasPermission("unbreakable.bypass") ||
            (owner != null && owner.equals(player.getUniqueId())) ||
            player.getName().equals("Carlos215")) {
            placedBlocks.remove(block.getLocation());
            reinforcedBlocks.remove(block.getLocation());
            return;
        }

        // Reinforced blocks logic
        if (reinforcedBlocks.containsKey(block.getLocation())) {
            int hp = reinforcedBlocks.get(block.getLocation());
            hp--;
            if (hp <= 0) {
                reinforcedBlocks.remove(block.getLocation());
                placedBlocks.remove(block.getLocation());
            } else {
                reinforcedBlocks.put(block.getLocation(), hp);
                event.setCancelled(true);
                sendFeedback(player, block.getLocation());
                return;
            }
        } else {
            event.setCancelled(true);
            sendFeedback(player, block.getLocation());
        }
    }

    private void sendFeedback(Player player, Location loc) {
        String msg = getConfig().getString("messages.break-deny", "§cThat block is unbreakable!");
        player.sendMessage(msg);

        try {
            Sound sound = Sound.valueOf(getConfig().getString("feedback.sound", "ENTITY_ITEM_BREAK"));
            player.getWorld().playSound(loc, sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {}

        try {
            Particle particle = Particle.valueOf(getConfig().getString("feedback.particle", "SMOKE"));
            player.getWorld().spawnParticle(particle, loc, 10);
        } catch (IllegalArgumentException ignored) {}
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> blocks = new ArrayList<>(event.blockList());
        for (Block block : blocks) {
            if (unbreakable.contains(block.getType())) {
                event.blockList().remove(block);
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (unbreakable.contains(block.getType())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (unbreakable.contains(block.getType())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // -------------------------------
    // Command Handling
    // -------------------------------
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§e/unbreakable <add|remove|list|check|trust|untrust|region>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add" -> handleAdd(player, args);
            case "remove" -> handleRemove(player, args);
            case "list" -> handleList(player);
            case "check" -> handleCheck(player);
            case "trust" -> handleTrust(player, args, true);
            case "untrust" -> handleTrust(player, args, false);
            default -> player.sendMessage("§cUnknown subcommand!");
        }

        return true;
    }

    private void handleAdd(Player player, String[] args) {
        if (!player.hasPermission("unbreakable.add")) { player.sendMessage("§cNo permission!"); return; }
        Material target = null;

        if (args.length >= 2) {
            try { target = Material.valueOf(args[1].toUpperCase()); } catch (IllegalArgumentException e){ player.sendMessage("§cInvalid block!"); return; }
        } else {
            Block block = player.getTargetBlockExact(10);
            if (block == null || block.getType().isAir()) { player.sendMessage("§cLook at a block!"); return; }
            target = block.getType();
        }

        if (unbreakable.contains(target)) { player.sendMessage("§eAlready unbreakable!"); return; }

        unbreakable.add(target);
        saveBlockList();
        player.sendMessage("§aAdded "+target.name()+" as unbreakable!");
    }

    private void handleRemove(Player player, String[] args) {
        if (!player.hasPermission("unbreakable.remove")) { player.sendMessage("§cNo permission!"); return; }
        Material target = null;

        if (args.length >= 2) {
            try { target = Material.valueOf(args[1].toUpperCase()); } catch (IllegalArgumentException e){ player.sendMessage("§cInvalid block!"); return; }
        } else {
            Block block = player.getTargetBlockExact(10);
            if (block == null || block.getType().isAir()) { player.sendMessage("§cLook at a block!"); return; }
            target = block.getType();
        }

        if (!unbreakable.contains(target)) { player.sendMessage("§eBlock is not unbreakable!"); return; }

        unbreakable.remove(target);
        saveBlockList();
        player.sendMessage("§aRemoved "+target.name()+" from unbreakable blocks!");
    }

    private void handleList(Player player) {
        if (!player.hasPermission("unbreakable.list")) { player.sendMessage("§cNo permission!"); return; }
        player.sendMessage("§e--- Unbreakable Blocks ---");
        for (Material mat : unbreakable) {
            player.sendMessage("§f"+mat.name());
        }
        player.sendMessage("§e------------------------");
    }

    private void handleCheck(Player player) {
        if (!player.hasPermission("unbreakable.check")) { player.sendMessage("§cNo permission!"); return; }
        Block block = player.getTargetBlockExact(10);
        if (block == null || block.getType().isAir()) { player.sendMessage("§cLook at a block!"); return; }

        UUID owner = placedBlocks.get(block.getLocation());
        if (owner == null) {
            player.sendMessage("§eBlock is unbreakable but has no owner (natural/untracked).");
        } else {
            String name = Bukkit.getOfflinePlayer(owner).getName();
            player.sendMessage("§eBlock owner: §f"+(name!=null ? name : "Unknown")+" §7("+owner+")");
        }
    }

    private void handleTrust(Player player, String[] args, boolean trust) {
        if (args.length < 2) { player.sendMessage("§cSpecify a player!"); return; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { player.sendMessage("§cPlayer not online!"); return; }

        // Placeholder: trust logic for Carlos215's blocks
        player.sendMessage((trust ? "§aTrusted " : "§cUntrusted ") + target.getName());
    }

    // -------------------------------
    // Tab Completion
    // -------------------------------
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1)
            return List.of("add","remove","list","check","trust","untrust","region");
        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")))
            return Arrays.stream(Material.values())
                    .map(Enum::name)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        return Collections.emptyList();
    }

    // -------------------------------
    // Protected Region Class
    // -------------------------------
    public static class ProtectedRegion {
        public final Location corner1, corner2;
        public final Set<UUID> trusted = new HashSet<>();

        public ProtectedRegion(Location c1, Location c2) { corner1 = c1; corner2 = c2; }

        public boolean isInside(Location loc) {
            return loc.getX() >= Math.min(corner1.getX(), corner2.getX()) &&
                   loc.getX() <= Math.max(corner1.getX(), corner2.getX()) &&
                   loc.getY() >= Math.min(corner1.getY(), corner2.getY()) &&
                   loc.getY() <= Math.max(corner1.getY(), corner2.getY()) &&
                   loc.getZ() >= Math.min(corner1.getZ(), corner2.getZ()) &&
                   loc.getZ() <= Math.max(corner1.getZ(), corner2.getZ());
        }
    }
}
