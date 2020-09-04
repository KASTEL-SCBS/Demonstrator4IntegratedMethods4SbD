package edu.kit.informatik.pcc.android.network.request;

import android.os.AsyncTask;
import android.util.Log;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import edu.kit.informatik.pcc.android.network.IRequestCompletion;
import edu.kit.informatik.pcc.android.network.IServerConfiguration;
import jersey.repackaged.com.google.common.collect.Multimap;

/**
 * An abstract request performs some request using the provided <code>IServerConfiguration</code>
 * @param <Result> The type of the value provided in the completion.
 */
public abstract class AbstractRequest<Result> extends AsyncTask<IServerConfiguration, Integer, Result> {
    private String path;
    private MultivaluedMap<String, Object> httpHeaders;
    private IRequestCompletion<Result> completion;

    public abstract Response performRequest(Invocation.Builder requestBuilder);
    public abstract Result handleResponse(Response response);


    @Override
    protected Result doInBackground(IServerConfiguration... serverConfigurations) {
        if (serverConfigurations.length == 0) {
            return handleResponse(null);
        }
        IServerConfiguration serverConfiguration = serverConfigurations[0];
        String serverURL = serverConfiguration.scheme() + "://" + serverConfiguration.host() + ":" + serverConfiguration.port() + "/" + serverConfiguration.path();
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(serverURL).path(path).register(MultiPartFeature.class);
        Log.i(logTag(), "URI: " + webTarget.getUri().toASCIIString());

        Invocation.Builder builder = webTarget.request();
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            builder = builder.headers(httpHeaders);
        }
        Response response = performRequest(builder);
        client.close();
        return handleResponse(response);
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        completion.onResponse(result);
    }

    public String logTag() {
        return this.getClass().getName();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setHTTPHeaders(MultivaluedMap<String, Object> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public void setCompletion(IRequestCompletion<Result> completion) {
        this.completion = completion;
    }
}
