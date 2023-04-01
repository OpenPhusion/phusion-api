package cloud.phusion;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

public class DataObject {
    private Object data;
    private String dataString;
    private JSONObject dataJSONObject;
    private JSONArray dataJSONArray;

    public DataObject(Object data) {
        super();
        this.data = data;
        this.dataJSONObject = null;
        this.dataJSONArray = null;
        this.dataString = null;
    }

    public void setData(Object data) {
        this.data = data;
        this.dataJSONObject = null;
        this.dataJSONArray = null;
        this.dataString = null;
    }

    public Object getData() {
        return data;
    }

    public JSONObject getJSONObject() {
        if (dataJSONObject == null) {
            if (data == null) dataJSONObject = null;
            else if (data instanceof JSONObject) dataJSONObject = (JSONObject) data;
            else if (data instanceof String) {
                try {
                    dataJSONObject = JSON.parseObject((String) data);
                } catch (Exception ex) {
                    dataJSONObject = null;
                }
            }
            else dataJSONObject = null;
        }

        return dataJSONObject;
    }

    public JSONArray getJSONArray() {
        if (dataJSONArray == null) {
            if (data == null) dataJSONArray = null;
            else if (data instanceof JSONArray) dataJSONArray = (JSONArray) data;
            else if (data instanceof String) {
                try {
                    dataJSONArray = JSON.parseArray((String) data);
                } catch (Exception ex) {
                    dataJSONArray = null;
                }
            }
            else dataJSONArray = null;
        }

        return dataJSONArray;
    }

    public String getString() {
        if (dataString == null) {
            if (data == null) dataString = null;
            else if (data instanceof String) dataString = (String) data;
            else if (data instanceof JSONObject) dataString = ((JSONObject) data).toJSONString();
            else if (data instanceof JSONArray) dataString = ((JSONArray) data).toJSONString();
            else dataString = null;
        }

        return dataString;
    }

    public String getString(int maxLength) {
        String result = getString();
        if (result!=null && result.length()>maxLength) {
            return result.substring(0, maxLength-4) + " ...";
        }
        else
            return result;
    }

}
