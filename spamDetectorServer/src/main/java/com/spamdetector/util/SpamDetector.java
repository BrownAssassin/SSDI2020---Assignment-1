package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.util.*;

/**
 * This class is responsible for detecting spam emails
 */
public class SpamDetector {
    private final Map<String, Double> spamFileProb;
    private final Map<String, Double> spamWordProb;
    private final Map<String, Double> hamWordProb;
    private final Map<String, Integer> trainHamFreq;
    private final Map<String, Integer> trainSpamFreq;
    private int numTrainHamFiles;
    private int numTrainSpamFiles;

    public SpamDetector() {
        spamFileProb = new TreeMap<>();
        spamWordProb = new HashMap<>();
        hamWordProb = new HashMap<>();
        trainHamFreq = new HashMap<>();
        trainSpamFreq = new HashMap<>();
    }

    public List<TestFile> trainAndTest(File mainDirectory) {
        // Load the training data
        loadTrainingData(mainDirectory);

        // Calculate the probabilities
        calculateProbabilities();

        //Test the model and return results
        return testModel(new File(mainDirectory, "test"));
    }

    /**
     * Loads the training data from the given directory
     */
    private void loadTrainingData(File mainDirectory) {
        File[] hamFiles = new File(mainDirectory, "/train/ham").listFiles();
        File[] ham2Files = new File(mainDirectory, "/train/ham2").listFiles();
        File[] spamFiles = new File(mainDirectory, "/train/spam").listFiles();

        // Count the number of files
        assert hamFiles != null;
        assert ham2Files != null;
        assert spamFiles != null;
        numTrainHamFiles = hamFiles.length + ham2Files.length;
        numTrainSpamFiles = spamFiles.length;

        // Process ham files
        for (File file : hamFiles) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                Set<String> words = new HashSet<>();
                while ((line = reader.readLine()) != null) {
                    words.addAll(Arrays.asList(line.split("\\W+")));
                }
                for (String word : words) {
                    word = word.toLowerCase();
                    if (!trainHamFreq.containsKey(word)) {
                        trainHamFreq.put(word, 1);
                    } else {
                        trainHamFreq.put(word, trainHamFreq.get(word) + 1);
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (File file : ham2Files) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                Set<String> words = new HashSet<>();
                while ((line = reader.readLine()) != null) {
                    words.addAll(Arrays.asList(line.split("\\W+")));
                }
                for (String word : words) {
                    word = word.toLowerCase();
                    if (!trainHamFreq.containsKey(word)) {
                        trainHamFreq.put(word, 1);
                    } else {
                        trainHamFreq.put(word, trainHamFreq.get(word) + 1);
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Process spam files
        for (File file : spamFiles) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                Set<String> words = new HashSet<>();
                while ((line = reader.readLine()) != null) {
                    words.addAll(Arrays.asList(line.split("\\W+")));
                }
                for (String word : words) {
                    word = word.toLowerCase();
                    if (!trainSpamFreq.containsKey(word)) {
                        trainSpamFreq.put(word, 1);
                    } else {
                        trainSpamFreq.put(word, trainSpamFreq.get(word) + 1);
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculates the probabilities for each word
     * Note: Uses Laplace smoothing to handle zero probabilities
     */
    private void calculateProbabilities() {
        for (String word : trainSpamFreq.keySet()) {
            double probability = (double) (trainSpamFreq.get(word) + 1) / (numTrainSpamFiles + 2);
            spamWordProb.put(word, probability);
        }

        for (String word : trainHamFreq.keySet()) {
            double probability = (double) (trainHamFreq.get(word) + 1) / (numTrainHamFiles + 2);
            hamWordProb.put(word, probability);
        }

        for (String word : spamWordProb.keySet()) {
            double probability =
                    spamWordProb.get(word) / (spamWordProb.get(word) + hamWordProb.getOrDefault(word, 0.0));
            spamFileProb.put(word, probability);
        }
    }

    /**
     * Tests the model on the given test directory
     */
    private ArrayList<TestFile> testModel(File testDirectory) {
        ArrayList<TestFile> testResults = new ArrayList<>();

        File[] hamFiles = new File(testDirectory, "ham").listFiles();
        assert hamFiles != null;
        for (File file : hamFiles) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                double n = 0.0;
                Set<String> words = new HashSet<>();
                while ((line = reader.readLine()) != null) {
                    words.addAll(Arrays.asList(line.split("\\W+")));
                }
                for (String word : words) {
                    word = word.toLowerCase();
                    if (spamFileProb.containsKey(word)) {
                        n += Math.log(1 - spamFileProb.get(word)) - Math.log(spamFileProb.get(word));
                    }
                }
                reader.close();

                double spamProbability = 1.0 / (1.0 + Math.exp(n));
                testResults.add(new TestFile(file.getName(), spamProbability, "Ham"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File[] spamFiles = new File(testDirectory, "spam").listFiles();
        assert spamFiles != null;
        for (File file : spamFiles) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                double n = 0.0;
                Set<String> words = new HashSet<>();
                while ((line = reader.readLine()) != null) {
                    words.addAll(Arrays.asList(line.split("\\W+")));
                }
                for (String word : words) {
                    word = word.toLowerCase();
                    if (spamFileProb.containsKey(word)) {
                        n += Math.log(1 - spamFileProb.get(word)) - Math.log(spamFileProb.get(word));
                    }
                }
                reader.close();

                double spamProbability = 1.0 / (1.0 + Math.exp(n));
                testResults.add(new TestFile(file.getName(), spamProbability, "Spam"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return testResults;
    }
}
