package de.pcc.privacycrashcam.applicationlogic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.pcc.privacycrashcam.R;
import de.pcc.privacycrashcam.gui.CameraActivity;
import edu.kit.informatik.pcc.android.Client;
import edu.kit.informatik.pcc.android.network.IRequestCompletion;
import edu.kit.informatik.pcc.android.network.IUserManagement;

/**
 * Shows the log in fragment.
 *
 * @author Giorgio Gross, David Laubenstein
 */
public class LogInFragment extends Fragment {

    private final static String URL_REGISTER = Client.getGlobal().getServerConfiguration().host() + ":9999";

    private EditText et_mail;
    private EditText et_password;
    private ProgressBar loginProgress;
    private Button login;
    private Button register;

    private View.OnClickListener mRegisterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_REGISTER));
            startActivity(browserIntent);
        }

    };

    private View.OnClickListener mLogInistener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            toggleVisibility(login, loginProgress);

            final String mail = et_mail.getText().toString();
            final String pw = et_password.getText().toString();

            IRequestCompletion<IUserManagement.AuthenticationResult> completion = new IRequestCompletion<IUserManagement.AuthenticationResult>() {
                @Override
                public void onResponse(IUserManagement.AuthenticationResult response) {
                    switch (response) {
                        case SUCCESS:
                            CameraActivity.Launch(getActivity());
                            getActivity().finish();
                            break;
                        case FAILURE_MISMATCH:
                            Toast.makeText(getContext(), "Mail and password are not matching",
                                    Toast.LENGTH_SHORT).show();
                            toggleVisibility(login, loginProgress);
                            break;
                        case FAILURE_MISSING:
                            Toast.makeText(getContext(), "Account not existing",
                                    Toast.LENGTH_SHORT).show();
                            toggleVisibility(login, loginProgress);
                            break;
                        case FAILURE_OTHER:
                            Toast.makeText(getContext(), "Account error!",
                                    Toast.LENGTH_SHORT).show();
                            toggleVisibility(login, loginProgress);
                            break;
                        default:
                            Toast.makeText(getContext(), getString(R.string.error_no_connection),
                                    Toast.LENGTH_SHORT).show();
                            toggleVisibility(login, loginProgress);
                            break;
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), getString(R.string.error_no_connection),
                            Toast.LENGTH_SHORT).show();
                    toggleVisibility(login, loginProgress);
                }
            };

            Client.getGlobal().getSessionManager().login(mail, pw, completion);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // get the main layout describing the content
        RelativeLayout base = (RelativeLayout) inflater.inflate(R.layout.content_login, container, false);

        // init view components
        et_mail = (EditText) base.findViewById(R.id.et_mail);
        et_password = (EditText) base.findViewById(R.id.et_password);
        loginProgress = (ProgressBar) base.findViewById(R.id.pb_login);
        login = (Button) base.findViewById(R.id.b_login);
        login.setOnClickListener(mLogInistener);

        register = (Button) base.findViewById(R.id.b_register);
        register.setOnClickListener(mRegisterListener);
        return base;
    }

    private void toggleVisibility(Button button, ProgressBar progressBar) {
        if (button.getVisibility() == View.VISIBLE) {
            button.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
