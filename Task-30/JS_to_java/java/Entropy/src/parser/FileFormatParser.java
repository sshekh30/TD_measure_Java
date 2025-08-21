package parser;

import java.io.*;
import java.util.*;

public class FileFormatParser implements DataParser {

    private static String extractArrayPresence(String jsonSection, String arrayName) {
        return jsonSection.contains("\"" + arrayName + "\":[]") ? "0" : "1";
    }

    private static String[] extractTraineeObject(String jsonObject, String fieldName) {
        int traineeStart = jsonObject.indexOf("\"trainees\":");
        if (traineeStart == -1) return new String[]{""};
        String traineesSection = jsonObject.substring(traineeStart);
        String[] traineeObjects = traineesSection.split("\"" + fieldName + "\":");
        return traineeObjects;
    }

    public static List<String> extractCommunication(String jsonObject) {
        List<String> comms = new ArrayList<>();
        String[] traineeObjects = extractTraineeObject(jsonObject, "Communication");
        for (int i = 1; i < traineeObjects.length; i++) {
            String afterComm = traineeObjects[i].toLowerCase().trim();
            boolean value = afterComm.startsWith("true");
            comms.add(value ? "1" : "0");
        }
        return comms;
    }

    public static List<String> extractVisual(String jsonObject) {
        List<String> visual = new ArrayList<>();
        String[] traineeObjects = extractTraineeObject(jsonObject, "VisualActivity");
        for (int i = 1; i < traineeObjects.length; i++) {
            String visualLayer = traineeObjects[i];
            
            String isWatchingAOI = extractArrayPresence(visualLayer, "AOI_Watched");
            String isWatchingOOI = extractArrayPresence(visualLayer, "OOI_Watched");
            String isWatchingROI = extractArrayPresence(visualLayer, "ROI_Watched");


            visual.add(isWatchingROI + isWatchingAOI + isWatchingOOI);

        }
        return visual;
    }

    @Override 
    public List<List<List<String>>> parseToSTTCLayers(List<String> rawData) throws IOException {
        List<List<List<String>>> sttcLayers = new ArrayList<>();

        List<List<String>> visual = new ArrayList<>();
        List<List<String>> comm = new ArrayList<>();
        for (String line: rawData) {
            List<List<String>> dataFrame = new ArrayList<>();
            List<String> traineeComm = extractCommunication(line);
            List<String> traineeViz = extractVisual(line);
            comm.add(traineeComm);
            visual.add(traineeViz);
            dataFrame.add(traineeComm);
            dataFrame.add(traineeViz);
            sttcLayers.add(dataFrame);
        }

        System.out.println("======Visual Layer: ======");
        System.out.println(visual);
        
        System.out.println("======Communication Layer: ======\n");
        System.out.println(comm);
        

        return sttcLayers;
    }
}