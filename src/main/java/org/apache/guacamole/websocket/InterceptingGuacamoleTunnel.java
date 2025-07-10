package org.apache.guacamole.websocket;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.io.GuacamoleWriter;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;

import java.util.UUID;

public class InterceptingGuacamoleTunnel implements GuacamoleTunnel {

    private final GuacamoleTunnel delegate;
    private final String connectionId;

    public InterceptingGuacamoleTunnel(GuacamoleTunnel delegate) {
        this.delegate = delegate;
        this.connectionId = delegate.getUUID().toString();
    }

    @Override
    public GuacamoleReader acquireReader() {
        GuacamoleReader originalReader = delegate.acquireReader();
        return new InterceptingGuacamoleReader(originalReader, connectionId);
    }

    @Override
    public void releaseReader() {
        delegate.releaseReader();
    }

    @Override
    public GuacamoleWriter acquireWriter() {
        return delegate.acquireWriter();
    }

    @Override
    public void releaseWriter() {
        delegate.releaseWriter();
    }

    @Override
    public void close() throws GuacamoleException {
        delegate.close();
    }

    @Override
    public UUID getUUID() {
        return delegate.getUUID();
    }

    @Override
    public GuacamoleSocket getSocket() {
        return delegate.getSocket();
    }

    @Override
    public boolean hasQueuedReaderThreads() {
        return delegate.hasQueuedReaderThreads();
    }

    @Override
    public boolean hasQueuedWriterThreads() {
        return delegate.hasQueuedWriterThreads();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }
}
