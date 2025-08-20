package extraction;
import dao.*;

import java.io.*;
import java.util.*;


//TODO: implement casualty, visual extraction logic
public class FieldExtractor {
    
    private DataSourceDAO dataSource;

    public FieldExtractor(DataSourceDAO dataSource) {
        this.dataSource = dataSource;
    }

    public static String extractArrayPresence(String jsonSection, String arrayName) {
        return jsonSection.contains("\"" + arrayName + "\":[]") ? "0" : "1";
    }

    public static String[] extractTraineeObject(String jsonObject, String fieldName) {
        int traineeStart = jsonObject.indexOf("\"trainees\":");
        if (traineeStart == -1) return new String[]{""};
        String traineesSection = jsonObject.substring(traineeStart);
        String[] traineeObjects = traineesSection.split("\"" + fieldName + "\":");
        return traineeObjects;
    } 

    // public static List<List<String>> extractCasualty(String jsonObject) {
    //     <List<List<String>> casualty = new ArrayList<>();
    //     String[] traineeObjects = extractTraineeObject(jsonObject, "")
    //     return casualty;

    // }


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

    public List<List<List<String>>> extractSTTCLayers() throws IOException {
        // List<String> lines = Files.readAllLines(Paths.get(fileName));
        List<String> lines = dataSource.readData();

        List<List<List<String>>> sttcLayers = new ArrayList<>();

        List<List<String>> visual = new ArrayList<>();
        List<List<String>> comm = new ArrayList<>();
        for (String line: lines) {
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