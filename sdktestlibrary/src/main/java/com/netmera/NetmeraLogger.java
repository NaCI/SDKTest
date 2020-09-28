package com.netmera;

import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class NetmeraLogger {
    private static final String LOG_TAG = "NETMERA";
    private static final int methodCount = 1;
    private static final int methodOffset = 0;
    private static final boolean showThreadInfo = false;
    private static boolean enabled = false;
    private static final int DEBUG = 3;
    private static final int ERROR = 6;
    private static final int ASSERT = 7;
    private static final int INFO = 4;
    private static final int VERBOSE = 2;
    private static final int WARN = 5;
    private static final int CHUNK_SIZE = 4000;
    private static final int JSON_INDENT = 4;
    private static final int MIN_STACK_OFFSET = 3;
    private static final char TOP_RIGHT_CORNER = '╗';
    private static final char TOP_LEFT_CORNER = '╔';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char BOTTOM_RIGHT_CORNER = '╝';
    private static final char MIDDLE_CORNER = '╟';
    private static final char HORIZONTAL_DOUBLE_LINE = '║';
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String TOP_BORDER = "╔════════════════════════════════════════════════════════════════════════════════════════";
    private static final String BOTTOM_BORDER = "╚════════════════════════════════════════════════════════════════════════════════════════";
    private static final String MIDDLE_BORDER = "╟────────────────────────────────────────────────────────────────────────────────────────";
    private final ThreadLocal<String> localTag = new ThreadLocal();
    private final ThreadLocal<Integer> localMethodCount = new ThreadLocal();

    NetmeraLogger() {
    }

    public NetmeraLogger t(String tag, int methodCount) {
        if (tag != null) {
            this.localTag.set(tag);
        }

        this.localMethodCount.set(methodCount);
        return this;
    }

    public static void logging(boolean enabled) {
        NetmeraLogger.enabled = enabled;
    }

    public void d(String message, Object... args) {
        this.log(3, message, args);
    }

    public void e(String message, Object... args) {
        this.e((Throwable)null, message, args);
    }

    public void e(Throwable throwable, String message, Object... args) {
        if (throwable != null && message != null) {
            message = message + " : " + throwable.toString();
        }

        if (throwable != null && message == null) {
            message = throwable.toString();
        }

        if (message == null) {
            message = "No message/exception is set";
        }

        this.log(6, message, args);
    }

    public void w(String message, Object... args) {
        this.log(5, message, args);
    }

    public void i(String message, Object... args) {
        this.log(4, message, args);
    }

    public void v(String message, Object... args) {
        this.log(2, message, args);
    }

    public void wtf(String message, Object... args) {
        this.log(7, message, args);
    }

    public void json(String json) {
        if (TextUtils.isEmpty(json)) {
            this.d("Empty/Null json content");
        } else {
            try {
                String message;
                if (json.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(json);
                    message = jsonObject.toString(4);
                    this.d(message);
                    return;
                }

                if (json.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(json);
                    message = jsonArray.toString(4);
                    this.d(message);
                }
            } catch (JSONException var4) {
                this.e(var4.getCause().getMessage() + "\n" + json);
            }

        }
    }

    private synchronized void log(int logType, String msg, Object... args) {
        if (enabled) {
            String tag = this.getTag();
            String message = this.createMessage(msg, args);
            int methodCount = this.getMethodCount();
            this.logTopBorder(logType, tag);
            this.logHeaderContent(logType, tag, methodCount);
            byte[] bytes = message.getBytes();
            int length = bytes.length;
            if (length <= 4000) {
                if (methodCount > 0) {
                    this.logDivider(logType, tag);
                }

                this.logContent(logType, tag, message);
                this.logBottomBorder(logType, tag);
            } else {
                if (methodCount > 0) {
                    this.logDivider(logType, tag);
                }

                for(int i = 0; i < length; i += 4000) {
                    int count = Math.min(length - i, 4000);
                    this.logContent(logType, tag, new String(bytes, i, count));
                }

                this.logBottomBorder(logType, tag);
            }
        }
    }

    private void logTopBorder(int logType, String tag) {
        this.logChunk(logType, tag, "╔════════════════════════════════════════════════════════════════════════════════════════");
    }

    private void logHeaderContent(int logType, String tag, int methodCount) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String level = "";
        int stackOffset = this.getStackOffset(trace) + 0;
        if (methodCount + stackOffset > trace.length) {
            methodCount = trace.length - stackOffset - 1;
        }

        for(int i = methodCount; i > 0; --i) {
            int stackIndex = i + stackOffset;
            if (stackIndex < trace.length) {
                StringBuilder builder = new StringBuilder();
                builder.append("║ ").append(level).append(this.getSimpleClassName(trace[stackIndex].getClassName())).append(".").append(trace[stackIndex].getMethodName());
                level = level + "   ";
                this.logChunk(logType, tag, builder.toString());
            }
        }

    }

    private void logBottomBorder(int logType, String tag) {
        this.logChunk(logType, tag, "╚════════════════════════════════════════════════════════════════════════════════════════");
    }

    private void logDivider(int logType, String tag) {
        this.logChunk(logType, tag, "╟────────────────────────────────────────────────────────────────────────────────────────");
    }

    private void logContent(int logType, String tag, String chunk) {
        String[] lines = chunk.split(System.getProperty("line.separator"));
        String[] var5 = lines;
        int var6 = lines.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            String line = var5[var7];
            this.logChunk(logType, tag, "║ " + line);
        }

    }

    private void logChunk(int logType, String tag, String chunk) {
        String finalTag = this.formatTag(tag);
        switch(logType) {
            case 2:
                Log.v(finalTag, chunk);
                break;
            case 3:
            default:
                Log.d(finalTag, chunk);
                break;
            case 4:
                Log.i(finalTag, chunk);
                break;
            case 5:
                Log.w(finalTag, chunk);
                break;
            case 6:
                Log.e(finalTag, chunk);
                break;
            case 7:
                Log.wtf(finalTag, chunk);
        }

    }

    private String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    private String formatTag(String tag) {
        return !TextUtils.isEmpty(tag) && !TextUtils.equals("NETMERA", tag) ? "NETMERA-" + tag : "NETMERA";
    }

    private String getTag() {
        String tag = (String)this.localTag.get();
        if (tag != null) {
            this.localTag.remove();
            return tag;
        } else {
            return "NETMERA";
        }
    }

    private String createMessage(String message, Object... args) {
        return args.length == 0 ? message : String.format(message, args);
    }

    private int getMethodCount() {
        Integer count = (Integer)this.localMethodCount.get();
        int result = 1;
        if (count != null) {
            this.localMethodCount.remove();
            result = count;
        }

        if (result < 0) {
            throw new IllegalStateException("methodCount cannot be negative");
        } else {
            return result;
        }
    }

    private int getStackOffset(StackTraceElement[] trace) {
        for(int i = 3; i < trace.length; ++i) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (!name.equals(NetmeraLogger.class.getName())) {
                --i;
                return i;
            }
        }

        return -1;
    }
}