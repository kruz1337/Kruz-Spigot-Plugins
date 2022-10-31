package org.requestx.bspawners;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;

/*
	TESTED IN (1.19, 1.18, 1.17), 1.16, 1.15, 1.14, 1.13, 1.12, 1.11, 1.10, 1.9
*/

public class Main extends JavaPlugin implements Listener
{
	File fileConfig = new File(this.getDataFolder(), "config.yml");
	FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(fileConfig);
	
	String clientVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].replace("v", "").replace("R", "").replace("_", ".").trim().toString();
	boolean isOlderVersion = (clientVersion.contains("1.8") || clientVersion.contains("1.9") || clientVersion.contains("1.10") || clientVersion.contains("1.11") || clientVersion.contains("1.12")); 
	boolean isNewerVersion = (clientVersion.contains("1.17") || clientVersion.contains("1.18") || clientVersion.contains("1.19")); 

	public void onEnable()
	{
		if(fileConfig.length() == 0)
		{
			createDefaultConfig();
		}
		else
		{
			checkDefaultconfig();
		}

		defaultConfig.options().copyDefaults(false);
		
		String logMsg = "§a[BSpawners] Has been enabled.\n" 
		+ "§5> §rCreated by §8github.com/kruz1337";
		
		getServer().getConsoleSender().sendMessage(logMsg);

		Bukkit.getPluginManager().registerEvents(this, this);
	}

	public void onDisable()
	{
		getServer().getConsoleSender().sendMessage("§c[BSpawners] has been disabled.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		Player player = (Player) sender;
		if (!(sender instanceof Player))
		{
			return false;
		}

		if(command.getName().equals("bspawners"))
		{
			if(args.length == 0 || (!args[0].equals("toggle") && !args[0].equals("help") && !args[0].equals("reload")))
			{
			  player.sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.help").replace("{0}", "/bspawners help"));
			  return true;
			}
			else
			{
				if(args[0].equals("toggle"))
				{
					defaultConfig.set("settings.enable-spawner-break", !defaultConfig.getBoolean("settings.enable-spawner-break"));
					
					if (defaultConfig.getBoolean("settings.enable-spawner-break"))
					{
						player.sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.bspawners-enabled"));
					}
					else 
					{
						player.sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.bspawners-disabled"));
					}

					try{
						defaultConfig.save(this.fileConfig);
					}
					catch (Exception e)
					{
						getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin("BreakableSpawners"));
					}
				}
				else if(args[0].equals("help"))
				{
					String helpString = "§5[BSpawners]\n"
						+ "§5> §rCreated by §8github.§kcom§r§8/kruz1337\n" 
						+ "§5> §r/bspawners toggle - §9Enable/Disable's Breakable Spawners\n" 
						+ "§5> §r/bspawners help - §9Show's Help Page\n"
						+ "§5> §r/bspawners reload - §9Reload's Config\n";
					
					player.sendMessage(helpString);
				}
				else if(args[0].equals("reload"))
				{
					if(player.isOp())
					{
						defaultConfig = YamlConfiguration.loadConfiguration(fileConfig);
						checkDefaultconfig();
						player.sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.bspawners-reload"));
					}
					else
					{
						player.sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.accesdenied"));
					}
				}		
			}
		}
		else if(command.getName().equals("toggle"))
		{
			defaultConfig.set("settings.enable-spawner-break", !defaultConfig.getBoolean("settings.enable-spawner-break"));

			if (defaultConfig.getBoolean("enable-spawner-break") == true)
			{
				player.sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.bspawners-enabled"));
			}
			else 
			{
				player.sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.bspawners-disabled"));
			}
						
			try{
				defaultConfig.save(this.fileConfig);
			}
			catch (Exception e)
			{
				getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin("BreakableSpawners"));
			}
		}
		else if(command.getName().equals("help"))
		{
            String helpString = "§5[BSpawners]\n"
                + "§5> §rCreated by §8github.§kcom§r§8/kruz1337\n" 
                + "§5> §r/bspawners toggle - §9Enables/Disables Breakable Spawners\n" 
                + "§5> §r/bspawners help - §9Show's Help Page\n"
                + "§5> §r/bspawners reload - §9Reload's Config\n";
			
			player.sendMessage(helpString);
		}
		else if(command.getName().equals("reload"))
		{
			if(player.isOp())
			{
				defaultConfig = YamlConfiguration.loadConfiguration(fileConfig);
				checkDefaultconfig();

				player.sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.bspawners-reload"));
			}
			else
			{
				player.sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.accesdenied"));
			}
		}
		
		return true;
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event)
	{
		if (!defaultConfig.getBoolean("settings.enable-spawner-break"))
		{
			return;
		}

		Player player = (Player) event.getPlayer();
		ItemStack playerTool = isOlderVersion ? player.getInventory().getItemInHand() : player.getInventory().getItemInMainHand();
		Material blockType = event.getBlock().getType();

		if (blockType != getSpawnerMaterial())
		{
			return;
		}

		String playerToolName = playerTool.getType().toString().substring(Math.abs(playerTool.getType().toString().indexOf("_")) + 1).toString();
		String playerToolType = playerTool.getType().toString().substring(0, Math.abs(playerTool.getType().toString().indexOf("_"))).toString();

		if((!defaultConfig.getBoolean("settings.enable-in-creative") && player.getGameMode() == GameMode.CREATIVE)
			||	(!defaultConfig.getBoolean("settings.enable-in-survival") && player.getGameMode() == GameMode.SURVIVAL)
			||	(!defaultConfig.getBoolean("settings.enable-in-adventure") && player.getGameMode() == GameMode.ADVENTURE))
		{
			return;
		}

		if ((!defaultConfig.getBoolean("settings.enable-tool-pickaxe") && playerToolName.equals("PICKAXE"))
			|| (!defaultConfig.getBoolean("settings.enable-tool-shovel") && playerToolName.equals("SHOVEL"))
			|| (!defaultConfig.getBoolean("settings.enable-tool-axe") && playerToolName.equals("AXE"))
			|| (!defaultConfig.getBoolean("settings.enable-tool-sword") && playerToolName.equals("SWORD"))
			|| (!defaultConfig.getBoolean("settings.enable-tool-hoe") && playerToolName.equals("HOE"))
		)
		{
			return;
		}
		else if (!playerToolName.equals("PICKAXE") && !playerToolName.equals("SHOVEL") && !playerToolName.equals("AXE") && !playerToolName.equals("SWORD") && !playerToolName.equals("HOE"))
		{
			return;
		}

		if ((!defaultConfig.getBoolean("settings.enable-type-wooden") && playerToolType.equals("WOODEN"))
			|| (!defaultConfig.getBoolean("settings.enable-type-iron") && playerToolType.equals("IRON"))
			|| (!defaultConfig.getBoolean("settings.enable-type-diamond") && playerToolType.equals("DIAMOND"))
			|| (!defaultConfig.getBoolean("settings.enable-type-golden") && playerToolType.equals("GOLDEN"))
			|| (!defaultConfig.getBoolean("settings.enable-type-netherite") && playerToolType.equals("NETHERITE")))
		{
			return;
		}

		if (defaultConfig.getBoolean("settings.enable-with-silktouch") && !playerTool.getEnchantments().containsKey(Enchantment.SILK_TOUCH))
		{
			if (defaultConfig.getBoolean("settings.enable-notify-silktouch"))
			{
				player.sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.silktouch-notify"));
			}
			return;
		}
		
		ItemStack spawnerItemStack = new ItemStack(getSpawnerMaterial(), 1);
		CreatureSpawner breakedSpawner = (CreatureSpawner) event.getBlock().getState();
		EntityType spawnerEntity = breakedSpawner.getSpawnedType();
		String mobName = getItemName(spawnerEntity.toString().replace('_', ' ')).replace("ı", "i");
		BlockStateMeta spawnerState = (BlockStateMeta) spawnerItemStack.getItemMeta();
		CreatureSpawner spawnerCreature = (CreatureSpawner) spawnerState.getBlockState();

		spawnerCreature.setDelay(breakedSpawner.getDelay());
		if (!isOlderVersion)
		{
			spawnerCreature.setMaxNearbyEntities(breakedSpawner.getMaxNearbyEntities());
			spawnerCreature.setMaxSpawnDelay(breakedSpawner.getMaxSpawnDelay());
			spawnerCreature.setMinSpawnDelay(breakedSpawner.getMinSpawnDelay());
			spawnerCreature.setRequiredPlayerRange(breakedSpawner.getRequiredPlayerRange());
			spawnerCreature.setSpawnCount(breakedSpawner.getSpawnCount());
			spawnerCreature.setSpawnRange(breakedSpawner.getSpawnRange());
		}

		spawnerCreature.setSpawnedType(spawnerEntity);
		spawnerState.setBlockState(spawnerCreature);
		spawnerState.setDisplayName("§r§b" + "Mob Spawner §7["+ mobName +"]");
		spawnerItemStack.setItemMeta(spawnerState);

		changeMaxStackSize(spawnerItemStack.getType(), 1);
		event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), spawnerItemStack);
	
		if (defaultConfig.getBoolean("settings.enable-break-message"))
		{
			player.sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.break-message").replace("{0}", mobName));
		}

		if (defaultConfig.getBoolean("settings.enable-break-detail-message") && !isOlderVersion)
		{
			String spawnerDetailMsg = "null";

			spawnerDetailMsg = defaultConfig.getString("strings.break-detail-message")
			.replace("{0}", mobName)
			.replace("{1}", Integer.toString(breakedSpawner.getMaxNearbyEntities()))
			.replace("{2}", Integer.toString(breakedSpawner.getMaxSpawnDelay()))
			.replace("{3}", Integer.toString(breakedSpawner.getMaxSpawnDelay()))
			.replace("{4}", Integer.toString(breakedSpawner.getRequiredPlayerRange()))
			.replace("{5}", Integer.toString(breakedSpawner.getSpawnCount()))
			.replace("{6}", Integer.toString(breakedSpawner.getSpawnRange()));

			player.sendMessage("§5[BSpawners] " + spawnerDetailMsg);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Player player = (Player) event.getPlayer();
		if (player == null || !(player instanceof Player)) {
		  return;
		}

		if (defaultConfig.getBoolean("settings.enable-silktouch-notify"))
		{
			event.getPlayer().sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.silktouch-notify"));
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (!defaultConfig.getBoolean("settings.enable-spawner-break"))
		{
			return;
		}

		ItemStack placedItemStack = event.getItemInHand();
        Block placedBlock = event.getBlockPlaced();

		if(placedBlock.getType() != getSpawnerMaterial())
		{
			return;
		}

		BlockStateMeta blockStateMeta = (BlockStateMeta) placedItemStack.getItemMeta();
		CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
		CreatureSpawner blockCreatureSpawner = (CreatureSpawner) placedBlock.getState();
		blockCreatureSpawner.setSpawnedType(creatureSpawner.getSpawnedType());
		blockCreatureSpawner.update();

		String mobName = getItemName(creatureSpawner.getSpawnedType().toString().replace('_', ' ')).replace("ı", "i");

		if (defaultConfig.getBoolean("settings.enable-place-message"))
		{
			event.getPlayer().sendMessage("§5[BSpawners] " + defaultConfig.getString("strings.place-message").replace("{0}", mobName));
		}
	}

	public Material getSpawnerMaterial()
	{
		return isOlderVersion ? Material.valueOf("MOB_SPAWNER") : Material.valueOf("SPAWNER");
	}

	public boolean changeMaxStackSize(Material material, int size) {
		if (material == null)
		{
			return true;
		}

        if (material.getMaxStackSize() == size) {
            return true;
        }
  
		try {
			final String packageVersion = this.getServer().getClass().getPackage().getName().split("\\.")[3];
			final Class<?> magicClass = Class.forName("org.bukkit.craftbukkit." + packageVersion + ".util.CraftMagicNumbers");
			final Method method = magicClass.getDeclaredMethod("getItem", Material.class);
			final Object item = method.invoke(null, material);
			final Class<?> itemClass = Class.forName("net.minecraft.server." + packageVersion + ".Item");
			final Field field = itemClass.getDeclaredField("OverStackSize");

			field.setAccessible(true);
			field.setInt(item, size);

			final Field mf = Material.class.getDeclaredField("maxStack");
			mf.setAccessible(true);
			mf.setInt(material, size);
			return true;
		}
		catch (Exception ex) {
			getServer().getConsoleSender().sendMessage("§c[BSpawners] Failed to change stack size!");
			return false;
		}
    }

	public void checkDefaultconfig()
	{
		String[] lists = 
		{ 
			"settings.enable-spawner-break",
			"settings.enable-with-silktouch",
			"settings.enable-silktouch-notify",
			"settings.enable-in-creative",
			"settings.enable-in-survival",
			"settings.enable-in-adventure",
			"settings.enable-tool-pickaxe",
			"settings.enable-tool-shovel",
			"settings.enable-tool-axe",
			"settings.enable-tool-sword",
			"settings.enable-tool-hoe",
			"settings.enable-type-wooden",
			"settings.enable-type-iron",
			"settings.enable-type-diamond",
			"settings.enable-type-golden",
			"settings.enable-type-netherite",
			"settings.enable-break-message",
			"settings.enable-break-detail-message",
			"settings.enable-place-message",
			"strings.bspawners-enabled",
			"strings.bspawners-disabled",
			"strings.bspawners-reload",
			"strings.help",
			"strings.accesdenied",
			"strings.break-message",
			"strings.break-detail-message",
			"strings.place-message",
			"strings.silktouch-notify"
		};

		if (isOlderVersion)
		{
			List<String> list = new ArrayList<String>(Arrays.asList(lists));
			list.remove("settings.enable-break-detail-message");
			list.remove("strings.break-detail-message");
			lists = list.toArray(new String[0]);
		}

		for (int i = 0; i < lists.length; i++)
		{
			if(defaultConfig.getString(lists[i]) == null)
			{
				createDefaultConfig();
			}
		}
	}
	
	public void createDefaultConfig()
	{
		try
		{
			defaultConfig.set("settings.enable-spawner-break", true);
			defaultConfig.set("settings.enable-with-silktouch", true);
			defaultConfig.set("settings.enable-silktouch-notify", true);
			defaultConfig.set("settings.enable-in-creative", false);
			defaultConfig.set("settings.enable-in-survival", true);
			defaultConfig.set("settings.enable-in-adventure", true);
			defaultConfig.set("settings.enable-tool-pickaxe", true);
			defaultConfig.set("settings.enable-tool-shovel", false);
			defaultConfig.set("settings.enable-tool-axe", false);
			defaultConfig.set("settings.enable-tool-sword", false);
			defaultConfig.set("settings.enable-tool-hoe", false);
			defaultConfig.set("settings.enable-type-wooden", false);
			defaultConfig.set("settings.enable-type-iron", false);
			defaultConfig.set("settings.enable-type-golden", false);
			defaultConfig.set("settings.enable-type-diamond", true);
			defaultConfig.set("settings.enable-type-netherite", true);
			defaultConfig.set("settings.enable-break-message", true);
			if (!isOlderVersion) defaultConfig.set("settings.enable-break-detail-message", true);
			defaultConfig.set("settings.enable-place-message", true);
			defaultConfig.set("strings.bspawners-enabled", "§7Breakable Spawners enabled!");
			defaultConfig.set("strings.bspawners-disabled", "§7Breakable Spawners disabled!");
			defaultConfig.set("strings.bspawners-reload", "§7Config files succesfully reloaded.");
			defaultConfig.set("strings.help", "§7Type '{0}' to check help page.");
			defaultConfig.set("strings.accesdenied", "§7You don't have permission to use this command!");
			defaultConfig.set("strings.break-message", "§7You breaked §b§n{0}§r §7spawner!");
			if (!isOlderVersion) defaultConfig.set("strings.break-detail-message", "§7§b§n{0}§7 spawner details: \n §5> §7Max Near Item: {1} §5> §7Max Spawn Delay: {2}\n §5> §7Min Spawn Delay: {3} §5> §7Required Player Range: {4} \n §5> §7Spawn Count: {5} §5> §7Spawn Range: {5}");
			defaultConfig.set("strings.place-message", "§7You placed §b§n{0}§r §7spawner!");
			defaultConfig.set("strings.silktouch-notify", "§7Hey, Silk Touch is enabled on this server, don't forget Silk Touch before break spawners :)");

			defaultConfig.save(this.fileConfig);
		}
		catch (Exception e)
		{
			getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin("BreakableSpawners"));
		}
	}

	private String getItemName(String string) {
		String result = "";
		string = string.toLowerCase();
		final String[] arr = string.split(" ");
		String[] array;
		for (int length = (array = arr).length, i = 0; i < length; ++i) {
		  final String s = array[i];
		  result = String.valueOf(result) + String.valueOf(s.charAt(0)).toUpperCase() + s.substring(1) + " ";
		}
		return result.substring(0, result.length() - 1);
	}
}