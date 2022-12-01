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
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.storage.Storage;
import com.google.gson.Gson;
import httpfaas.FaaSMain;
import saaf.Inspector;
import saaf.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.*;
/*
* Edit methods in this class for Section II Migration task.
* Todo list:
* [ ] modify variable projectID (Line 83) in method of processImageProcedure
* [ ] write code to read file from Google Cloud Storage after line 86 in method of processImageProcedure
* [ ] write code to upload file to Google Cloud Storage after line 135 in method of processImageProcedure
* [ ] write code after line 199 in method of service as the handler for Google CLoud Function.
* */

public class ImageProcessing implements RequestHandler<Request, HashMap<String, Object>>, HttpFunction {
    private static final Gson gson = new Gson();

    private byte[] processImage(String photoURL) throws IOException {
        /*
         * This method take input of photoURL,
         * fetch the image from the url,
         * return the byte array of resized image
         * */
        URL url = new URL(photoURL);
        BufferedImage originImage = ImageIO.read(url);
        int resizedWidth = originImage.getWidth() / 10;
        int resizedHeight = originImage.getHeight() / 10;
        Image resizedImage = originImage.getScaledInstance(resizedWidth, resizedHeight, Image.SCALE_DEFAULT);
        BufferedImage resizedBufferedImage = new BufferedImage(resizedWidth, resizedHeight, BufferedImage.TYPE_INT_RGB);
        resizedBufferedImage.getGraphics().drawImage(resizedImage, 0, 0, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedBufferedImage, "jpg", outputStream);
        return outputStream.toByteArray();
    }

    private void processImageProcedure(Request request, Inspector inspector, String credentialFileName, String provider, String csvBucket, String imageBucket) throws Exception {
        /*
         * This method is the procedure to read csv file,
         * get list of photo urls from the csv file,
         * process the images get from the photo urls.
         * put the processed images to S3/Google
         * */
        CredentialRetriever credentialRetriever = new CredentialRetriever(credentialFileName, provider);
        // get project name and bucket name for the image
        String studentID = request.getStudentID();// your UW Net ID.
        String objectName = request.getObjectName();// the csv file name
        int startIndex = request.getStartIndex();// start index to process the csv file
        // add photo URLs to a list, add photo name to a list
        List<String> photoURLs = new ArrayList<>();
        List<String> photoNames = new ArrayList<>();
        AmazonS3 s3Client = null;
        Storage storage = null;
        Scanner scanner = null;
        // Read the csv file from Google Cloud Storage or S3
        if (provider.equals("google-cloud-storage")) {
            // Read the csv file from Google Cloud Storage. the file name is objectName, and the bucket is csvBucket
            //Todo: The ID of GCP Project, Replace `GPROJECT_ID`with `project_id` in the provided json file
            String projectID = "GPROJECT_ID";
            String readContent = null;
            // ************************* ADD CODE HERE - START ******************************
            // Todo: Write your code here, to read csv file from cloud storage







            // ************************* ADD CODE HERE - END ******************************
            // use scanner to scan the readContent
            scanner = new Scanner(readContent);
        } else if (provider.equals("aws-s3")) {
            // Read the csv file from S3. the file name is objectName, and the bucket is csvBucket
            s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(credentialRetriever.getIdentity(), credentialRetriever.getCredential())))
                    .withRegion(Regions.DEFAULT_REGION)
                    .build();
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(csvBucket, objectName));
            InputStream csvFile = s3Object.getObjectContent();
            scanner = new Scanner(csvFile);
        }

        // Parse the csv file: Read the dataset file scanning data line by line and process line by line
        if (scanner != null) {
            int currLine = 0;
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] vals = line.split("\\t");
                if (currLine >= startIndex && currLine < startIndex + 3) {
                    photoURLs.add(vals[2]);
                    photoNames.add(vals[0]);
                } else if (currLine >= startIndex + 3) {
                    break;
                }
                currLine++;
            }
            scanner.close();
        }
        // Process each photoURL to get IMAGE, resize the image to upload to Google Cloud Storage or S3
        boolean[] success = new boolean[photoURLs.size()];
        for (int i = 0; i < photoURLs.size(); i++) {
            String photoURL = photoURLs.get(i);
            String photoName = photoNames.get(i);
            String uploadName = studentID + "/" + photoName + ".jpg";// this is the object name for uploading object
            try {
                byte[] uploadImageBytes = processImage(photoURL);//  this is the data to upload to Google Cloud Storage/S3
                if (provider.equals("google-cloud-storage")) {
                    // Upload the processed image to Google Cloud Storage. Upload uploadImageBytes to imageBucket, set the object Name as uploadName
                    // ************************* ADD CODE HERE - START ******************************
                    // Todo write your code here to upload object






                    // ************************* ADD CODE HERE - END ******************************
                } else if (provider.equals("aws-s3")) {
                    // Upload the processed image to S3. Upload uploadImageBytes to imageBucket, set the object Name as uploadName
                    InputStream is = new ByteArrayInputStream(uploadImageBytes);
                    ObjectMetadata objectMetadata = new ObjectMetadata();
                    objectMetadata.setContentType("image/jpeg");
                    objectMetadata.setContentLength(uploadImageBytes.length);
                    // Save processed Image to S3
                    s3Client.putObject(imageBucket, uploadName, is, objectMetadata);
                }
                success[i] = true;
            } catch (IOException e) {
                System.out.println(i + "the photo fails processing with error: " + e.getMessage());
            }

        }
        Response response = new Response();
        response.setValue("This Function is for Processing Image with " + provider);
        inspector.addAttribute("successList", Arrays.toString(success));
        inspector.addAttribute("startIndex", startIndex);
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
        String csvBucket = "image.processing.aws.csv"; // this is the bucket storing the csv file
        String imageBucket = "image.processing.aws.images"; // this is the bucket storing the processed image
        String provider = "aws-s3";
        try {
            processImageProcedure(request, inspector, credentialFileName, provider, csvBucket, imageBucket);
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
        //Todo:  Migrate your code here




        //****************END FUNCTION IMPLEMENTATION***************************
        if (!isMac) {
            inspector.inspectAllDeltas();
        }
        BufferedWriter writer = httpResponse.getWriter();
        writer.write(gson.toJson(inspector.finish()));
    }

    public static void main(String[] args) throws Exception {
        ImageProcessing imageProcessing = new ImageProcessing();
        FaaSMain.output(args, imageProcessing, gson);
    }
}
