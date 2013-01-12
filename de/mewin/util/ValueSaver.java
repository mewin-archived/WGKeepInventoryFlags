/*
 * Copyright (C) 2012 mewin <mewin001@hotmail.de>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package de.mewin.util;

import de.mewin.util.savers.ItemStackSaver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 * @version 0.3 (for Java 6)
 */
public class ValueSaver
{
    private Object objToSave;
    private File file;
    private List<Field> fields;
    private Map<Class, Saver> savers;
    
    public ValueSaver(Object obj)
    {
        this(obj, null);
    }
    public ValueSaver(Object obj, File file)
    {
        this.objToSave = obj;
        this.file = file;
        savers = new HashMap<Class, Saver>();
        savers.put(ItemStack.class, new ItemStackSaver());
        
        findFields();
    }
    
    private void findFields()
    {
        Class cls = objToSave.getClass();
        
        fields = findFields(cls);
    }
    
    private List<Field> findFields(Class cls)
    {
        Class supCls = cls.getSuperclass();
        Class[] interfaces = cls.getInterfaces();
        
        ArrayList<Field> fs = new ArrayList<Field>();
        
        if (supCls != null)
        {
            fs.addAll(findFields(supCls));
        }
        for (Class iface : interfaces)
        {
                fs.addAll(findFields(iface));
        }
        
        for (Field f : cls.getDeclaredFields())
        {
            f.setAccessible(true);
            if (f.isAnnotationPresent(SavingValue.class))
            {
                fs.add(f);
            }
        }
        
        return fs;
    }
    
    public void registerSaver(Saver saver)
    {
        for (Class cls : saver.getClasses())
        {
            if (!savers.containsKey(cls))
            {
                savers.put(cls, saver);
            }
        }
    }
    
    public void save() throws IOException
    {
        FileWriter out = new FileWriter(file);
        Yaml yaml = new Yaml();
        
        if (!file.exists())
        {
            if (!file.createNewFile())
            {
                return;
            }
        }
        
        yaml.dump(getYaml(), out);
    }
    
    public Object getYaml()
    {
        List<Map> fieldList = new ArrayList<Map>();
        
        for (Field f : fields)
        {
            saveField(f, fieldList);
        }
        
        return fieldList;
    }
    
    private void saveField(Field f, List<Map> lst)
    {
        HashMap<String, Object> values = new HashMap<String, Object>();
        
        values.put("declaringClass", f.getDeclaringClass().getName());
        values.put("fieldName", f.getName());
        values.put("value", getFieldValue(f));
        
        lst.add(values);
    }
    
    private Entry<Class, Saver> getSaver(Object obj)
    {
        for (Entry<Class, Saver> ent : savers.entrySet())
        {
            if (ent.getKey().isInstance(obj))
            {
                return ent;
            }
        }
        return null;
    }
    
