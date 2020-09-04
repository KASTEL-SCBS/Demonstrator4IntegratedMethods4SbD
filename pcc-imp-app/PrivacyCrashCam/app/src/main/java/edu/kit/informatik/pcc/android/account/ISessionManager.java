package edu.kit.informatik.pcc.android.account;

import android.support.annotation.Nullable;

import edu.kit.informatik.pcc.android.network.IRequestCompletion;
import edu.kit.informatik.pcc.android.network.IUserManagement;

public interface ISessionManager {
    /**
     * Logs in the user with the provided credentials. If the login is successful, the acquired session token is stored internally.
     * @param email The email to login with.
     * @param password The password to login with.
     * @param completion A completion handler to be called when the request has finished.
     */
    void login(String email, String password, final IRequestCompletion<IUserManagement.AuthenticationResult> completion);
    Boolean hasActiveSession();
    @Nullable String getAuthenticationToken();

    /**
     * Logs out the current user by removing the current session from storage and deleting all stored video content.
     */
    void logout();
}
