package com.spamdetector.service;

import com.spamdetector.domain.TestFile;
import com.spamdetector.util.SpamDetector;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.File;
import java.util.List;
import java.util.Objects;

import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/spam")
public class SpamResource {
    // SpamDetector Class responsible for all the SpamDetecting logic
    SpamDetector detector = new SpamDetector();
    List<TestFile> testFiles;

    public SpamResource() {
        System.out.print("Training and testing the model, please wait");
        testFiles = this.trainAndTest();
    }

    @GET
    @Produces("application/json")
    public Response getSpamResults() {
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "http://localhost:63342")
                .header("Content-Type", "application/json")
                .entity(this.toJSON(0, 0.0))
                .build();
    }

    @GET
    @Path("/accuracy")
    @Produces("application/json")
    public Response getAccuracy() {
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "http://localhost:63342")
                .header("Content-Type", "application/json")
                .entity(this.toJSON(1, this.calculateAccuracy()))
                .build();
    }

    @GET
    @Path("/precision")
    @Produces("application/json")
    public Response getPrecision() {
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "http://localhost:63342")
                .header("Content-Type", "application/json")
                .entity(this.toJSON(1, this.calculatePrecision()))
                .build();
    }

    private List<TestFile> trainAndTest()  {
        if (this.detector==null){
            this.detector = new SpamDetector();
        }

        File mainDirectory = new File(Objects.requireNonNull(getClass().getResource("/data")).getPath());
        return this.detector.trainAndTest(mainDirectory);
    }

    /**
     * if operation equals 0: turns this.testFiles into JSON format
     * if operation equals 1: turns value into JSON format
     * @return a formatted JSON file
     */
    private String toJSON(int operation, double value) {
        if (operation == 0) {
            JSONArray root = new JSONArray();

            for (TestFile testFile : testFiles) {
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("file", testFile.getFilename());
                jsonObject.put("spamProbability", testFile.getSpamProbability());
                jsonObject.put("actualClass", testFile.getActualClass());

                root.put(jsonObject);
            }

            return root.toString();
        } else if (operation == 1) {
            JSONObject root = new JSONObject();

            root.put("val", value);

            return root.toString();
        }

        return new JSONObject().put("Error", "Invalid operation or value").toString();
    }

    private double calculateAccuracy() {
        int numTruePositives = 0;
        int numTrueNegatives = 0;

        for (TestFile testFile : testFiles) {
            if (testFile.getActualClass().equals("spam")) {
                if (testFile.getSpamProbability() >= 0.5) {
                    numTruePositives++;
                }
            } else {
                if (!(testFile.getSpamProbability() >= 0.5)) {
                    numTrueNegatives++;
                }
            }
        }

        int numCorrectGuesses = numTruePositives + numTrueNegatives;

        return (double) numCorrectGuesses / testFiles.size();
    }

    private double calculatePrecision() {
        int numTruePositives = 0;
        int numFalsePositives = 0;

        for (TestFile testFile : testFiles) {
            if (testFile.getActualClass().equals("spam")) {
                if (testFile.getSpamProbability() >= 0.5) {
                    numTruePositives++;
                }
            } else {
                if (testFile.getSpamProbability() >= 0.5) {
                    numFalsePositives++;
                }
            }
        }

        if (numFalsePositives + numTruePositives > 0) {
            return (double) numTruePositives / (numFalsePositives + numTruePositives);
        }

        return 0.0;
    }
}
