package de.mewin.util.savers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public class ItemStackSaver extends SingleClassSaver<ItemStack>
{
    @Override
    public Object serializeT(ItemStack t)
    {
        Map<String, Object> map = t.serialize();
        map.remove("meta");
        if (t.hasItemMeta())
        {
            map.put("mData", mDataToMap(t.getItemMeta()));
        }
        return map;
    }
    
    private Map<String, Object> mDataToMap(ItemMeta meta)
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        
        if (meta.hasDisplayName())
        {
            map.put("displayName", meta.getDisplayName().replaceAll(ChatColor.COLOR_CHAR + "", "&"));
        }
        if (meta.hasLore())
        {
            ArrayList<String> lst = new ArrayList<String>();
            for (String str : meta.getLore())
            {
                lst.add(str.replaceAll(ChatColor.COLOR_CHAR + "", "&"));
            }
            map.put("lore", lst);
        }
        if (meta.hasEnchants())
        {
            ArrayList<Map> eList = new ArrayList<Map>();
            for (Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet())
            {
                HashMap eMap = new HashMap();
                eMap.put("type", enchant.getKey().getName());
                eMap.put("level", enchant.getValue());
                eList.add(eMap);
            }
            if (eList.size() > 0)
            {
                map.put("enchantments", eList);
            }
        }
        
        return map;
    }
    
    private void giveStackMData(ItemStack stack, Map map)
    {
        ItemMeta meta = stack.getItemMeta();
        if (map.containsKey("displayName"))
        {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (String) map.get("displayName")));
        }
        if (map.containsKey("lore"))
        {
            ArrayList<String> lst = new ArrayList<String>();
            for (String str : (List<String>) map.get("lore"))
            {
                lst.add(ChatColor.translateAlternateColorCodes('&', str));
            }
            meta.setLore(lst);
        }
        if (map.containsKey("enchantments"))
        {
            for (Map m : (List<Map>) map.get("enchantments"))
            {
                meta.addEnchant(Enchantment.getByName((String) m.get("type")), (Integer) m.get("level"), true);
            }
        }
        stack.setItemMeta(meta);
    }

    @Override
    public ItemStack deserializeT(Object obj)
    {
        HashMap map = (HashMap) obj;
        ItemStack stack = ItemStack.deserialize(map);
        if (map.containsKey("mData"))
        {
            giveStackMData(stack, (Map) map.get("mData"));
        }
        return stack;
    }
    
}
