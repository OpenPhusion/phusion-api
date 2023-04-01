package cloud.phusion.storage;

import cloud.phusion.Context;

import java.util.List;

/**
 * Relational Data Storage.
 */
public interface DBStorage {

    void prepareTable(String tableName, String schema, Context ctx) throws Exception;
    void prepareTable(String tableName, String schema) throws Exception;
    void removeTable(String tableName, Context ctx) throws Exception;
    void removeTable(String tableName) throws Exception;

    boolean doesTableExist(String tableName, Context ctx) throws Exception;
    boolean doesTableExist(String tableName) throws Exception;

    int insertRecord(String tableName, Record record, Context ctx) throws Exception;
    int insertRecord(String tableName, Record record) throws Exception;
    int insertRecords(String tableName, String fields, List<Object> params, Context ctx) throws Exception;

    int upsertRecord(String tableName, String whereClause, List<Object> params, Record record, boolean noLog, Context ctx) throws Exception;
    int upsertRecord(String tableName, String whereClause, List<Object> params, Record record, Context ctx) throws Exception;
    int upsertRecord(String tableName, String whereClause, List<Object> params, Record record) throws Exception;
    int upsertRecordById(String tableName, String idField, Record record, Context ctx) throws Exception;
    int upsertRecordById(String tableName, String idField, Record record) throws Exception;

    Record[] queryRecords(String tableName, String selectClause, String whereClause, String groupClause, String havingClause, List<Object> params, String orderClause, long from, long length, Context ctx) throws Exception;
    Record[] queryRecords(String tableName, String selectClause, String whereClause, String groupClause, String havingClause, List<Object> params, String orderClause, long from, long length) throws Exception;
    Record[] queryRecords(String tableName, String selectClause, String whereClause, List<Object> params, String orderClause, long from, long length, Context ctx) throws Exception;
    Record[] queryRecords(String tableName, String selectClause, String whereClause, List<Object> params, String orderClause, long from, long length) throws Exception;
    Record[] queryRecords(String tableName, String selectClause, String whereClause, List<Object> params, Context ctx) throws Exception;
    Record[] queryRecords(String tableName, String selectClause, String whereClause, List<Object> params) throws Exception;

    long queryCount(String tableName, String selectClause, String whereClause, List<Object> params, Context ctx) throws Exception;
    long queryCount(String tableName, String selectClause, String whereClause, List<Object> params) throws Exception;

    Record queryRecordById(String tableName, String selectClause, String idField, Object value, Context ctx) throws Exception;
    Record queryRecordById(String tableName, String selectClause, String idField, Object value) throws Exception;

    Record[] freeQuery(String sql, List<Object> params, long from, long length, Context ctx) throws Exception;
    int freeUpdate(String sql, List<Object> params, Context ctx) throws Exception;

    int updateRecords(String tableName, Record record, String whereClause, List<Object> params, Context ctx) throws Exception;
    int updateRecords(String tableName, Record record, String whereClause, List<Object> params) throws Exception;
    int updateRecordById(String tableName, Record record, String idField, Object value, Context ctx) throws Exception;
    int updateRecordById(String tableName, Record record, String idField, Object value) throws Exception;
    int replaceRecordById(String tableName, Record record, String idField, Object value, Context ctx) throws Exception;
    int replaceRecordById(String tableName, Record record, String idField, Object value) throws Exception;

    int deleteRecords(String tableName, String whereClause, List<Object> params, Context ctx) throws Exception;
    int deleteRecords(String tableName, String whereClause, List<Object> params) throws Exception;
    int deleteRecordById(String tableName, String idField, Object value, Context ctx) throws Exception;
    int deleteRecordById(String tableName, String idField, Object value) throws Exception;

}
