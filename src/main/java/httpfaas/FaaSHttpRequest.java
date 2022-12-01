package httpfaas;

import com.google.cloud.functions.HttpRequest;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FaaSHttpRequest implements HttpRequest {
    private String contents;
    public FaaSHttpRequest(String contents){
        this.contents = contents;
    }
    @Override
    public String getMethod() {
        return null;
    }

    @Override
    public String getUri() {
        return null;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public Optional<String> getQuery() {
        return Optional.empty();
    }

    @Override
    public Map<String, List<String>> getQueryParameters() {
        return null;
    }

    @Override
    public Map<String, HttpPart> getParts() {
        return null;
    }

    @Override
    public Optional<String> getContentType() {
        return Optional.empty();
    }

    @Override
    public long getContentLength() {
        return 0;
    }

    @Override
    public Optional<String> getCharacterEncoding() {
        return Optional.empty();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new StringReader(this.contents));    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return null;
    }
}
