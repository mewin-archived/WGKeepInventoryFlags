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

package com.mewin.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public class ConfigMgr {
    private Plugin plugin;
    private String confName;
    private File confFile;
    private FileConfiguration conf;
    
    public ConfigMgr(Plugin plugin, String confFile)
    {
        this(plugin, confFile, plugin.getConfig());
    }
    
    public ConfigMgr(Plugin plugin, String confFile, FileConfiguration conf)
    {
        this.plugin = plugin;
        this.confName = confFile;
        
        this.confFile = new File(plugin.getDataFolder(), this.confName);
        this.conf = conf;
    }
    
    public ConfigMgr(Plugin plugin)
    {
        this(plugin, "config.yml");
    }
    
    public void save()
    {
        try {
            if (!plugin.getDataFolder().exists())
            {
                if (!plugin.getDataFolder().mkdir())
                {
                    plugin.getLogger().log(Level.SEVERE, "Could not create plugin data folder.");
                    return;
                }
            }
            if (!confFile.exists())
            {
                if(!confFile.createNewFile())
                {
                    plugin.getLogger().log(Level.SEVERE, "Could not create config file.");
                    return;
                }
            }
            conf.save(confFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config file: ", ex);
        }
    }
    
    public void load(boolean saveDefaults)
    {
        if (!confFile.exists())
        {
            if (saveDefaults)
            {
                plugin.getLogger().log(Level.INFO, "Config file not found, writing default config.");
                save();
            }
        }
        else
        {
            try
            {
                conf.load(confFile);
            }
            catch(Exception ex)
            {
                plugin.getLogger().log(Level.SEVERE, "Could not load config file: ", ex);
            }
        }
    }
}
