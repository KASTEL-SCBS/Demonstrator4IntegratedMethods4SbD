package edu.kit.informatik.pcc.android.network.request;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public abstract class AbstractGetRequest<Result> extends AbstractRequest<Result> {
    @Override
    public Response performRequest(Invocation.Builder requestBuilder) {
        return requestBuilder.get();
    }
}
