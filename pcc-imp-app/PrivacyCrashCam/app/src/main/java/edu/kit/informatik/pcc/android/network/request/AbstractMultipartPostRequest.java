package edu.kit.informatik.pcc.android.network.request;

import android.util.Log;

import org.glassfish.jersey.media.multipart.MultiPart;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

public abstract class AbstractMultipartPostRequest<Result> extends AbstractRequest<Result> {
    private MultiPart multiPart;

    public void setMultiPart(MultiPart multiPart) {
        this.multiPart = multiPart;
    }

    @Override
    public Response performRequest(Invocation.Builder requestBuilder) {
        Future<Response> futureResponse = requestBuilder.async().post(Entity.entity(multiPart, multiPart.getMediaType()), Response.class);

        // wait for response
        String responseContent;
        try {
            Response response = futureResponse.get();
            responseContent = response.readEntity(String.class);
            Log.i(logTag(), "response: " + responseContent);
            return response;
        } catch (InterruptedException | ExecutionException | ProcessingException e) {
            e.printStackTrace();
            Log.i(logTag(), "Failure on getting response!");
            return null;
        }
    }
}
