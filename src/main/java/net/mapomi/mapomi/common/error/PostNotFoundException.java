package net.mapomi.mapomi.common.error;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException() {
        super();
    }

    @Override
    public synchronized Throwable fillInStackTrace() {return this;}
}
