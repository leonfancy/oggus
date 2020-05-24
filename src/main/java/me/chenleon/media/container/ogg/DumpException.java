package me.chenleon.media.container.ogg;

import java.io.IOException;

public class DumpException extends RuntimeException {
    public DumpException(String s, Throwable e) {
        super(s, e);
    }
}
