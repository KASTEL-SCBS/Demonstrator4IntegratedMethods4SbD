package edu.kit.informatik.pcc.android.network;

/**
 * Observer for events occurring when communicating with the server.
 *
 * @author Giorgio Gross
 */
public interface IRequestCompletion<E> {
    /**
     * Called when the response was received. The request was completed.
     * @param response the response of the server
     */
    void onResponse(E response);

    /**
     * Called when an error occurred while establishing the connection to the server.
     *
     * @param error Error message - not intended to be readable for user
     */
    void onError(String error);
}
