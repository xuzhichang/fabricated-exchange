package com.skirlez.fabricatedexchange.util;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

import com.skirlez.fabricatedexchange.FabricatedExchange;

public class ConfigFile extends DataFile<Map<String, Object>> {
    ConfigFile(Type type, String name) {
        super(type, name);
    }

    public static enum Bool {
        SHOW_ITEM_EMC_ORIGIN,
        MAPPER_ENABLED
    }

    /* Fetch the config and compare to the default config to see if any keys
    are missing or any of them have mismatched value types. If true, set that 
    key to the default value and save */
    @Override
    public void fetch() {
        super.fetch();
        
        Map<String, Object> defaultValue = getDefaultValue();
        Iterator<String> iterator = defaultValue.keySet().iterator();
        boolean changed = false;
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!value.containsKey(key)) {
                FabricatedExchange.LOGGER.error("Config file is missing key " + key + "! Inserting it into the config with the default value.");
                value.put(key, defaultValue.get(key));
                if (!changed)
                    changed = true;
            }
            else if (!value.get(key).getClass().equals(defaultValue.get(key).getClass())) {
                FabricatedExchange.LOGGER.error("Key " + key + " has different value type than the default type! Reseting it to the default value.");
                value.put(key, defaultValue.get(key));
                if (!changed)
                    changed = true;
            }
        }
        if (changed)
            save();
    }
    
    public boolean getOption(Bool option) {
        String str;
        switch (option) {
            case SHOW_ITEM_EMC_ORIGIN:
                str = "showItemEmcOrigin";
                break;
            case MAPPER_ENABLED:
                str = "mapper_enabled";
                break;
            default:
                FabricatedExchange.LOGGER.error("Config file has enum without a matching string");
                return false;
        }
        return (boolean)value.get(str);
    }

}
