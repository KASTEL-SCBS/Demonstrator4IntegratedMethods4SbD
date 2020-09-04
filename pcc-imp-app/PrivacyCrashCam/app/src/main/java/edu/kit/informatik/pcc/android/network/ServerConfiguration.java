package edu.kit.informatik.pcc.android.network;

public final class ServerConfiguration implements IServerConfiguration {
    @Override
    public String scheme() {
        return "http";
    }

    @Override
    public String host() {
        return "localhost";
    }

    @Override
    public String path() {
        return "webservice";
    }

    @Override
    public int port() {
        return 2222;
    }
}
