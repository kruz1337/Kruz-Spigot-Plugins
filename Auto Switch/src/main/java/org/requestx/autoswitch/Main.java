package org.requestx.autoswitch;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener
{
  File fileConfig = new File(this.getDataFolder(), "config.yml");
  FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(fileConfig);

  String clientVersion = getServer().getClass().getPackage().getName().split("\\.")[3].replace("v", "").replace("R", "").replace("_", ".").trim().toString();
  boolean isOlderVersion = (clientVersion.contains("1.8") || clientVersion.contains("1.9") || clientVersion.contains("1.10") || clientVersion.contains("1.11") || clientVersion.contains("1.12") || clientVersion.contains("1.13") || clientVersion.contains("1.14") || clientVersion.contains("1.15")); 

  public HashMap<Player, Integer> slotMap = new HashMap<>();
  public HashMap<Player, BukkitTask> taskMap = new HashMap<>();

  public void onEnable()
  {
    if(fileConfig.length() == 0)
		{
			createDefaultConfig();
		}
		else
		{
			checkDefaultConfig();
		}

    defaultConfig.options().copyDefaults(false);

    getServer().getPluginManager().registerEvents(this, this);
		getServer().getConsoleSender().sendMessage("§a[AutoSwitch] Has been enabled.");
  }

  public void onDisable()
  {
		getServer().getConsoleSender().sendMessage("§c[AutoSwitch] Has been disabled.");
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
  {
    if (!(sender instanceof Player)) {
      return false;
    }

    Player player = (Player) sender;

    if (command.getName().equals("autoswitch"))
    {
      if (args.length == 0 || (args.length == 1 && !args[0].equals("help") && !args[0].equals("reload") && !args[0].equals("toggle") && !args[0].equals("switchback")))
      {
			  player.sendMessage("§5[AutoSwitch] " + defaultConfig.getString("strings.help").replace("{0}", "/autoswitch help"));
      }
      else if (args[0].equals("help"))
      {
        String helpString = "§5[AutoSwitch]\n"
        + "§5> §rCreated by §8github.§kcom§r§8/kruz1337\n" 
        + "§5> §r/autoswitch toggle - §9Enables/Disables auto tool switch\n" 
        + "§5> §r/autoswitch switchback - §9Enables/Disables switch back after using tool\n" 
        + "§5> §r/autoswitch help - §9Show's Help Page\n"
        + "§5> §r/autoswitch reload - §9Reload's Config\n";

        player.sendMessage(helpString);
      }
      else if (args[0].equals("toggle"))
      {
        defaultConfig.set("users." + player.getUniqueId().toString() + ".enabled-autoswitch", !defaultConfig.getBoolean("users." + player.getUniqueId().toString() + ".enabled-autoswitch"));
        player.spigot().sendMessage(defaultConfig.getBoolean("settings.send-toggle-message-chat") ? ChatMessageType.CHAT : ChatMessageType.ACTION_BAR, new TextComponent(defaultConfig.getBoolean("settings.send-toggle-message-chat") ? "§5[AutoSwitch] §r"+ (defaultConfig.getBoolean("users."+player.getUniqueId().toString()+".enabled-autoswitch") ? defaultConfig.getString("strings.enabled-autoswitch") : defaultConfig.getString("strings.disabled-autoswitch")) : (defaultConfig.getBoolean("users."+player.getUniqueId().toString()+".enabled-autoswitch") ? defaultConfig.getString("strings.enabled-autoswitch") : defaultConfig.getString("strings.disabled-autoswitch"))));

        try{
          defaultConfig.save(this.fileConfig);
        }
        catch (Exception e)
        {
          getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin("AutoSwitch"));
        }
      }
      else if (args[0].equals("switchback"))
      {
        defaultConfig.set("users." + player.getUniqueId().toString() + ".enabled-switchback", !defaultConfig.getBoolean("users." + player.getUniqueId().toString() + ".enabled-switchback"));
        player.spigot().sendMessage(defaultConfig.getBoolean("settings.send-toggle-message-chat") ? ChatMessageType.CHAT : ChatMessageType.ACTION_BAR, new TextComponent(defaultConfig.getBoolean("settings.send-toggle-message-chat") ? "§5[AutoSwitch] §r"+ (defaultConfig.getBoolean("users."+player.getUniqueId().toString()+".enabled-switchback") ? defaultConfig.getString("strings.enabled-switchback") : defaultConfig.getString("strings.disabled-switchback")) : (defaultConfig.getBoolean("users."+player.getUniqueId().toString()+".enabled-switchback") ? defaultConfig.getString("strings.enabled-switchback") : defaultConfig.getString("strings.disabled-switchback"))));

        try{
          defaultConfig.save(this.fileConfig);
        }
        catch (Exception e)
        {
          getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin("AutoSwitch"));
        }
      }
      else if (args[0].equals("reload"))
      {
        if(player.isOp())
        {
          defaultConfig = YamlConfiguration.loadConfiguration(fileConfig);
          checkDefaultConfig();
          player.sendMessage("§5[AutoSwitch] " + defaultConfig.getString("strings.reload"));
        }
        else
        {
          player.sendMessage("§5[AutoSwitch] " + defaultConfig.getString("strings.accesdenied"));
        }
      }	
    }
    else if (command.getName().equals("help"))
    {
      String helpString = "§5[AutoSwitch]\n"
      + "§5> §rCreated by §8github.§kcom§r§8/kruz1337\n" 
      + "§5> §r/autoswitch toggle - §9Enables/Disables auto tool switch\n" 
      + "§5> §r/autoswitch switchback - §9Enable/Disable's switch back after using tool\n" 
      + "§5> §r/autoswitch help - §9Show's Help Page\n"
      + "§5> §r/autoswitch reload - §9Reload's Config\n";

      player.sendMessage(helpString);
    }
    else if (command.getName().equals("reload"))
    {
      if(player.isOp())
      {
        defaultConfig = YamlConfiguration.loadConfiguration(fileConfig);
        checkDefaultConfig();
        player.sendMessage("§5[AutoSwitch] " + defaultConfig.getString("strings.reload"));
      }
      else
      {
        player.sendMessage("§5[AutoSwitch] " + defaultConfig.getString("strings.accesdenied"));
      }
    }
    else if (command.getName().equals("toggle"))
    {
      defaultConfig.set("users." + player.getUniqueId().toString() + ".enabled-autoswitch", !defaultConfig.getBoolean("users." + player.getUniqueId().toString() + ".enabled-autoswitch"));
      player.spigot().sendMessage(defaultConfig.getBoolean("settings.send-toggle-message-chat") ? ChatMessageType.CHAT : ChatMessageType.ACTION_BAR, new TextComponent(defaultConfig.getBoolean("settings.send-toggle-message-chat") ? "§5[AutoSwitch] §r"+ (defaultConfig.getBoolean("users."+player.getUniqueId().toString()+".enabled-autoswitch") ? defaultConfig.getString("strings.enabled-autoswitch") : defaultConfig.getString("strings.disabled-autoswitch")) : (defaultConfig.getBoolean("users."+player.getUniqueId().toString()+".enabled-autoswitch") ? defaultConfig.getString("strings.enabled-autoswitch") : defaultConfig.getString("strings.disabled-autoswitch"))));

      try{
        defaultConfig.save(this.fileConfig);
      }
      catch (Exception e)
      {
        getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin("AutoSwitch"));
      }
    }
    else if (command.getName().equals("switchback"))
    {
      defaultConfig.set("users." + player.getUniqueId().toString() + ".enabled-switchback", !defaultConfig.getBoolean("users." + player.getUniqueId().toString() + ".enabled-switchback"));
      player.spigot().sendMessage(defaultConfig.getBoolean("settings.send-toggle-message-chat") ? ChatMessageType.CHAT : ChatMessageType.ACTION_BAR, new TextComponent(defaultConfig.getBoolean("settings.send-toggle-message-chat") ? "§5[AutoSwitch] §r"+ (defaultConfig.getBoolean("users."+player.getUniqueId().toString()+".enabled-switchback") ? defaultConfig.getString("strings.enabled-switchback") : defaultConfig.getString("strings.disabled-switchback")) : (defaultConfig.getBoolean("users."+player.getUniqueId().toString()+".enabled-switchback") ? defaultConfig.getString("strings.enabled-switchback") : defaultConfig.getString("strings.disabled-switchback"))));

      try{
        defaultConfig.save(this.fileConfig);
      }
      catch (Exception e)
      {
        getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin("AutoSwitch"));
      }
    }

    return true;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player eventPlayer = (Player) event.getPlayer();

    if (!(eventPlayer instanceof Player)) {
      return;
    }

    if (!defaultConfig.getBoolean("users."+eventPlayer.getUniqueId().toString()+".enabled-autoswitch"))
    {
      return;
    }

    if (event.getClickedBlock() == null)
    {
      return;
    }

    if (event.getAction() != Action.LEFT_CLICK_BLOCK)
    {
      return;
    }

    if (eventPlayer.getGameMode() != GameMode.SURVIVAL && eventPlayer.getGameMode() != GameMode.ADVENTURE)
    {
      return;
    }

    Block clickedBlock = event.getClickedBlock();
    Material clickedBlockType = clickedBlock.getType();

    List<Float> breakSpeeds = new ArrayList<>();
    for (int i = 0; i < 9; i++)
    {
      ItemStack playerStack = eventPlayer.getInventory().getItem(i);
      if (playerStack != null && (!clickedBlock.getDrops(playerStack).isEmpty() || (clickedBlockType == (isOlderVersion ? Material.valueOf("MOB_SPAWNER") : Material.valueOf("SPAWNER"))) && isUsableTool(playerStack, clickedBlockType)))
      {
        String playerToolName = playerStack.getType().toString().substring(playerStack.getType().toString().indexOf("_")  + 1).toString();
        String playerToolType = playerStack.getType().toString().substring(0, Math.abs(playerStack.getType().toString().indexOf("_"))).toString();
        
        if (!playerToolName.equals("SHEARS") && !playerToolName.equals("PICKAXE") && !playerToolName.equals("SHOVEL") && !playerToolName.equals("SPADE") && !playerToolName.equals("AXE") && !playerToolName.equals("SWORD") && !playerToolName.equals("HOE"))
        {
          breakSpeeds.add(-1.0f);
          continue;
        }

        breakSpeeds.add(getDestroySpeed(clickedBlockType, playerStack) + (playerStack.getEnchantments().containsKey(Enchantment.DIG_SPEED) ? 3.0f : 0.0f));
        /*
          Boolean isntItemTool = isOlderVersion ? playerToolType != Material.DIAMOND_PICKAXE && playerToolType != Material.DIAMOND_AXE && playerToolType != Material.DIAMOND_SHOVEL && playerToolType != Material.DIAMOND_HOE && playerToolType != Material.DIAMOND_SWORD
          && playerToolType != Material.IRON_PICKAXE && playerToolType != Material.IRON_AXE && playerToolType != Material.IRON_SHOVEL && playerToolType != Material.IRON_HOE && playerToolType != Material.IRON_SWORD
          && playerToolType != Material.GOLDEN_PICKAXE && playerToolType != Material.GOLDEN_AXE && playerToolType != Material.GOLDEN_SHOVEL && playerToolType != Material.GOLDEN_HOE && playerToolType != Material.GOLDEN_SWORD
          && playerToolType != Material.STONE_PICKAXE && playerToolType != Material.STONE_AXE && playerToolType != Material.STONE_SHOVEL && playerToolType != Material.STONE_HOE && playerToolType != Material.STONE_SWORD
          && playerToolType != Material.WOODEN_PICKAXE && playerToolType != Material.WOODEN_AXE && playerToolType != Material.WOODEN_SHOVEL && playerToolType != Material.WOODEN_HOE && playerToolType != Material.WOODEN_SWORD
          && playerToolType != Material.SHEARS : playerToolType != Material.NETHERITE_PICKAXE && playerToolType != Material.NETHERITE_AXE && playerToolType != Material.NETHERITE_SHOVEL && playerToolType != Material.NETHERITE_HOE && playerToolType != Material.NETHERITE_SWORD
          && playerToolType != Material.DIAMOND_PICKAXE && playerToolType != Material.DIAMOND_AXE && playerToolType != Material.DIAMOND_SHOVEL && playerToolType != Material.DIAMOND_HOE && playerToolType != Material.DIAMOND_SWORD
          && playerToolType != Material.IRON_PICKAXE && playerToolType != Material.IRON_AXE && playerToolType != Material.IRON_SHOVEL && playerToolType != Material.IRON_HOE && playerToolType != Material.IRON_SWORD
          && playerToolType != Material.GOLDEN_PICKAXE && playerToolType != Material.GOLDEN_AXE && playerToolType != Material.GOLDEN_SHOVEL && playerToolType != Material.GOLDEN_HOE && playerToolType != Material.GOLDEN_SWORD
          && playerToolType != Material.STONE_PICKAXE && playerToolType != Material.STONE_AXE && playerToolType != Material.STONE_SHOVEL && playerToolType != Material.STONE_HOE && playerToolType != Material.STONE_SWORD
          && playerToolType != Material.WOODEN_PICKAXE && playerToolType != Material.WOODEN_AXE && playerToolType != Material.WOODEN_SHOVEL && playerToolType != Material.WOODEN_HOE && playerToolType != Material.WOODEN_SWORD
          && playerToolType != Material.SHEARS;
          if (isntItemTool)
          {
            breakSpeeds.add(-1.0f);
            continue;
          }
        */
      }
      else
      {
        breakSpeeds.add(-1.0f);
      }
    }

    if (!breakSpeeds.isEmpty())
    {
      Float maxValue = Collections.max(breakSpeeds);
      int highestNumber = breakSpeeds.indexOf(maxValue);

      if (maxValue != -1.0f && (isNeedSilkTouchTool(clickedBlock, eventPlayer.getInventory().getItem(highestNumber).getType()) || maxValue != 1.0f))
      {
        if (event.getPlayer().getInventory().getHeldItemSlot() != highestNumber)
        {
          if (slotMap.get(event.getPlayer()) == null)
          {
            slotMap.put(event.getPlayer(), event.getPlayer().getInventory().getHeldItemSlot());
          }
        }
        
        event.getPlayer().getInventory().setHeldItemSlot(highestNumber);
      }
    }
  }
  
  @EventHandler
  public void onPlayerAnimation(PlayerAnimationEvent event)
  {
    final Player eventPlayer = (Player) event.getPlayer();

    if (!(eventPlayer instanceof Player)) {
      return;
    }

    if (!defaultConfig.getBoolean("users."+eventPlayer.getUniqueId().toString()+".enabled-autoswitch"))
    {
      return;
    }

    if (!defaultConfig.getBoolean("users."+eventPlayer.getUniqueId().toString()+".enabled-switchback"))
    {
      return;
    }

    if (slotMap.get(eventPlayer) == null)
    {
      return;
    }

    BukkitTask task = taskMap.get(eventPlayer);
    if (task != null && getServer().getScheduler().isQueued(task.getTaskId()))
    {
      getServer().getScheduler().cancelTask(task.getTaskId());
    }

    task = getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
      @Override
      public void run() {
        eventPlayer.getInventory().setHeldItemSlot(slotMap.get(eventPlayer));
        slotMap.remove(eventPlayer);
      }
    }, 4);

    if (taskMap.get(eventPlayer) == null)
    {
      taskMap.put(eventPlayer, task);
    }
    else
    {
      taskMap.replace(eventPlayer, task);
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event)
  {
    final Player eventPlayer = (Player) event.getPlayer();

    if (!(eventPlayer instanceof Player)) {
      return;
    }

    if (!defaultConfig.getBoolean("users."+eventPlayer.getUniqueId().toString()+".enabled-autoswitch"))
    {
      return;
    }

    if (!defaultConfig.getBoolean("users."+eventPlayer.getUniqueId().toString()+".enabled-switchback"))
    {
      return;
    }

    if (slotMap.get(eventPlayer) == null)
    {
      return;
    }

    BukkitTask task = taskMap.get(eventPlayer);
    if (task != null && getServer().getScheduler().isQueued(task.getTaskId()))
    {
      getServer().getScheduler().cancelTask(task.getTaskId());
    }

    task = getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
      @Override
      public void run() {
        eventPlayer.getInventory().setHeldItemSlot(slotMap.get(eventPlayer));
        slotMap.remove(eventPlayer);
      }
    }, 10);

    if (taskMap.get(eventPlayer) == null)
    {
      taskMap.put(eventPlayer, task);
    }
    else
    {
      taskMap.replace(eventPlayer, task);
    }
  }

  public boolean isNeedSilkTouchTool(Block block, Material toolData)
  {
    ItemStack defItem = new ItemStack(toolData);
    ItemStack silkItem = new ItemStack(toolData);
    silkItem.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
    if (block.getDrops(defItem).isEmpty() && !block.getDrops(silkItem).isEmpty())
    {
      return true;
    }

    return false;
  }

  public Float getDestroySpeed(Material material, ItemStack itemStack)
  {
    /* Without Reflection (im so smart 😎)
      net.minecraft.server.v1_16_R3.Block nmsBlock = org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers.getBlock(material);
      net.minecraft.server.v1_16_R3.IBlockData nmsClickedBlockData = nmsBlock.getBlockData();
      net.minecraft.server.v1_16_R3.ItemStack nmsPlayerStack = org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack.asNMSCopy(itemStack);
      net.minecraft.server.v1_16_R3.Item nmsIteMStack = nmsPlayerStack.getItem();
      return nmsIteMStack.getDestroySpeed(nmsPlayerStack, nmsClickedBlockData);
    */

    try
    {
      //version
      String version = getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
      //nmsBlock
      Class<?> magicNumberClass = Class.forName("org.bukkit.craftbukkit." + version + ".util.CraftMagicNumbers");
      Method getBlockMethod = magicNumberClass.getDeclaredMethod("getBlock", Material.class);
      Object nmsBlock = getBlockMethod.invoke(null, material);
      //nmsClickedBlockData
      Method getBlockDataMethod = nmsBlock.getClass().getMethod("getBlockData");
      Object nmsClickedBlockData = getBlockDataMethod.invoke(nmsBlock);
      //nmsPlayerStack
      Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
      Method asNMSCopyMethod = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class);
      Object nmsPlayerStack = asNMSCopyMethod.invoke(null, itemStack);
      //nmsItemStack
      Method getItemMethod = nmsPlayerStack.getClass().getDeclaredMethod("getItem");
      Object nmsItem = getItemMethod.invoke(nmsPlayerStack);
      Class<?> nmsItemClass = nmsItem.getClass();
      if (!nmsItemClass.getCanonicalName().split("\\.")[nmsItemClass.getCanonicalName().split("\\.").length-1].toLowerCase().equalsIgnoreCase("item")) {
          while(!nmsItemClass.getCanonicalName().split("\\.")[nmsItemClass.getCanonicalName().split("\\.").length-1].toLowerCase().equalsIgnoreCase("item")) {
            nmsItemClass = nmsItemClass.getSuperclass();
          }
      }

      //destroySpeed
      Method getDestroySpeedMethod = nmsItemClass.getMethod("getDestroySpeed", nmsPlayerStack.getClass(), nmsClickedBlockData.getClass().forName("net.minecraft.server."+version+".IBlockData"));
      Object getDestroySpeed = getDestroySpeedMethod.invoke(nmsItem, nmsPlayerStack, nmsClickedBlockData);
      Float destroySpeed = Float.parseFloat(getDestroySpeed.toString());

      return destroySpeed;
    }
    catch(Exception err)
    {
      getServer().getConsoleSender().sendMessage("§c ERROR: "+err.toString());
    }

    return 0.0f;
  }

  public boolean isUsableTool(ItemStack tool, Material blockType)
  {
    /* Without Reflection (im so smart 😎)
      net.minecraft.server.v1_16_R3.Block block = org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers.getBlock(blockType);
      if (block == null) {
        return false;
      }
    
      net.minecraft.server.v1_16_R3.IBlockData data = block.getBlockData();
      if (data.getMaterial().isReplaceable() || !data.isRequiresSpecialTool()) {
        return true;
      }
    
      return tool != null && tool.getType() != Material.AIR && org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers.getItem(tool.getType()).canDestroySpecialBlock(data);
    */
    try
    {
      //version
      String version = getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
      //nmsBlock
      Class<?> magicNumberClass = Class.forName("org.bukkit.craftbukkit." + version + ".util.CraftMagicNumbers");
      Method getBlockMethod = magicNumberClass.getDeclaredMethod("getBlock", Material.class);
      Object nmsBlock = getBlockMethod.invoke(null, blockType);
      if (nmsBlock == null)
      {
        return false;
      }

      //nmsBlockData
      Method getBlockDataMethod = nmsBlock.getClass().getMethod("getBlockData");
      Object nmsBlockData = getBlockDataMethod.invoke(nmsBlock);
      
      //nmsMaterial
      Method getMaterialMethod = nmsBlockData.getClass().getMethod("getMaterial");
      Object nmsMaterial = getMaterialMethod.invoke(nmsBlockData);
      //isReplaceable
      Method isReplaceableMethod = nmsMaterial.getClass().getDeclaredMethod("isReplaceable");
      Object isReplaceableObj = isReplaceableMethod.invoke(nmsMaterial);
      Boolean isReplaceable = Boolean.valueOf(isReplaceableObj.toString());
      if (isReplaceable) {
        return true;
      }

      //nmsItem
      Method getItemMethod = magicNumberClass.getDeclaredMethod("getItem", Material.class);
      Object nmsItem = getItemMethod.invoke(null, tool.getType());
      Class<?> nmsItemClass = nmsItem.getClass();
      if (!nmsItemClass.getCanonicalName().split("\\.")[nmsItemClass.getCanonicalName().split("\\.").length-1].toLowerCase().equalsIgnoreCase("item")) {
          while(!nmsItemClass.getCanonicalName().split("\\.")[nmsItemClass.getCanonicalName().split("\\.").length-1].toLowerCase().equalsIgnoreCase("item")) {
            nmsItemClass = nmsItemClass.getSuperclass();
          }
      }

      //canDestroySpecialBlock
      Method canDestroySpecialBlockMethod = nmsItemClass.getDeclaredMethod("canDestroySpecialBlock", nmsBlockData.getClass().forName("net.minecraft.server."+version+".IBlockData"));
      Object canDestroySpecialBlockObj = canDestroySpecialBlockMethod.invoke(nmsItem, nmsBlockData);
      Boolean canDestroySpecialBlock = Boolean.valueOf(canDestroySpecialBlockObj.toString());

      return tool != null && tool.getType() != Material.AIR && canDestroySpecialBlock;
    }
    catch(Exception err)
    {
      getServer().getConsoleSender().sendMessage("§c ERROR: "+err.toString());
    }

    return false;
  }

  public void checkDefaultConfig()
	{
		String[] lists = { 
      "settings.send-toggle-message-chat",
      "strings.enabled-autoswitch",
      "strings.disabled-autoswitch",
      "strings.enabled-switchback",
      "strings.disabled-switchback",
      "strings.reload",
      "strings.help",
      "strings.accesdenied"
    };

    for (int i = 0; i < lists.length; i++)
    {
      if(defaultConfig.getString(lists[i]) == null)
      {
        getServer().getConsoleSender().sendMessage("§5[AutoSwitch] §eCorrupt config detected but could not fixed.");
        createDefaultConfig();
      }
    }
	}

	public void createDefaultConfig()
	{
		try
		{
			if(fileConfig.exists())
			{
        fileConfig.delete();
			}

      defaultConfig.set("settings.send-toggle-message-chat", false);
      defaultConfig.set("strings.enabled-autoswitch", "§fAuto Switch: §aEnabled");
      defaultConfig.set("strings.disabled-autoswitch", "§fAuto Switch: §cDisabled");
      defaultConfig.set("strings.enabled-switchback", "§fSwitch Back: §aEnabled");
      defaultConfig.set("strings.disabled-switchback", "§fSwitch Back: §cDisabled");
      defaultConfig.set("strings.reload", "§7Config files succesfully reloaded.");
      defaultConfig.set("strings.help", "§7Type '{0}' to check help page.");
      defaultConfig.set("strings.accesdenied", "§7You don't have permission to use this command!");

			defaultConfig.save(this.fileConfig);
		}
		catch (Exception e)
		{
			getServer().getConsoleSender().sendMessage("§5[AutoSwitch] §4Corrupt config detected but could not fixed.");
			Plugin plugin = getServer().getPluginManager().getPlugin("AutoSwitch");
			getServer().getPluginManager().disablePlugin(plugin);
		}
	}
}