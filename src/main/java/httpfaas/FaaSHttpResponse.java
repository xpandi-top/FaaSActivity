package httpfaas;

import com.google.cloud.functions.HttpResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FaaSHttpResponse implements HttpResponse{
    private final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out));

    @Override
    public void setStatusCode(int code) {

    }

    @Override
    public void setStatusCode(int code, String message) {

    }

    @Override
    public void setContentType(String contentType) {

    }

    @Override
    public Optional<String> getContentType() {
        return Optional.empty();
    }

    @Override
    public void appendHeader(String header, String value) {

    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public BufferedWriter getWriter() throws IOException {
        return this.bufferedWriter;
    }
}