# FaaS Migration Activity
## SECTION I: FaaS Tutorial on Google Cloud Platform
### Objective
Google is an alternative cloud provider to AWS. AWS and Google Cloud both provide multiple cloud storage services and computing services. While AWS provides the Simple Storage Service (S3) as the platform's object storage service, Google Cloud provides Google Cloud Storage as the object storage service.
Google Cloud Functions is an alternative serverless Function-as-a-Service (FaaS) platform to AWS Lambda.
In this tutorial, we introduce the Google Cloud Platform, Google Cloud Functions, how to use the command line to upload functions to Google Cloud Functions (GCF), how to test a Google Cloud Function using the CLI, and how to use Google Cloud Storage.

We will practice how to interact with Google Cloud Storage using Google Cloud Functions to read and write a file in Google Cloud Storage.

### Prerequisites
Please make sure you have installed the following tools on your computer:
- [ ] git 
- [ ] Java 11 
- [ ] maven 
- [ ] gcloud
- [ ] AWS CLI (optional)


The gcloud command line interface (CLI) provides a command-line based interface for managing Google Cloud resources. We can configure the gcloud CLI on our personal computer to then perform tasks from the terminal or scripts.
Follow this [gcloud install tutorial](https://cloud.google.com/sdk/docs/install) to install the gcloud CLI on your personal computer (e.g. desktop or laptop).
If you add the gcloud CLI to your `PATH` variable then you can run gcloud CLI commands directly. If not, you need to remember where is the gcloud CLI is installed, and use fully qualified paths to run the commands.
After installation of the gcloud CLI, use gcloud to activate the service account provided.

You will receive two JSON files that contains a Google Cloud service account key (gcp_credential.json) and AWS Credential file (aws_credential.json). Please store them safely.
Replace the KEYFILE below with the file path of the provided gcp_credential.json file stored on your computer. Replace `SERVICE-ACCOUNT` with the client_email in the json file. Replace `GPROJECT_ID`with `project_id` in the provided json file.
```bash
gcloud auth activate-service-account SERVICE-ACCOUNT --key-file=KEYFILE  --project=GPROJECT_ID
```
You can use this command to check whether your gcloud configuration is activated.
```bash
gcloud config configurations list
```
if you see the Account is the service account and the project is the project_id in json file. You are ready to go.

### Use git to download the codes
```bash
git clone https://github.com/xpandi-top/FaaSActivity.git
```
Open the downloaded folder. Put the provided gcp_credential json file in to the `src/main/resources` folder. You may need to create the resources folder under main. Rename this file to `gcp_credential.json`.

Use any IDE you like or Editor to open the project folder (the folder with pom.xml). Now we are going to Write a function to upload a text file to Google cloud storage.

Google Cloud Storage is similar to AWS S3. We can upload, update, read, and delete files in Cloud Storage. The bucket has already been created in advance in the Google cloud project. 

We are going to directly interact with the bucket in this project without worrying about how to create a bucket in the cloud. 

### Upload a file to cloud storage.
We are going to deploy a UploadObject function to upload a file to Google Object Storage. The java class you are going to work with is `src/main/java/functions/UploadObject.java`.
The `UploadObject` class implements `HttpFunction` for uploading to google cloud function and `RequestHandler<Request, HashMap<String, Object>>` for uploading to AWS lambda. In this class, method `handleRequest` is handler method for AWS. Method `service` is handler method for Google Cloud Functions.
The `uploadObjectProcedure` method is about some common code and logic of function. 

Modify the variables `projectID`(line 57) and `objectName`(line 50) in `UploadObject`.

The following code is from line 46 - line 54 of `src/main/java/functions/UploadObject.java`. This code implements uploading content from memory \(in this case the contents String\) to the google cloud storage. We first need to create a storage object which can access the Google Cloud Storage service from Java. We then create a BlobInfo object that describes the bucketname and objectname for saving the blob using the BlobId class. Then upload the contents to the bucket. 
```java
        String projectID = "GPROJECT_ID"; //Todo: The ID of GCP Project, Replace `GPROJECT_ID`with `project_id` in the provided json file
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
```
Now you can try to compile and run to test it.
Make sure you are in the project directory.
```bash
mvn clean -f pom.xml
mvn verify -f pom.xml
```
Then execute your function from the command line to test it locally. You can change the name `Alice` to some other name.
```bash
java -cp target/FaaSActivity-1.0-SNAPSHOT-jar-with-dependencies.jar functions.UploadObject GCP "{\"objectName\":\"Alice\"}"
```
The output will look like below:
```bash
Running function for GCP
cmd-line json input is: {"objectName":"Alice"}
function result:
{"runtime":6362,"startTime":1669770949496,"endTime":1669770955858,"lang":"java","message":"Hello Alice! This is Function is to Upload content to google-cloud-storage","version":0.5,"value":"UWNetID/Alice.txt is created"}
```
### Check the files you uploaded to Google Cloud Storage
Replace `UWNetID` with your UW NetID(This is your uw email without “@uw.edu"). For example, My uw email is "dimo@uw.edu", so my UW NetID is “dimo”.
```bash
gcloud storage ls  gs://gcp-tutorial-562f22/UWNetID
```
For example, I run the command `gcloud storage ls  gs://gcp-tutorial-562f22/dimo`. This will list the files I created. The files `test1.txt` and `test.txt` are the two files I created.
```bash
gs://gcp-tutorial-562f22/dimo/test.txt
gs://gcp-tutorial-562f22/dimo/test1.txt
```

#### Optional: Test function locally for AWS
Open the downloaded folder. Put the provided `aws_credential.json`  file in to the src/main/resources folder. 

Now you can try to compile and run to test it.
Make sure you are in the project directory.
```bash
mvn clean -f pom.xml
mvn verify -f pom.xml
```

The UploadObject class can also upload data to the Amazon Simple Storage Service (S3). Now test running the function locally to upload data to Amazon S3:
```bash
# test the function for uploading to AWS Lambda
java -cp target/FaaSActivity-1.0-SNAPSHOT-jar-with-dependencies.jar functions.UploadObject AWS "{\"objectName\":\"Alice\"}"
```

check the file you uploaded in AWS S3. Replace `UWNetID` with your UW NetID.
```bash
aws s3 ls gcp.tutorial.562f22/UWNetID/
```

### Deploy functions to create file in Cloud Storage
Go to the project directory where you have cloned the project source code. Deploy the function from the terminal. Replace the `FUNCTIONNAME`with `UploadObjectUWNetID`. Replace `UWNetID` with yourUW NetID(This is your uw email without “@uw.edu"). For example, My uw email is "dimo@uw.edu", so my UW NetID is “dimo” and my function name should be `UploadObjectdimo`.
Run the following command to use gcloud to upload function to Cloud Functions.
```bash
gcloud functions deploy FUNCTIONNAME \
--entry-point functions.UploadObject \
--timeout 300 \
--runtime java11 --trigger-http 
```
It will take several minutes to upload. After you finish. you can try to use gcloud to call the uploaded function.
Replace the `FUNCTIONNAME` with your function name. you can change the `test` with some other name value.
```bash
gcloud functions call FUNCTIONNAME --data '{"objectName":"test"}'
```

Result will look similar like below. This means your function is working. Your file is uploaded to the Google Cloud Storage bucket.
```bash
executionId: ix8adchin8lw
result: '{"cpuType":"unknown","cpuNiceDelta":0,"vmuptime":1668499540,"cpuModel":"85","linuxVersion":"Linux
  localhost 4.4.0 #1 SMP Sun Jan 10 15:06:54 PST 2016 x86_64 x86_64 x86_64 GNU/Linux","cpuSoftIrqDelta":0,"cpuUsrDelta":0,"uuid":"c5bd4e3e-2f2f-4918-9f91-2ec1ee42cd0b","platform":"Unknown
  Platform","contextSwitches":0,"cpuKrn":0,"cpuIdleDelta":0,"cpuIowaitDelta":0,"newcontainer":1,"cpuNice":0,"startTime":1668499677976,"lang":"java","cpuUsr":0,"freeMemory":"201136","value":"MyCMD.txt
  is created","frameworkRuntime":399,"contextSwitchesDelta":0,"frameworkRuntimeDeltas":1,"vmcpusteal":0,"cpuKrnDelta":0,"cpuIdle":0,"runtime":12600,"message":"Hello
  MyCMD! This is Function is to Upload content to Google Cloud Storage","version":0.5,"cpuIrqDelta":0,"cpuIrq":0,"totalMemory":"262144","cpuCores":"0\nv","cpuSoftIrq":0,"cpuIowait":0,"endTime":1668499690577,"vmcpustealDelta":0,"userRuntime":12199}'
```

### Practice: Deploy a ReadObject function 
#### Deploy a function to read object from Cloud Storage
Please edit `src/main/java/functions/ReadObject.java` to read a file from Google Cloud Storage. Modify the variables `objectName`(line 46) and`projectID`(line 51). Write your code after Line 53. 

Name your function as `ReadObjectUWNetID` when upload your function to Google Cloud Functions, Replace `UWNetID` with your UW NetID(This is your uw email without “@uw.edu"). For example, My uw email is "dimo@uw.edu", so my UW NetID is “dimo”.
Below is code example of reading contents of a object from Google Cloud Storage. 
```java
Storage storage = StorageOptions.newBuilder().setProjectId(projectID)
        .setCredentials(GoogleCredentials.fromStream(Files.newInputStream(Paths.get("src/main/resources/gcp_credential.json"))))//put the provided json credential file path here.
        .build()
        .getService();
byte[] content = storage.readAllBytes(bucketName, objectName);
readContent = new String(content, StandardCharsets.UTF_8);
```
The input for the function should be 
```json
{"objectName": OBJECTNAME}
```

The output fot the function should be 
```json
{...,
  "value": "This is content read from ReadObject Function: " + OBJECTCONTENTS,
  "message": "Hello Alice! This is Function is to read content from google-cloud-storage",
  ...
}
```

### Optional
The ReadObject class can also be used to read content from Amazon S3. You can test the function locally to read data from Amazon S3
```bash
# test the function for uploading to AWS Lambda
java -cp target/FaaSActivity-1.0-SNAPSHOT-jar-with-dependencies.jar functions.ReadObject AWS "{\"objectName\":\"Alice\"}"
```
#### set your AWS CLI and deploy function to AWS Lambda
##### Back up your own AWS credentials
Deploying function to AWS in this section, you are using provided the aws credential. It is important to back up your own AWS credentials before going to next step.
You can run the following command for back up.
```bash
# backup AWS credentials
cp ~/.aws/credentials ~/.aws/credentials-backup
```
At the conclusion of the tutorial and activity, You can restore credentials with the commands:
```bash
# backup the tutorial 8 credentials by renaming the file
mv ~/.aws/credentials ~/.aws/credentials.tutorial8

# restore the original account credentials
mv ~/.aws/credentials-backup ~/.aws/credentials
```
##### AWS CLI set up
Open your terminal, run the following command.
```bash
aws configure
```
There will be prompts to ask you enter the Access Key ID and Secrete Access Key.
Enter the Access Key ID with the `identity` in `aws_credential.json` file.
Enter the Secrete Access Key with `credential` in `aws_credential.json` file.

#### Deploy upload a file function to AWS Lambda with AWS CLI
Replace the `FUNCTIONNAME`with `UploadObjectUWNetID`. Replace the `UWNetID` with your UW NetID.
Replace the `ROLE_NAME` with `role_name` in the provided JSON file. The role is created in advance with S3 Read and Write permission.
```bash
aws lambda create-function --function-name FUNCTIONNAME \
--zip-file fileb://target/FaaSActivity-1.0-SNAPSHOT-jar-with-dependencies.jar \
--handler functions.ReadObject::handleRequest --runtime java11 \
--role ROLE_NAME \
--timeout 300 \
--memory 512
```
It will take seconds to minutes to upload the lambda. Once it is finished. You can test it with AWS CLI. You can try different payload.You can check [aws cli invoke reference](https://docs.aws.amazon.com/cli/latest/reference/lambda/invoke.html) for more options.
```bash
aws lambda invoke --function-name FUNCTIONNAME --payload '{"objectName":"test"}' /dev/stdout
```

If you want to update codes to existing lambda. Use the following command to update your lambda. You can check [update function code](https://docs.aws.amazon.com/cli/latest/reference/lambda/update-function-code.html) for more options.
```bash
aws lambda update-function-code --function-name FUNCTIONNAME --zip-file fileb://target/FaaSActivity-1.0-SNAPSHOT-jar-with-dependencies.jar --handler functions.ReadObject::handleRequest
```
If you made some modification and wanted to update the function, run the following command. You can check [aws cli update function reference](https://docs.aws.amazon.com/cli/latest/reference/lambda/update-function-configuration.html) for more options.
Replace Modified handler with the handler you want to change. TimeOutNumber with the seconds you want to change.
```bash
aws lambda update-function-configuration --function-name FUNCTIONNAME --handler ModifiedHandler --timeout TimeOutNumber
```

## SECTION II: Migration Activity
### Objective
Migrate code for an image processing function written for AWS Lambda so it can be uploaded and made operational under Google Cloud Functions.
There are some CSV files already uploaded in AWS S3. These files contain image URLs we want to use.
The size of the original images is large. Our function will get the image from the object store URL, resize the image, and then upload the processed \(shrunk\) image as a new image file in AWS S3.
This function will process 3 images for each call. There are already CSV files stored in S3 and Google Cloud Storage which point to the image files.
### Completing code migration
You are going to complete this file `src/main/java/functions/ImageProcessing.java`. The `ImageProcessing` class is working for AWS. Your task is to make this function work in Google Cloud functions.
Write your code in the `service` method, after line 193. Write your code in the `imageProcessingProcedure` method, after line 86 and after line 135. Modify the variable `projectID` on line 83.
#### Input for the function
`startIndex` the start line of the csv to process.
Replace `UWNetID` with your UW Net ID
```json
{
  "objectName": "CSVFILENAME",
  "studentID": "UWNetID",
  "startIndex": 0
}
```
#### Results format.
```json
{successList=[true, true, true], startIndex=0, runtime=8130, startTime=1668555310558, endTime=1668555318688, lang=java, version=0.5, value=This Function is for Processing Image,....}
```

#### Buckets and filenames
Please set the provider,credentialFileName, csvBucket and imageBucket as below.
```java
        String credentialFileName="gcp_credential.json";
        String provider="google-cloud-storage";
        String csvBucket = "image-gcp-processing-csv"; // this is the bucket storing the csv file in Google Cloud Storage
        String imageBucket ="image-gcp-processing-images"; // this is the bucket storing the processed image in Google Cloud Storage
```
After completing your code, you can build and test your code locally. If it works, you can deploy your function to the cloud and test it with gcloud.
### Local test
```bash
java -cp target/FaaSActivity-1.0-SNAPSHOT-jar-with-dependencies.jar functions.ImageProcessing GCP "{\"objectName\":\"photo.csv\",\"studentID\":\"UWNetID\",\"startIndex\":0}" 
```
### Deploy function to Cloud Storage
We are going to deploy a function to upload image
Go to project directory. Deploy function from terminal. Replace the `FUNCTIONNAME`with `ImageProcessingUWNetID`. Replace `UWNetID` with your UW NetID(This is your uw email without “@uw.edu"). For example, My uw email is "dimo@uw.edu", so my UW NetID is “dimo”. Then the function name should be `ImageProcessingdimo`
Run the following command to use gcloud to upload function to Cloud Functions
```bash
gcloud functions deploy FUNCTIONNAME \
--entry-point functions.ImageProcessing \
--timeout 300 \
--runtime java11 --trigger-http 
```
It will take several minutes to upload. After you finish. you can try to use gcloud to call the uploaded function.
Replace the `FUNCTIONNAME` with your function name. you can change the `test` with some other name value.
```bash
gcloud functions call FUNCTIONNAME --data '{"objectName": "CSVFILENAME", "studentID": "UWNetID","startIndex": 0}'
```
### Check the resized image
Replace `UWNetID` with your UW NetID(This is your uw email without “@uw.edu"). For example, My uw email is "dimo@uw.edu", so my UW NetID is “dimo”.
```bash
gcloud storage ls  gs://image-gcp-processing-images/UWNetID/
```
### Check the csv files
Replace `UWNetID` with your UW NetID(This is your uw email without “@uw.edu"). For example, My uw email is "dimo@uw.edu", so my UW NetID is “dimo”.
```bash
gcloud storage ls  gs://image-gcp-processing-csv/
```
If you call the function successfully and see the files created in your folder, Congratulations you have successfully completed the migration task !!
