package dev.outfluencer.mcproxy.networking.netty;

public class QuietException extends RuntimeException {

    public QuietException(String string) {
        super(string);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }


}
