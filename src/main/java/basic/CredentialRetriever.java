package basic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CredentialRetriever {
    private final String  provider;
    private final String identity;
    private final String credential;

    public CredentialRetriever(String filename, String provider) {
        JsonObject providerInformation = null;
        try {
            providerInformation = getProviderInformation(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.provider = provider;
        switch (provider){
            case "aws-s3":
                this.identity=providerInformation.get("identity").getAsString();
                this.credential=providerInformation.get("credential").getAsString();
                break;
            case "google-cloud-storage":
                this.identity = providerInformation.get("client_email").getAsString();
                this.credential=providerInformation.get("private_key").getAsString();
                break;
            default:
                System.out.println(provider+"doesn't exist");
                throw new RuntimeException("the provider doesn't exist");
        }
    }

    public static JsonObject getProviderInformation(String filename) throws IOException {
        Gson gson = new Gson();
        String file = "{}";
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(filename);
        assert is != null;
        file = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return gson.fromJson(file, JsonObject.class);
    }

    public String getCredential() {
        return credential;
    }

    public String getIdentity() {
        return identity;
    }

    public String getProvider() {
        return provider;
    }
}
