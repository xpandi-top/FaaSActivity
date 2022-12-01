package functions;

import basic.CredentialRetriever;
import basic.Request;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import httpfaas.FaaSMain;
import saaf.Inspector;
import saaf.Response;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
/*
 * Edit methods in this class for Section I UploadObject task.
 * Todo list:
 * [ ] modify variable objectName (Line 50) in method of uploadObjectProcedure
 * [ ] modify variable projectID (Line 57) in method of uploadObjectProcedure
 * */
public class UploadObject implements HttpFunction, RequestHandler<Request, HashMap<String, Object>> {
    private static final Gson gson = new Gson();

    private void uploadObjectProcedure(Request request, Inspector inspector, String credentialFileName, String provider, String bucketName) throws Exception {
        /*
         * This method is the procedure to upload text file with objectName to given bucket
         * */

        String name = request.getObjectName();
        if (name == null) throw new Exception("the object Name cannot be null");
        // Todo: Object Name for the file to upload. Replace `UWNetID` with your UW NetID(This is your uw email without “@uw.edu"). For example, My uw email is "dimo@uw.edu", so my UW NetID is “dimo”.
        String objectName = "UWNetID/" + name + ".txt";
        String contents = "Hello " + name + "! This is content from Cloud Function";// Contents to upload
        byte[] uploadContent = contents.getBytes(StandardCharsets.UTF_8);
        // Upload the file to Google Cloud Storage or S3
        if (provider.equals("google-cloud-storage")) {
            // Upload the file to Google Cloud Storage. the file name is objectName, and the bucket is bucketName, the upload Content is uploadContent
            //Todo: The ID of GCP Project, Replace `GPROJECT_ID`with `project_id` in the provided json file
            String projectID = "GPROJECT_ID";
            // Create connection to google cloud storage service, provide project ID and the service account credentials.
            Storage storage = StorageOptions.newBuilder().setProjectId(projectID)
                    .setCredentials(GoogleCredentials.fromStream(Files.newInputStream(Paths.get("src/main/resources/gcp_credential.json")))) // put the provided gcp_credential.json in src/main/resources/ folder
                    .build()
                    .getService();
            //Create Google Storage Object identifier.Blob id contains the information of the file/object we want to upload.
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            // Upload Contents
            storage.createFrom(blobInfo, new ByteArrayInputStream(uploadContent));
        } else if (provider.equals("aws-s3")) {
            // Upload the file to S3. the file name is objectName, and the bucket is bucketName, the upload Content is uploadContent
            CredentialRetriever credentialRetriever = new CredentialRetriever(credentialFileName, provider);
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(credentialRetriever.getIdentity(), credentialRetriever.getCredential())))
                    .withRegion(Regions.DEFAULT_REGION)
                    .build();
            InputStream inputStream = new ByteArrayInputStream(uploadContent);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(uploadContent.length);
            s3Client.putObject(bucketName, objectName, inputStream, meta);
        } else {
            throw new Exception("The provider should be provided. The available providers are google-cloud-storage, aws-s3");
        }
        inspector.addAttribute("message", "Hello " + name
                + "! This is Function is to Upload content to " + provider);

        Response response = new Response();
        response.setValue(objectName + " is created");
        inspector.consumeResponse(response);
    }

    @Override
    public HashMap<String, Object> handleRequest(Request request, Context context) {
        Inspector inspector = new Inspector();
        boolean isMac = System.getProperty("os.name").contains("Mac");
        if (!isMac) {
            inspector.inspectAll();
        }
        //****************START FUNCTION IMPLEMENTATION*************************
        String credentialFileName = "aws_credential.json";
        String provider = "aws-s3";
        String bucketName = "gcp.tutorial.562f22"; // The bucket name of Google Cloud Storage Name
        try {
            uploadObjectProcedure(request, inspector, credentialFileName, provider, bucketName);
        } catch (Exception e) {
            System.out.println("There is something error: " + e.getMessage());
            throw new RuntimeException(e);
        }
        //****************END FUNCTION IMPLEMENTATION***************************
        if (!isMac) {
            inspector.inspectAllDeltas();
        }
        return inspector.finish();
    }

    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        Inspector inspector = new Inspector();
        boolean isMac = System.getProperty("os.name").contains("Mac");
        if (!isMac) {
            inspector.inspectAll();
        }
        //****************START FUNCTION IMPLEMENTATION*************************
        Request request = gson.fromJson(httpRequest.getReader(), Request.class);
        String credentialFileName = "gcp_credential.json";
        String provider = "google-cloud-storage";
        String bucketName = "gcp-tutorial-562f22"; // The bucket name of Google Cloud Storage Name
        try {
            uploadObjectProcedure(request, inspector, credentialFileName, provider, bucketName);
        } catch (Exception e) {
            System.out.println("There is an error: " + e.getMessage());
            throw new RuntimeException(e);
        }
        //****************END FUNCTION IMPLEMENTATION***************************
        if (!isMac) {
            inspector.inspectAllDeltas();
        }
        BufferedWriter writer = httpResponse.getWriter();
        writer.write(gson.toJson(inspector.finish()));
    }

    public static void main(String[] args) throws Exception {
        UploadObject uploadObject = new UploadObject();
        FaaSMain.output(args, uploadObject, gson);
    }
}
