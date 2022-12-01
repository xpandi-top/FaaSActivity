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
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import httpfaas.FaaSMain;
import saaf.Inspector;
import saaf.Response;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


/*
 * Edit methods in this class for Section I ReadObject task.
 * Todo list:
 * [ ] modify variable objectName (Line 46) in method of readObjectProcedure
 * [ ] modify variable projectID (Line 51) in method of readObjectProcedure
 * [ ] write code to read file from Google Cloud Storage after line 53 in method of readObjectProcedure
 * */

public class ReadObject implements HttpFunction, RequestHandler<Request, HashMap<String, Object>> {
    private static final Gson gson = new Gson();
    private void readObjectProcedure(Request request, Inspector inspector, String credentialFileName, String provider, String bucketName) throws Exception {
        /*
         * This method is the procedure to read file with given bucket and object name,
         * return the content
         * */
        String name = request.getObjectName();
        if (name == null) throw new Exception("the object Name cannot be null");
        // Todo: Object Name for the file to upload. Replace `UWNetID` with your UW NetID(This is your uw email without “@uw.edu"). For example, My uw email is "dimo@uw.edu", so my UW NetID is “dimo”.
        String objectName = "UWNetID/" + name + ".txt";
        String readContent = null;
        if (provider.equals("google-cloud-storage")){
            // Read the file from Google Cloud Storage. the file name is objectName, and the bucket is bucketName
            //Todo: The ID of GCP Project, Replace `GPROJECT_ID`with `project_id` in the provided json file
            String projectID = "GPROJECT_ID";
            // ************************* ADD CODE HERE - START ******************************
            //Todo: write code to read object from Google Cloud storage






            // ************************* ADD CODE HERE - END ******************************
        }else if (provider.equals("aws-s3")){
            // Read the file from S3. the file name is objectName, and the bucket is bucketName
            CredentialRetriever credentialRetriever = new CredentialRetriever(credentialFileName,provider);
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(credentialRetriever.getIdentity() ,credentialRetriever.getCredential())))
                    .withRegion(Regions.DEFAULT_REGION)
                    .build();
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectName));
            InputStream inputStream = s3Object.getObjectContent();
            readContent = new String(inputStream.readAllBytes(),StandardCharsets.UTF_8);
        }else {
            throw new Exception("The provider should be provided. The available providers are google-cloud-storage, aws-s3");
        }
        inspector.addAttribute("message", "Hello " + name
                + "! This is Function is to read content from "+provider);

        Response response = new Response();
        response.setValue("This is content read from ReadObject Function: "+ readContent);
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
        String credentialFileName="aws_credential.json";
        String provider="aws-s3";
        String bucketName = "gcp.tutorial.562f22"; // The bucket name of Google Cloud Storage Name
        try {
            readObjectProcedure(request,inspector,credentialFileName,provider,bucketName);
        } catch (Exception e) {
            System.out.println("There is an error: " + e.getMessage());
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
        String credentialFileName="gcp_credential.json";
        String provider="google-cloud-storage";
        String bucketName = "gcp-tutorial-562f22"; // The bucket name of Google Cloud Storage Name
        try {
            readObjectProcedure(request,inspector,credentialFileName,provider,bucketName);
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
        ReadObject readObject = new ReadObject();
        FaaSMain.output(args,readObject,gson);
    }


}
