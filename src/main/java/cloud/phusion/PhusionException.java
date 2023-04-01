package cloud.phusion;

import java.util.HashMap;
import java.util.Map;

/**
 * Example:
 *
 * throw new PhusionException(
 *      "CODE",
 *      "The failed operation",
 *      String.format("param=%s, param=%d, param=%.fms, param=%tF %tT",
 *          stringValue, integerValue, floatValue, dateValue, dateValue),
 *      ctx,
 *      ex
 * );
 */
public class PhusionException extends Exception {
    private static Map<String, String> codes = new HashMap<>();
    private String code;
    private String contextId;

    static {
        codes.put("", "");
    }

    public static void setCode(String code, String desc) {
        codes.put(code, desc);
    }

    public PhusionException(String code, String msg) {
        super(_composeMessage(code, msg, null, null, null));
        this.code = code;
        this.contextId = null;
    }

    public PhusionException(String code, String msg, Throwable t) {
        super(_composeMessage(code, msg, null, null, t), t);
        this.code = code;
        this.contextId = null;
    }

    public PhusionException(String code, String msg, Context ctx) {
        super(_composeMessage(code, msg, null, ctx, null));
        this.code = code;
        this.contextId = ctx==null ? null : ctx.getId();
    }

    public PhusionException(String code, String msg, Context ctx, Throwable t) {
        super(_composeMessage(code, msg, null, ctx, t), t);
        this.code = code;
        this.contextId = ctx==null ? null : ctx.getId();
    }

    public PhusionException(String code, String msg, String data) {
        super(_composeMessage(code, msg, data, null, null));
        this.code = code;
        this.contextId = null;
    }

    public PhusionException(String code, String msg, String data, Throwable t) {
        super(_composeMessage(code, msg, data, null, t), t);
        this.code = code;
        this.contextId = null;
    }

    public PhusionException(String code, String msg, String data, Context ctx) {
        super(_composeMessage(code, msg, data, ctx, null));
        this.code = code;
        this.contextId = ctx==null ? null : ctx.getId();
    }

    public PhusionException(String code, String msg, String data, Context ctx, Throwable t) {
        super(_composeMessage(code, msg, data, ctx, t), t);
        this.code = code;
        this.contextId = ctx==null ? null : ctx.getId();
    }

    private static String _composeMessage(String code, String msg, String data, Context ctx, Throwable t) {
        StringBuilder result = new StringBuilder();

        result.append(code);
        String codeDesc = codes.get(code);
        if (codeDesc!=null && codeDesc.length()>0) result.append(" (").append(codeDesc).append(") ");

        result.append(msg);
        if (t != null) result.append(": ").append(t.getMessage());

        String ctxInfo = ctx==null ? null : ctx.getContextInfo();

        if (ctxInfo!=null && t instanceof PhusionException) {
            // If the exception has same context id as "ctx" (they are relatives), do not output "ctx"

            String exCtxId = ((PhusionException)t).getContextId();
            String ctxId = ctx.getId();
            if (exCtxId!=null && ctxId!=null && !exCtxId.equals(ctxId)) ctxInfo = null;
        }

        if (ctxInfo!=null && ctxInfo.length()==0) ctxInfo = null;
        if (data!=null && data.length()==0) data = null;

        if (data!=null || ctxInfo!=null) result.append(". ");

        if (ctxInfo != null) {
            result.append(ctxInfo);
            if (data != null) result.append(", ").append(data);
        } else if (data != null) result.append(data);

        return result.toString();
    }

    public String getCode() {
        return code;
    }

    public String getContextId() {
        return contextId;
    }

}