    private Object getValue(Object val)
    {
        Entry<Class, Saver> saver = getSaver(val);
        if (saver != null)
        {
            HashMap map = new HashMap();
            map.put("customSaverClass", saver.getKey().getName());
            map.put("value", saver.getValue().serialize(val));
            return map;
        }
        else if (val instanceof Integer
                || val instanceof Double
                || val instanceof Boolean
                || val instanceof Float
                || val instanceof Short
                || val instanceof Long
                || val instanceof String)
        {
            return val;
        }
        else if (val instanceof List)
        {
            List lst = (List) val;
            ArrayList<Object> newLst = new ArrayList<Object>();

            for (Object obj : lst)
            {
                newLst.add(getValue(obj));
            }

            return newLst;
        }
        else if (val instanceof Location)
        {
            return serializeLocation((Location) val);
        }
        else if (val instanceof Saveable)
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("saveableclass", val.getClass().getName());
            map.put("value", ((Saveable) val).serialize());
            return map;
        }
        else if (val instanceof ItemStack)
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("isItemStack", true);
            map.put("stack", ((ItemStack) val).serialize());
            return map;
        }
        else if (val instanceof Map)
        {
            HashMap saveMap = new HashMap();
            for (Entry ent : ((Map<?, ?>) val).entrySet())
            {
                saveMap.put(getValue(ent.getKey()), getValue(ent.getValue()));
            }
            return saveMap;
        }
        else if (val instanceof Object[])
        {
            HashMap saveMap = new HashMap();
            saveMap.put("isArray", true);
            saveMap.put("length", ((Object[]) val).length);
            saveMap.put("class", val.getClass().getComponentType().getName());
            List lst = new ArrayList();
            for (int i = 0; i < ((Object[]) val).length; i++)
            {
                Object obj = ((Object[])val)[i];
                if (obj != null)
                {
                    HashMap objMap = new HashMap();
                    objMap.put("pos", i);
                    objMap.put("value", getValue(obj));
                    lst.add(objMap);
                }
            }
            saveMap.put("values", lst);
            return saveMap;
        }
        else
        {
            return null;
        }
    }
    
    public static Map<String, Object> serializeLocation(Location loc)
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        
        map.put("world", loc.getWorld().getUID().toString());
        map.put("x", loc.getX());
        map.put("y", loc.getY());
        map.put("z", loc.getZ());
        map.put("yaw", (double) loc.getYaw());
        map.put("pitch", (double) loc.getPitch());
        
        return map;
    }
    
    public static Location deserializeLocation(Map<String, Object> map)
    {
        return new Location(Bukkit.getWorld(UUID.fromString((String) map.get("world"))), 
                (Double) map.get("x"), 
                (Double) map.get("y"), 
                (Double) map.get("z"), 
                Float.valueOf(((Double) map.get("yaw")) + ""), // that can't be correct
                Float.valueOf(((Double) map.get("pitch")) + ""));
    }
    
    private Object getFieldValue(Field f)
    {
        try
        {
            Object val = f.get(objToSave);
            
            return getValue(val);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
    
    public void load() throws FileNotFoundException
    {
        Yaml yaml = new Yaml();
        List<Map<String, Object>> lst;
        if (!file.exists())
        {
            try
            {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                return;
            }
        }
        lst = (List<Map<String, Object>>) yaml.load(new FileInputStream(file));
        
        for (Map<String, Object> map : lst)
        {
            loadMap(map);
        }
    }
    
    public void loadYaml(String yamlString)
    {
        Yaml yaml = new Yaml();
        List<Map<String, Object>> lst = (List<Map<String, Object>>) yaml.load(yamlString);
        
        for (Map<String, Object> map : lst)
        {
            loadMap(map);
        }
    }
    
    public void load(List<Map<String, Object>> lst)
    {        
        for (Map<String, Object> map : lst)
        {
            loadMap(map);
        }
    }
    
    private void loadMap(Map<String, Object> map)
    {
        try
        {
            Class cls = Class.forName((String) map.get("declaringClass"));
            Field f = cls.getDeclaredField((String) map.get("fieldName"));
            
            f.setAccessible(true);
            Object val = map.get("value");
            
            if (val != null)
            {
                f.set(objToSave, loadValue(val));
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    private Object loadValue(Object val)
    {
        if (val instanceof Integer
            || val instanceof Double
            || val instanceof Boolean
            || val instanceof Float
            || val instanceof Short
            || val instanceof Long
            || val instanceof String)
        {
            return val;
        }
        else if (val instanceof List)
        {
            ArrayList lst = new ArrayList();
            
            for (Object obj : (List) val)
            {
                lst.add(loadValue(obj));
            }
            
            return lst;
        }
        else if (val instanceof Map)
        {
            Map map = (Map) val;
            
            if (map.containsKey("x")
                    && map.containsKey("y")
                    && map.containsKey("z")
                    && map.containsKey("world")
                    && map.containsKey("yaw")
                    && map.containsKey("pitch"))
            { //wir gehen davon aus, dass es eine Location ist
                return deserializeLocation(map);
            }
            else if (map.containsKey("saveableclass"))
            {
                try
                {
                    Class cls = Class.forName((String) map.get("saveableclass"));
                    Method m = cls.getDeclaredMethod("deserialize", Object.class);
                    Object value = map.get("value");
                    
                    return m.invoke(null, value);
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    return null;
                }
            }
            /*else if (map.containsKey("isItemStack") &&
                    (boolean) map.get("isItemStack"))
            {
                return ItemStack.deserialize((Map) map.get("stack"));
            }*/
            else if (map.containsKey("customSaverClass"))
            {
                try
                {
                    Class cls = Class.forName((String) map.get("customSaverClass"));
                    Saver saver = savers.get(cls);
                    
                    return saver.deserialize(map.get("value"));
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    return null;
                }
            }
            else if (map.containsKey("isArray") &&
                    (Boolean) map.get("isArray"))
            {
                Class cls;
                try
                {
                    cls = Class.forName((String) map.get("class"));
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    return null;
                }
                
                Object[] arr = (Object[]) Array.newInstance(cls, (Integer) map.get("length"));//new Object[(int) map.get("length")];
                for(Map<String, Object> m : (List<Map>) map.get("values"))
                {
                    arr[(Integer) m.get("pos")] = loadValue(m.get("value"));
                }
                return arr;
            }
            else
            {
                HashMap m = new HashMap();
                for (Entry ent : ((HashMap<?, ?>) val).entrySet())
                {
                    m.put(loadValue(ent.getKey()), loadValue(ent.getValue()));
                }
                return m;
            }
        }
        else
        {
            System.out.println("Incompatible class: " + val.getClass().getName());
            return null;
        }
    }
}