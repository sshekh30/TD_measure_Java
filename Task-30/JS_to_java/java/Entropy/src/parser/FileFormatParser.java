package parser;

import java.io.*;
import java.util.*;

public class FileFormatParser implements DataParser {

    private enum VisualCat {
        ROI, OOI, CASUALTY, TRAINEE, NONE
    }

    private static final VisualCat[] VISUAL_ORDER = {
        VisualCat.OOI, VisualCat.ROI, VisualCat.TRAINEE, VisualCat.CASUALTY
    };

    private static String bits(VisualCat cat) {
        if (cat == VisualCat.NONE) {
            return "0000";
        }
        StringBuilder sb = new StringBuilder(4);
        for (VisualCat c : VISUAL_ORDER) {
            sb.append(c == cat ? '1' : '0');
        }
        return sb.toString();
    }

    private static final String KEY_ROI = "\"ROI_Watched\"";
    private static final String KEY_OOI = "\"OOI_Watched\"";
    private static final String KEY_CAS = "\"Casualty_Watched\"";
    private static final String KEY_TRA = "\"Trainee_Watched\"";

    // private static String extractArrayPresence(String jsonSection, String arrayName) {
    //     return jsonSection.contains("\"" + arrayName + "\":[]") ? "0" : "1";
    // }

    private static String[] extractTraineeObject(String jsonObject, String fieldName) {
        int traineeStart = jsonObject.indexOf("\"trainees\":");
        if (traineeStart == -1) {
            return new String[]{""};
        }
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
            String v = traineeObjects[i];

            VisualCat cat
                    = v.contains(KEY_ROI) ? VisualCat.ROI
                    : v.contains(KEY_OOI) ? VisualCat.OOI
                    : v.contains(KEY_CAS) ? VisualCat.CASUALTY
                    : v.contains(KEY_TRA) ? VisualCat.TRAINEE
                    : VisualCat.NONE;

            visual.add(bits(cat));
        }
        return visual;
    }

    @Override
    public List<List<List<String>>> parseToSTTCLayers(List<String> rawData) throws IOException {
        List<List<List<String>>> sttcLayers = new ArrayList<>();

        List<List<String>> visual = new ArrayList<>();
        List<List<String>> comm = new ArrayList<>();
        for (String line : rawData) {
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