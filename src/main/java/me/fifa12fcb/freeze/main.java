package me.fifa12fcb.freeze;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class main extends JavaPlugin implements CommandExecutor, Listener {
    private Plugin plugin;
    public List <Player> frozenplayers = new ArrayList<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this,this);
        plugin = this;
        (new Scheduler(this)).runTaskTimer(this, 20, 20);

    }
    @EventHandler
    public void movement(PlayerMoveEvent e) {
        if(frozenplayers.contains(e.getPlayer()) && e.getFrom().getY() == e.getTo().getY() && e.getFrom().getX() != e.getTo().getX() && e.getFrom().getZ() != e.getTo().getZ()) {
            e.setCancelled(true);
            e.getPlayer().teleport(e.getFrom());

        }
    }
    @EventHandler
    public void breakblock(BlockPlaceEvent e) {
        if(frozenplayers.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void InteractEvent(PlayerInteractEvent e) {
        if(frozenplayers.contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void hitplayer(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if(frozenplayers.contains(p)){
                e.setCancelled(true);
                e.getDamager().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis player is currently frozen."));
            }
        }
    }
    @EventHandler
    public void command(PlayerCommandPreprocessEvent e) {
        if(frozenplayers.contains(e.getPlayer()) && !e.getMessage().toLowerCase().split(" +")[0].replaceAll("/","").matches("(msg|r|helpop|m|reply|amsg|message|tell)")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED+"You can't send commands while frozen");
        }
    }


    @EventHandler
    public void logout(PlayerQuitEvent e) {
        if(frozenplayers.contains(e.getPlayer())) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.hasPermission("freeze.freeze"))
                    p.sendMessage("");
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c " + e.getPlayer().getName() + " &chas logged out while frozen!"));
                    p.sendMessage("");
            }
            frozenplayers.remove(e.getPlayer());
        }
    }

    @EventHandler
    public void ClickItemInFrozen(InventoryClickEvent e) {
         Player player = (Player) e.getWhoClicked();
         ItemStack clicked = e.getCurrentItem();
         Inventory inventory = e.getInventory();
         if(frozenplayers.contains(player)) {
             e.setCancelled(true);
         }
    }

    @EventHandler
    public void InventoryExit(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (frozenplayers.contains(e.getPlayer())) {
            frozenplayers.remove(e.getPlayer());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                openGUI(player);
                frozenplayers.add(player);
            }, 3L);
        }
    }

    private void openGUI(Player player) {
        Inventory i = Bukkit.createInventory(null, 9, ChatColor.translateAlternateColorCodes('&', "&bFrozen"));

        ItemStack Frozen = new ItemStack(Material.PAPER, 1);
        ItemMeta FrozenMeta = Frozen.getItemMeta();

        FrozenMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cJoin Discord - Screenshare waiting room."));
        ArrayList<String> LoreFrozen = new ArrayList<>();
        LoreFrozen.add(0, ChatColor.translateAlternateColorCodes('&', "&cYou got 5 minutes to join Discord \n &celse you will be banned."));
        FrozenMeta.setLore(LoreFrozen);
        Frozen.setItemMeta(FrozenMeta);

        i.setItem(4, Frozen);

        player.openInventory(i);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.getServer().getPluginCommand("freeze").setExecutor(this::onCommand);
        if(!sender.hasPermission("freeze.freeze")) {
            sender.sendMessage(ChatColor.RED+"You do not have permissions to freeze people");
            return false;
        }
        try{
            String Playertofreeze = args[0];
            for(Player p: plugin.getServer().getOnlinePlayers()) {
                if(p.getName().toLowerCase().equals(args[0].toLowerCase())){
                    for(Player s: frozenplayers) {
                        if(s == p) {
                            sender.sendMessage(ChatColor.GREEN+"Unfroze " + s.getName() + "!");
                            p.sendMessage("");
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have been unfrozen."));
                            p.sendMessage("");
                            frozenplayers.remove(s);
                            return true;
                        }
                    }
                    if((Player) sender == p) {
                        sender.sendMessage(ChatColor.RED+"You can't freeze yourself");
                        return false;
                    }
                    sender.sendMessage(ChatColor.GREEN+"You froze " + p.getName() + "!");
                    frozenplayers.add(p);
                    openGUI(p);
                    p.sendMessage("");
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have been frozen by " + sender.getName()));
                    p.sendMessage("");
                    p.setFlying(false);
                    p.setGameMode(GameMode.SURVIVAL);
                    p.setAllowFlight(false);
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED+"You need to select a online player");
            return false;
        } catch(ArrayIndexOutOfBoundsException e) {
            sender.sendMessage(ChatColor.RED+"You need a player to freeze");
            return true;
        }
    }
}

class Scheduler extends BukkitRunnable {
    private main plugin;
    public Scheduler(main p) {this.plugin = p;}
    @Override
    public void run() {
        for(Player p: plugin.getServer().getOnlinePlayers()) {
            if(plugin.frozenplayers.contains(p)) {
                new ActionBar(ChatColor.translateAlternateColorCodes('&', "&cYou are currently frozen" )).sendToPlayer(p);
            }
        }
    }
}