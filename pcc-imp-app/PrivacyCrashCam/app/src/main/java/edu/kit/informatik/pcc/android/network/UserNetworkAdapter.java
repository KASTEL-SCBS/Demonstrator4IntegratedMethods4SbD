package edu.kit.informatik.pcc.android.network;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import edu.kit.informatik.pcc.android.network.request.AbstractPostRequest;

public class UserNetworkAdapter implements IUserManagement {
    private IServerConfiguration serverConfiguration;

    public void setServerConfiguration(IServerConfiguration serverConfiguration) {
        assert this.serverConfiguration == null;
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    public void createAccount(String email, String password, IRequestCompletion<AuthenticationResult> completion) {
        assertCompletelySetup();

        CreateAccountRequest request = new CreateAccountRequest();
        request.setCompletion(completion);
        request.setForm(formFromCredentials(email, password));
        request.execute(serverConfiguration);
    }

    @Override
    public void login(String email, String password, IRequestCompletion<LoginResult> completion) {
        assertCompletelySetup();

        LoginRequest request = new LoginRequest();
        request.setCompletion(completion);
        request.setForm(formFromCredentials(email, password));
        request.execute(serverConfiguration);
    }

    private Form formFromCredentials(String email, String password) {
        Form form = new Form();
        form.param("mail", email);
        form.param("password", password);
        return form;
    }

    private void assertCompletelySetup() {
        assert serverConfiguration != null;
    }



    private class CreateAccountRequest extends AbstractPostRequest<AuthenticationResult> {
        public CreateAccountRequest() {
            setPath("account/create");
        }

        @Override
        public AuthenticationResult handleResponse(Response response) {
            if (response == null) {
                return AuthenticationResult.FAILURE_NETWORK;
            }
            else if (response.getStatus() == 200) {
                return AuthenticationResult.SUCCESS;
            }
            return AuthenticationResult.FAILURE_OTHER;
        }
    }

    private class LoginRequest extends AbstractPostRequest<LoginResult> {
        public LoginRequest() {
            setPath("account/login");
        }

        @Override
        public LoginResult handleResponse(Response response) {
            LoginResult result = new LoginResult();
            if (response == null) {
                result.result = AuthenticationResult.FAILURE_NETWORK;
            }
            else if (response.getStatus() == 200) {
                result.result = AuthenticationResult.SUCCESS;
                result.authenticationToken = response.getCookies().get("token").getValue();
            }
            else {
                result.result = AuthenticationResult.FAILURE_OTHER;
            }
            return result;
        }
    }
}
