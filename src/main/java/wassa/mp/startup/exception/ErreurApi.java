package wassa.mp.startup.exception;

import java.time.LocalDateTime;

public class ErreurApi {

    private int status;
    private String code;
    private String message;
    private LocalDateTime timestamp;

    public ErreurApi(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}