package cloud.phusion.storage;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;
import java.util.*;

/**
 * Record (line) of a database table.
 *
 * It is not thread-safe.
 */
public class Record {

    Map<String, Object> data = null;

    public Record() {
        super();
        data = new HashMap<String, Object>();
    }

    /**
     * Create a record from a JSON string.
     */
    public Record(String json) throws Exception {
        super();
        data = new HashMap<String, Object>();

        JSONObject doc = JSON.parseObject(json);
        Set<String> fields = doc.keySet();
        for (String field : fields) {
            data.put(field, doc.get(field));
        }
    }

    public Object getValue(String field) {
        return data.get(field);
    }

    public String getString(String field) {
        Object value = data.get(field);
        if (value == null) return null;
        else if (value instanceof String) return (String) value;
        else return value.toString();
    }

    public Boolean getBoolean(String field) {
        Object value = data.get(field);
        if (value == null) return null;
        else if (value instanceof String) return new Boolean((String)value);
        else if (value instanceof Integer) return new Boolean(((Integer)value).intValue()==0 ? false : true);
        else return (Boolean) value;
    }

    public Integer getInteger(String field) {
        Object value = data.get(field);
        if (value == null) return null;
        else if (value instanceof String) return new Integer((String)value);
        else if (value instanceof Long) return new Integer(((Long)value).intValue());
        else if (value instanceof Double) return new Integer(((Double)value).intValue());
        else if (value instanceof BigDecimal) return new Integer(((BigDecimal) value).intValue());
        else return (Integer) value;
    }

    public Long getLong(String field) {
        Object value = data.get(field);
        if (value == null) return null;
        else if (value instanceof String) return new Long((String)value);
        else if (value instanceof Integer) return new Long(((Integer) value).intValue());
        else if (value instanceof Double) return new Long(((Double)value).longValue());
        else if (value instanceof BigDecimal) return new Long(((BigDecimal) value).longValue());
        else return (Long) value;
    }

    public Float getFloat(String field) {
        Object value = data.get(field);
        if (value == null) return null;
        else if (value instanceof String) return new Float((String)value);
        else if (value instanceof Integer) return new Float(((Integer) value).intValue());
        else if (value instanceof Long) return new Float(((Long) value).longValue());
        else if (value instanceof BigDecimal) return new Float(((BigDecimal) value).floatValue());
        else if (value instanceof Double) return new Float(((Double) value).floatValue());
        else return (Float) value;
    }

    public Double getDouble(String field) {
        Object value = data.get(field);
        if (value == null) return null;
        else if (value instanceof String) return new Double((String)value);
        else if (value instanceof Float) return new Double(((Float) value).floatValue());
        else if (value instanceof Integer) return new Double(((Integer) value).intValue());
        else if (value instanceof Long) return new Double(((Long) value).longValue());
        else if (value instanceof BigDecimal) return new Double(((BigDecimal) value).floatValue());
        else return (Double) value;
    }

    public void setValue(String field, Object value) {
        data.put(field, value);
    }

    public void removeValue(String field) {
        data.remove(field);
    }

    public Set<String> getFields() {
        return data.keySet();
    }

    public boolean doesFieldExist(String field) {
        return data.containsKey(field);
    }

    /**
     * Clone a new record with specified fields.
     */
    public Record clone(String[] fields) {
        Record result = new Record();

        if (fields==null || fields.length==0) {
            Set<String> fs = getFields();
            for (String f : fs) {
                Object v = data.get(f);
                if (v != null) result.setValue(f, v);
            }
        }
        else {
            for (int i = 0; i < fields.length; i++) {
                Object v = data.get(fields[i]);
                if (v != null) result.setValue(fields[i], v);
            }
        }

        return result;
    }

    public Record clone() {
        return clone(null);
    }

    public String toJSONString() {
        return JSON.toJSONString(data);
    }

}
