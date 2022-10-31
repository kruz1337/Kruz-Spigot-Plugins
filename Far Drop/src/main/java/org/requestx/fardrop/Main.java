package org.requestx.fardrop;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
  File fileConfig = new File(this.getDataFolder(), "config.yml");
	FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(fileConfig);
  
  String clientVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].replace("v", "").replace("R", "").replace("_", ".").trim().toString();
	boolean isOlderVersion = (clientVersion.contains("1.8") || clientVersion.contains("1.9") || clientVersion.contains("1.10") || clientVersion.contains("1.11") || clientVersion.contains("1.12")); 
  
  public void onEnable() {
    if(fileConfig.length() == 0)
		{
			createDefaultConfig();
		}
		else
		{
			checkDefaultconfig();
		}

		defaultConfig.options().copyDefaults(false);
    getServer().getConsoleSender().sendMessage("§a[FarDrop] has been enabled.");
  }

  public void onDisable() {
    getServer().getConsoleSender().sendMessage("§c[FarDrop] has been disabled.");
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      return false;
    }
    Player player = (Player) sender;

    if (command.getName().equals("fardrop")) {
      if(args.length == 0 || (!args[0].equals("help") && !args[0].equals("reload")))
			{
			  player.sendMessage("§5[FarDrop] §r" + defaultConfig.getString("strings.help").replace("{0}", "/fardrop help"));
			  return true;
			}
      else if (args[0].equals("help"))
      {
        String helpString = "§5[FarDrop]\n§5> §rCreated by §8github.§kcom§r§8/kruz1337\n§5> §r/fardrop help - §9Show help page.\n§5> §r/fardrop reload - §9Reload's config.\n§5> §r/drop <User> <Amount> - §9Drop's items to defined player.\n§5> §r/dropxp <User> <Amount> - §9Drop's xp to defined player.";
        player.sendMessage(helpString);
        return true;
      }
      else if (args[0].equals("reload"))
      {
        if(player.isOp())
        {
          defaultConfig = YamlConfiguration.loadConfiguration(fileConfig);
          checkDefaultconfig();
          player.sendMessage("§5[FarDrop] §r" + defaultConfig.getString("strings.reload"));
        }
        else
        {
          player.sendMessage("§5[FarDrop] §r" + defaultConfig.getString("strings.accesdenied"));
        }
			  return true;
      }
    }
    else if (command.getName().equals("drop")) {
      if (!defaultConfig.getBoolean("settings.enable-drop"))
      {
        return true;
      }

      if (defaultConfig.getBoolean("settings.enable-just-administrator") && !player.isOp())
      {
        player.sendMessage("§5[FarDrop] §r"+ defaultConfig.getString("strings.accesdenied"));
        return true;
      }

      if (args.length == 0) {
        player.sendMessage("§5[FarDrop] §r"+ defaultConfig.getString("strings.chooseplayer"));
        return true;
      }

      ItemStack handItem = isOlderVersion ? player.getInventory().getItemInHand() : player.getInventory().getItemInMainHand();
      if (handItem == null || handItem.getType() == Material.AIR) {
        player.sendMessage("§5[FarDrop] §r"+ defaultConfig.getString("strings.getvaliditem"));
        return true;
      }

      Player dropPlyr = Bukkit.getPlayer(args[0]);
      if (dropPlyr == null) {
        player.sendMessage("§5[FarDrop] §r"+ defaultConfig.getString("strings.invalidplayer"));
        return true;
      }

      Integer itemNumber = args.length == 2 ? (args[1] == null ? 1 : Integer.parseInt(args[1]) > 100 ? 100 : Integer.parseInt(args[1])) : 1;
      if (itemNumber > handItem.getAmount()) {
        player.sendMessage("§5[FarDrop] §r"+ defaultConfig.getString("strings.donthave"));
        return true;
      }

      boolean isInventoryFull = dropPlyr.getInventory().firstEmpty() == -1;
      if (defaultConfig.getBoolean("settings.check-inventory-size") && isInventoryFull)
      {
        player.sendMessage("§5[FarDrop] §r"+ defaultConfig.getString("strings.inventoryfull").replace("{0}", dropPlyr.getName()));
        return true;
      }

      World world = dropPlyr.getWorld();
      ItemStack chandItem = new ItemStack(handItem.getType());
      chandItem.setItemMeta(handItem.getItemMeta());

      String handItemName = this.getItemName(handItem.getType().name().replace("_", " ")).replace("ı", "i");
      chandItem.setAmount(itemNumber);

      if (defaultConfig.getBoolean("settings.enable-natural-drop") || isInventoryFull)
      {
        world.dropItem(dropPlyr.getLocation(), chandItem);
      }
      else
      {
        dropPlyr.getInventory().addItem(chandItem);
      }

      handItem.setAmount(handItem.getAmount() - chandItem.getAmount());

      String dropMsg = "§5[FarDrop] §r"+defaultConfig.getString("strings.drop-message");
      dropPlyr.sendMessage(dropMsg.replace("{0}", player.getName()).replace("{1}", Integer.toString(chandItem.getAmount())).replace("{2}", handItemName));
    }
    else if (command.getName().equals("dropxp")) {
      if (!defaultConfig.getBoolean("settings.enable-dropxp"))
      {
        return true;
      }

      if (defaultConfig.getBoolean("settings.enable-just-administrator") && !player.isOp())
      {
        player.sendMessage("§5[FarDrop] §r"+ defaultConfig.getString("strings.accesdenied"));
        return true;
      }

      if (args.length == 0) {
        player.sendMessage("§5[FarDrop] §r"+ defaultConfig.getString("strings.chooseplayer"));
        return true;
      }

      Player dropPlyr = Bukkit.getPlayer(args[0]);
      if (dropPlyr == null) {
        player.sendMessage("§5[FarDrop] §r"+ defaultConfig.getString("strings.invalidplayer"));
        return true;
      }

      int xpNumber = args.length == 2 ? (args[1] == null ? 1 : Integer.parseInt(args[1])) : 1;

      if (xpNumber > player.getTotalExperience()) {
        player.sendMessage("§5[FarDrop] §r"+ defaultConfig.getString("strings.donthavexp"));
        return true;
      }

      World world = dropPlyr.getWorld();

      if (defaultConfig.getBoolean("settings.enable-natural-dropxp"))
      {
        world.spawn(dropPlyr.getLocation(), ExperienceOrb.class).setExperience(xpNumber);
      }
      else
      {
        dropPlyr.giveExp(xpNumber);
      }

      player.giveExp(0-xpNumber);

      String dropMsg = "§5[FarDrop] §r"+defaultConfig.getString("strings.dropxp-message");
      dropPlyr.sendMessage(dropMsg.replace("{0}", player.getName()).replace("{1}", Integer.toString(xpNumber)));
    }
    else if (command.getName().equals("help")) {
      String helpString = "§5[FarDrop]\n§5> §rCreated by §8github.§kcom§r§8/kruz1337\n§5> §r/fardrop help - §9Show help page.\n§5> §r/fardrop reload - §9Reload's config.\n§5> §r/drop <User> <Amount> - §9Drop's items to defined player.\n§5> §r/dropxp <User> <Amount> - §9Drop's xp to defined player.";
      player.sendMessage(helpString);
      return true;
    }
    else if (command.getName().equals("reload")) {
      if(player.isOp())
      {
        defaultConfig = YamlConfiguration.loadConfiguration(fileConfig);
        checkDefaultconfig();
        player.sendMessage("§5[FarDrop] §r" + defaultConfig.getString("strings.reload"));
      }
      else
      {
        player.sendMessage("§5[FarDrop] §r" + defaultConfig.getString("strings.accesdenied"));
      }
      return true;
    }

    return true;
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

  public void checkDefaultconfig()
	{
		String[] lists = 
		{ 
			"settings.enable-drop",
      "settings.enable-dropxp",
			"settings.enable-natural-drop",
			"settings.enable-natural-dropxp",
      "settings.check-inventory-size",
      "settings.enable-just-administrator",
			"strings.drop-message",
			"strings.dropxp-message",
			"strings.help", 
			"strings.reload",
			"strings.accesdenied",
			"strings.chooseplayer",
			"strings.getvaliditem",
			"strings.invalidplayer",
			"strings.donthave",
      "strings.donthavexp",
      "strings.inventoryfull"
		};

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
			defaultConfig.set("settings.enable-drop", true);
			defaultConfig.set("settings.enable-dropxp", true);
			defaultConfig.set("settings.enable-natural-drop", true);
			defaultConfig.set("settings.enable-natural-dropxp", false);
			defaultConfig.set("settings.check-inventory-size", true);
			defaultConfig.set("settings.enable-just-administrator", false);
			defaultConfig.set("strings.drop-message", "§f§l{0} §rdropped, §n§o{2} (x{1}).");
			defaultConfig.set("strings.dropxp-message", "§f§l{0} §rdropped, §n§o{1} exp.");
			defaultConfig.set("strings.help", "§7Type '{0}' to check help page.");
			defaultConfig.set("strings.reload", "§7Config files succesfully reloaded.");
			defaultConfig.set("strings.accesdenied", "§7You don't have permission to use this command!");
			defaultConfig.set("strings.chooseplayer", "§7Choose player to drop item!");
			defaultConfig.set("strings.getvaliditem", "§7Get valid item in hand!");
			defaultConfig.set("strings.invalidplayer", "§7Invalid player!");
			defaultConfig.set("strings.donthave", "§7You don't have that many items!");
      defaultConfig.set("strings.donthavexp", "§7You don't have that many xp!");
			defaultConfig.set("strings.inventoryfull", "§l{0}§r's inventory is full!");

			defaultConfig.save(this.fileConfig);
		}
		catch (Exception e)
		{
			getServer().getPluginManager().disablePlugin(this);
		}
	}
}