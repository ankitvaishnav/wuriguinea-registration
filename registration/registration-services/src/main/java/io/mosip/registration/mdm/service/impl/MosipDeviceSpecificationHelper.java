package io.mosip.registration.mdm.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegistrationExceptionConstants;

public class MosipDeviceSpecificationHelper {
    private ObjectMapper mapper = new ObjectMapper();

    private final String CONTENT_LENGTH = "Content-Length:";

    public String buildUrl(int port, String endPoint) {
        return String.format("%s:%s/%s", getRunningurl(), port, endPoint);
    }

    private String getRunningurl() {
        return "http://127.0.0.1";
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * Using the urlStream get the next JPEG image as a byte[]
     *
     * @return byte[] of the JPEG
     * @throws IOException
     * @throws RegBaseCheckedException
     */
    public byte[] getJPEGByteArray(InputStream urlStream, long maxTimeLimit)
            throws IOException, RegBaseCheckedException {

        int currByte = -1;

        boolean captureContentLength = false;
        StringWriter contentLengthStringWriter = new StringWriter(128);
        StringWriter headerWriter = new StringWriter(128);

        int contentLength = 0;

        while ((currByte = urlStream.read()) > -1) {
            if (captureContentLength) {
                if (currByte == 10 || currByte == 13) {
                    contentLength = Integer.parseInt(contentLengthStringWriter.toString().replace(" ", ""));
                    break;
                }
                contentLengthStringWriter.write(currByte);

            } else {
                headerWriter.write(currByte);
                String tempString = headerWriter.toString();
                int indexOf = tempString.indexOf(CONTENT_LENGTH);
                if (indexOf > 0) {
                    captureContentLength = true;
                }
            }
            timeOutCheck(maxTimeLimit);
        }

        // 255 indicates the start of the jpeg image
        while (urlStream.read() != 255) {

            timeOutCheck(maxTimeLimit);
        }

        // rest is the buffer
        byte[] imageBytes = new byte[contentLength + 1];
        // since we ate the original 255 , shove it back in
        imageBytes[0] = (byte) 255;
        int offset = 1;
        int numRead = 0;
        while (offset < imageBytes.length
                && (numRead = urlStream.read(imageBytes, offset, imageBytes.length - offset)) >= 0) {
            timeOutCheck(maxTimeLimit);
            offset += numRead;
        }

        return imageBytes;
    }

    private void timeOutCheck(long maxTimeLimit) throws RegBaseCheckedException {

        if (System.currentTimeMillis() > maxTimeLimit) {

            throw new RegBaseCheckedException(RegistrationExceptionConstants.MDS_STREAM_TIMEOUT.getErrorCode(),
                    RegistrationExceptionConstants.MDS_STREAM_TIMEOUT.getErrorMessage());
        }
    }
}
