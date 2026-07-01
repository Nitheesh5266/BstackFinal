package com.bstackdemo.utils;

import java.util.HashMap;
import java.util.Map;

public final class ScenarioContext {
    private final Map<String, Object> data = new HashMap<>();

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public String getString(String key) {
        Object value = data.get(key);
        return value == null ? null : String.valueOf(value);
    }

    public int getInt(String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    public double getDouble(String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }
}
