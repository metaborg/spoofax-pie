package org.metaborg.spoofax.eclipse.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import rx.functions.Action1;

public class OnCloseByteArrayOutputStream extends ByteArrayOutputStream {
    final Action1<ByteArrayOutputStream> action;

    public OnCloseByteArrayOutputStream(Action1<ByteArrayOutputStream> action) {
        this.action = action;
    }

    @Override public void close() throws IOException {
        action.call(this);
        super.close();
    }
}
