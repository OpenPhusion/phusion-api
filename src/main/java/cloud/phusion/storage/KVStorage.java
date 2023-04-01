package cloud.phusion.storage;

import cloud.phusion.Context;

/**
 * Key-value Storage.
 */
public interface KVStorage {

    void put(String key, Object value) throws Exception;
    void put(String key, Object value, long millisecondsToLive) throws Exception;
    void put(String key, Object value, Context ctx) throws Exception;
    void put(String key, Object value, long millisecondsToLive, Context ctx) throws Exception;

    Object get(String key) throws Exception;
    Object get(String key, Context ctx) throws Exception;

    boolean doesExist(String key) throws Exception;
    boolean doesExist(String key, Context ctx) throws Exception;

    void remove(String key) throws Exception;
    void remove(String key, Context ctx) throws Exception;

    boolean lock(String key) throws Exception;
    boolean lock(String key, long millisecondsToLive) throws Exception;
    void unlock(String key) throws Exception;
    boolean lock(String key, Context ctx) throws Exception;
    boolean lock(String key, long millisecondsToLive, Context ctx) throws Exception;
    void unlock(String key, Context ctx) throws Exception;
}
