package edu.kit.informatik.pcc.android.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface IUserManagement {
    void createAccount(String email, String password, IRequestCompletion<AuthenticationResult> completion);
    void login(String email, String password, IRequestCompletion<LoginResult> completion);

    enum AuthenticationResult {
        //! The authentication was successful
        SUCCESS,

        //! Network not reachable
        FAILURE_NETWORK,

        //! Account does not exist or mail and password do not match
        FAILURE_MISMATCH,

        //! Account does not exist or mail and password do not match
        FAILURE_MISSING,

        //! Something unexpected went wrong
        FAILURE_OTHER
    }

    class LoginResult {
        @Nullable public String authenticationToken;
        @NonNull public AuthenticationResult result;
    }
}
