package teamDynamics;

import java.util.List;
import java.util.Map;

import model.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DynamicsCalculator {

    private double[][] prepareEntropyMatrix(Map<EntropyLayer, double[]> sessionEntropyMap) {

        // Define the required keys using the ENUM CONSTANTS
        EntropyLayer keyTrainee1 = EntropyLayer.TRAINEE1;
        EntropyLayer keyTrainee2 = EntropyLayer.TRAINEE2;
        EntropyLayer keyTrainee3 = EntropyLayer.TRAINEE3;
        EntropyLayer keyTeam = EntropyLayer.TEAM;

        // 💥 FIX: Check containment using the ENUM constant 💥
        if (sessionEntropyMap.isEmpty() || !sessionEntropyMap.containsKey(keyTrainee1)) {
            // Return 0x4 matrix for cleaner handling by subsequent code
            return new double[0][4];
        }

        // Get total time using the TRAINEE1 key
        double[] trainee1Array = sessionEntropyMap.get(keyTrainee1);
        int totalTime = trainee1Array.length;

        // Matrix columns: T1, T2, T3, Team
        double[][] dataMatrix = new double[totalTime][4];

        for (int t = 0; t < totalTime; t++) {
            // 💥 FIX: Access data using the ENUM constant 💥
            dataMatrix[t][0] = sessionEntropyMap.get(keyTrainee1)[t];
            dataMatrix[t][1] = sessionEntropyMap.get(keyTrainee2)[t];
            dataMatrix[t][2] = sessionEntropyMap.get(keyTrainee3)[t];
            dataMatrix[t][3] = sessionEntropyMap.get(keyTeam)[t];
        }

        return dataMatrix;
    }

    private List<int[]> prepareCommunicationMatrix(List<List<List<String>>> layers) {
        List<int[]> communicationMatrix = new ArrayList<>();
        int commLayerIndex = 0;

        for (List<List<String>> timeInstance : layers) {
            List<String> commCodes = timeInstance.get(commLayerIndex);

            if (commCodes.size() >= 3) {
                int[] codes = new int[commCodes.size()];

                try {
                    for (int i = 0; i < commCodes.size(); i++) {
                        codes[i] = Integer.parseInt(commCodes.get(i));
                    }
                    communicationMatrix.add(codes);

                } catch (NumberFormatException e) {
                    System.err.println("Error parsing communication code to integer: " + e.getMessage());
                }
            }
        }
        return communicationMatrix;
    }

    private static Map<String, List<Object>> combineFinalResults(
            Map<String, int[]> Concepts,
            Map<String, Double> Influence
    ) {
        Map<String, List<Object>> finalResults = new LinkedHashMap<>();

        for (Map.Entry<String, int[]> conceptEntry : Concepts.entrySet()) {
            String subject = conceptEntry.getKey();
            int[] conceptValues = conceptEntry.getValue();
            List<Object> metrics = new ArrayList<>();

            if (conceptValues != null) {
                for (int val : conceptValues) {
                    metrics.add(val);
                }
            } else {
                metrics.add(null);
                metrics.add(null);
                metrics.add(null);
            }
            if (Influence.containsKey(subject)) {
                metrics.add(Influence.get(subject));
            } else {
                metrics.add(null);
            }

            finalResults.put(subject, metrics);
        }

        return finalResults;
    }

    public Map<String, List<Object>> calculateDynamics(Map<EntropyLayer, double[]> sessionEntropyMap,
            List<List<List<String>>> layers) {

        double[][] entropyMatrix = prepareEntropyMatrix(sessionEntropyMap);
        Map<String, int[]> Concepts = EntropyProcessor.processEntropyData(entropyMatrix);

        List<int[]> commListMatrix = prepareCommunicationMatrix(layers);
        int[][] communicationData = commListMatrix.toArray(new int[0][]);
        Map<String, Double> Influence = InfluenceProcessor.processCommunicationData(communicationData);

        Map<String, List<Object>> finalResults = combineFinalResults(Concepts, Influence);

        return finalResults;

    }
}
