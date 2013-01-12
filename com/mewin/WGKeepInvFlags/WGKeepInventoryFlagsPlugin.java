/*
 * Copyright (C) 2013 mewin<mewin001@hotmail.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mewin.WGKeepInvFlags;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.mewin.util.ConfigMgr;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public class WGKeepInventoryFlagsPlugin extends JavaPlugin {
    public static final StateFlag KEEP_INVENTORY_FLAG = new StateFlag("keep-inventory", false);
    public static final StateFlag KEEP_LEVEL_FLAG = new StateFlag("keep-level", false);
    
    private WGCustomFlagsPlugin custPlugin;
    private WorldGuardPlugin wgPlugin;
    private WGKIFlagListener listener;
    private FileConfiguration invConf;
    
    @Override
    public void onEnable()
    {
        Plugin plug = getServer().getPluginManager().getPlugin("WGCustomFlags");
        
        if (plug == null || !(plug instanceof WGCustomFlagsPlugin) || !plug.isEnabled())
        {
            getLogger().warning("Could not load WorldGuard Custom Flags Plugin, disabling");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        else
        {
            custPlugin = (WGCustomFlagsPlugin) plug;
        }
        
        plug = getServer().getPluginManager().getPlugin("WorldGuard");
        
        if (plug == null || !(plug instanceof WorldGuardPlugin) || !plug.isEnabled())
        {
            getLogger().warning("Could not load WorldGuard Plugin, disabling");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        else
        {
            wgPlugin = (WorldGuardPlugin) plug;
        }
        
        custPlugin.addCustomFlag(KEEP_INVENTORY_FLAG);
        custPlugin.addCustomFlag(KEEP_LEVEL_FLAG);
        invConf = new YamlConfiguration();
        
        listener = new WGKIFlagListener(this, wgPlugin);
        
        getServer().getPluginManager().registerEvents(listener, this);
    }
    
    @Override
    public void onDisable()
    {
        saveInventories(listener.inventories);
    }
    
    public void saveInventories(Map<String, ItemStack[]> invs)
    {
        ConfigMgr mgr = new ConfigMgr(this, "inventories.yml", invConf);
        
        for(Entry<String, ItemStack[]> entry : invs.entrySet())
        {
            List<Map> list = isArrayToList(entry.getValue());
            String player = entry.getKey();
            
            invConf.set(player, list);
        }
        
        mgr.save();
    }
    
    public void loadInventories(Map<String, ItemStack[]> invs)
    {
        ConfigMgr mgr = new ConfigMgr(this, "inventories.yml", invConf);
        
        mgr.load(false);
        
        Map<String, Object> values = invConf.getValues(false);
        
        for(Entry<String, Object> value : values.entrySet())
        {
            if (value.getValue() instanceof List)
            {
                List<Map> list = (List<Map>) value.getValue();
                
                invs.put(value.getKey(), listToIsArray(list, 36));
            }
        }
    }
    
    private List<Map> isArrayToList(ItemStack[] isArray)
    {
        List list = new ArrayList<Map>();
        
        for(int i = 0; i < isArray.length; i++)
        {
            ItemStack is = isArray[i];
            
            if (is != null && is.getTypeId() != 0)
            {
                Map<String, Object> map = is.serialize();
                map.put("slot", i);
                
                list.add(map);
            }
        }
        
        return list;
    }
    
    private ItemStack[] listToIsArray(List<Map> list, int invSize)
    {
        ItemStack[] iss = new ItemStack[invSize];
        
        for(int i = 0; i < invSize; i++)
        {
            iss[i] = new ItemStack(0, 0);
        }
        
        for(Map map : list)
        {
            iss[(Integer) map.remove("slot")] = ItemStack.deserialize(map);
        }
        
        return iss;
    }
}
