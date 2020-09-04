package edu.kit.informatik.pcc.android.network.request;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public abstract class AbstractPostRequest<Result> extends AbstractRequest<Result> {
    public Form form;

    public void setForm(Form form) {
        this.form = form;
    }

    @Override
    public Response performRequest(Invocation.Builder requestBuilder) {
        return requestBuilder.post(Entity.entity(form,
                MediaType.APPLICATION_FORM_URLENCODED_TYPE), Response.class);
    }
}
