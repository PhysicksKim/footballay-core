package com.footballay.core.websocket.response;

public abstract class AbstractRemoteResponse extends AbstractBaseResponse {
    protected final String pubPath;
    protected final String subPath;
    protected final String remoteCode;

    public AbstractRemoteResponse(String remoteCode) {
        this(200, "success", remoteCode);
    }

    public AbstractRemoteResponse(int code, String message, String remoteCode) {
        super(code, message);
        this.pubPath = "/app/remote/" + remoteCode;
        this.subPath = "/user/topic/remote/" + remoteCode;
        this.remoteCode = remoteCode;
    }

    public String getPubPath() {
        return this.pubPath;
    }

    public String getSubPath() {
        return this.subPath;
    }

    public String getRemoteCode() {
        return this.remoteCode;
    }
}
