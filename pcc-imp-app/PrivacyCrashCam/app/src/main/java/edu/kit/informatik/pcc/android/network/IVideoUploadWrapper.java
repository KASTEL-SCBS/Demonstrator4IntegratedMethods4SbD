package edu.kit.informatik.pcc.android.network;

/**
 * An implementing instance tries to upload the video identified by the <code>videoId</code> hiding all details to the caller.
 * Hidden details include session validation, getting the required files and checking their validity and executing the network request.
 * If any input is invalid even before the request is performed, the completion handler is called with an appropriate result.
 */
public interface IVideoUploadWrapper {
    void uploadVideo(int videoId, IRequestCompletion<IClientVideoUpload.UploadResult> completion);
}
