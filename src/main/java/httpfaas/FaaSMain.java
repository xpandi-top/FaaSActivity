package httpfaas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import basic.Request;

import java.io.BufferedWriter;
import java.util.HashMap;

public class FaaSMain {
    public static <T extends HttpFunction & RequestHandler<Request, HashMap<String, Object>>> void output(String[] args, T faasObject, Gson gson) throws Exception {
        if (args.length < 2) throw new RuntimeException("Please provide the provider and the payload for function");
        String provider = args[0];
        String content = args[1];
        System.out.println("Running function for " + provider);
        System.out.println("cmd-line json input is: " + content);
        // For Google Cloud Platform
        if (provider.equals("GCP")) {
            System.out.println("function result:");
            HttpRequest httpRequest = new FaaSHttpRequest(content);
            HttpResponse httpResponse = new FaaSHttpResponse();
            BufferedWriter bufferedWriter = httpResponse.getWriter();
            faasObject.service(httpRequest, httpResponse);
            bufferedWriter.flush();
        } else if (provider.equals("AWS")) {
            System.out.println("function result:");
            Request request = gson.fromJson(content, Request.class);
            Context context = new FaaSLambdaContext();
            System.out.println(faasObject.handleRequest(request, context));
        }
    }
}
