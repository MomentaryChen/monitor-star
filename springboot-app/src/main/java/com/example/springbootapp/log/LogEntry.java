package com.example.springbootapp.log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * 單筆日誌實體，對應 logback JSON 輸出一行。
 * 具備 Spring Cloud 分散式追蹤的 traceId / spanId。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String timestamp;
    private String message;

    @JsonProperty("logger_name")
    private String loggerName;

    @JsonProperty("thread_name")
    private String threadName;

    private String level;

    /** Spring Cloud Sleuth 分散式追蹤 ID，串起同一請求跨服務的日誌 */
    private String traceId;

    /** 當前 span ID */
    private String spanId;

    public LogEntry() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }
}
