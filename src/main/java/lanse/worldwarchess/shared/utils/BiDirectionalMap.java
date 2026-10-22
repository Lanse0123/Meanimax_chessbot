package lanse.worldwarchess.shared.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BiDirectionalMap<K, V> {
    private final Map<K, V> keyToValue;
    private final Map<V, K> valueToKey;

    public BiDirectionalMap() {
        keyToValue = new HashMap<>();
        valueToKey = new HashMap<>();
    }

    public void put(K key, V value) {
        V oldValue = keyToValue.get(key);
        K oldKey = valueToKey.get(value);
        if (oldValue != null) valueToKey.remove(oldValue);
        if (oldKey != null) keyToValue.remove(oldKey);

        keyToValue.put(key, value);
        valueToKey.put(value, key);
    }

    public V getValue(K key) {
        return keyToValue.get(key);
    }

    public K getKey(V value) {
        return valueToKey.get(value);
    }

    public void removeByKey(K key) {
        V value = keyToValue.remove(key);
        if (value != null) valueToKey.remove(value);
    }

    public void removeByValue(V value) {
        K key = valueToKey.remove(value);
        if (key != null) keyToValue.remove(key);
    }

    public Map<K, V> asKeyToValueMap() {
        return Collections.unmodifiableMap(keyToValue);
    }

    public Map<V, K> asValueToKeyMap() {
        return Collections.unmodifiableMap(valueToKey);
    }

    public void clear() {
        keyToValue.clear();
        valueToKey.clear();
    }

    public boolean containsKey(K key) {
        return keyToValue.containsKey(key);
    }

    public boolean containsValue(V value) {
        return valueToKey.containsKey(value);
    }

    public int size() {
        return keyToValue.size();
    }
}