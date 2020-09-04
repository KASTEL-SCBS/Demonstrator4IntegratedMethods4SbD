package edu.kit.informatik.pcc.android.account;

import edu.kit.informatik.pcc.android.network.IRequestCompletion;
import edu.kit.informatik.pcc.android.network.IUserManagement;
import edu.kit.informatik.pcc.android.storage.ISimpleValueStorage;
import edu.kit.informatik.pcc.android.storage.video.ILocalVideoManager;

public class SessionManager implements ISessionManager {
    private static final String sessionTokenKey = SessionManager.class.getName() + ".sessiontoken";

    private ILocalVideoManager localVideoManager;
    private IUserManagement userManagement;
    private ISimpleValueStorage simpleValueStorage;

    public void setLocalVideoManager(ILocalVideoManager localVideoManager) {
        assert this.localVideoManager == null;
        this.localVideoManager = localVideoManager;
    }

    public void setUserManagement(IUserManagement userManagement) {
        assert this.userManagement == null;
        this.userManagement = userManagement;
    }

    public void setSimpleValueStorage(ISimpleValueStorage simpleValueStorage) {
        assert this.simpleValueStorage == null;
        this.simpleValueStorage = simpleValueStorage;
    }

    @Override
    public void login(String email, String password, final IRequestCompletion<IUserManagement.AuthenticationResult> completion) {
        assertCompletelySetup();
        IRequestCompletion<IUserManagement.LoginResult> lCompletion = new IRequestCompletion<IUserManagement.LoginResult>() {
            @Override
            public void onResponse(IUserManagement.LoginResult response) {
                if (response.result == IUserManagement.AuthenticationResult.SUCCESS) {
                    simpleValueStorage.putString(sessionTokenKey, response.authenticationToken);
                }
                completion.onResponse(response.result);
            }

            @Override
            public void onError(String error) {
                completion.onError(error);
            }
        };
        userManagement.login(email, password, lCompletion);
    }

    @Override
    public Boolean hasActiveSession() {
        assertCompletelySetup();
        return getAuthenticationToken() != null;
    }

    @Override
    public String getAuthenticationToken() {
        assertCompletelySetup();
        return simpleValueStorage.getString(sessionTokenKey);
    }

    @Override
    public void logout() {
        assertCompletelySetup();
        for (int videoId: localVideoManager.getLocallyStoredVideoIds()) {
            localVideoManager.deleteContentForVideo(videoId);
        }
        simpleValueStorage.putString(sessionTokenKey, null);
    }

    private void assertCompletelySetup() {
        assert userManagement != null;
        assert localVideoManager != null;
        assert simpleValueStorage != null;
    }
}
