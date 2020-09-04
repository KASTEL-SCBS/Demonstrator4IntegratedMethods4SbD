package edu.kit.informatik.pcc.android.network;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import java.io.File;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import edu.kit.informatik.pcc.android.network.request.AbstractMultipartPostRequest;

public class VideoNetworkAdapter implements IClientVideoUpload {
    private IServerConfiguration serverConfiguration;

    public void setServerConfiguration(IServerConfiguration serverConfiguration) {
        assert this.serverConfiguration == null;
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    public void uploadVideo(File encryptedVideo, File encryptedMetadata, byte[] encryptedKey, String authenticationToken, IRequestCompletion<UploadResult> completion) {
        assertCompletelySetup();

        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        FileDataBodyPart video = new FileDataBodyPart("video", encryptedVideo.getAbsoluteFile(), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        FileDataBodyPart metadata = new FileDataBodyPart("metadata", encryptedMetadata.getAbsoluteFile(), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        FormDataBodyPart key = new FormDataBodyPart("key", encryptedKey, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        multiPart.bodyPart(video);
        multiPart.bodyPart(metadata);
        multiPart.bodyPart(key);

        UploadVideoRequest request = new UploadVideoRequest();
        request.setMultiPart(multiPart);

        MultivaluedHashMap httpHeaders = new MultivaluedHashMap<String, Object>();
        httpHeaders.add("token", authenticationToken);
        request.setHTTPHeaders(httpHeaders);
        request.setCompletion(completion);
        request.execute(serverConfiguration);
    }

    private void assertCompletelySetup() {
        assert serverConfiguration != null;
    }

    private class UploadVideoRequest extends AbstractMultipartPostRequest<UploadResult> {
        public UploadVideoRequest() {
            setPath("videos");
        }

        @Override
        public UploadResult handleResponse(Response response) {
            if (response == null) {
                return UploadResult.NETWORK_FAILURE;
            }
            else if (response.getStatus() == 200) {
                return UploadResult.SUCCESS;
            }
            else {
                return UploadResult.FAILURE_OTHER;
            }
        }
    }
}
