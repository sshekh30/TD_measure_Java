package dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import model.*;

public class FileResultStorageDAOImpl implements ResultStorageDAO {

    public FileResultStorageDAOImpl() {
    }

    private ObjectMapper json;
    private String filePath;

    public FileResultStorageDAOImpl(String filePath) {
        this.filePath = filePath;
        this.json = new ObjectMapper();
    }

    public void writeEntropy(SessionEntropyData sessionEntropyData)
            throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        String jsonLine = json.writeValueAsString(sessionEntropyData);

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(jsonLine + "\n");
        }
    }

    public void writeTeamDynamics(String sessionID,String scenarioID, Map<String, List<Object>> teamDynamicsMap)
            throws IOException {

        String dynamicsFilePath = filePath.replace(".jsonl", "_dynamics.jsonl");
        File file = new File(dynamicsFilePath);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        Map<String, Object> record = new java.util.LinkedHashMap<>();
        record.put("sessionID", sessionID);
        record.put("scenarioID", scenarioID);
        record.put("teamDynamics", teamDynamicsMap);

        String jsonLine = json.writeValueAsString(record);

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(jsonLine + "\n");
        }

        System.out.println("Team Dynamics results written to: " + dynamicsFilePath);
    }

    public SessionEntropyData readEntropy(String sessionID) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        for (String line : lines) {
            SessionEntropyData data = json.readValue(
                    line,
                    SessionEntropyData.class
            );
            if (data.getSessionID().equals(sessionID)) {
                return data;
            }
        }
        return null;
    }

    public Map<String, List<Object>> readTeamDynamics(String sessionID) throws IOException {

        String dynamicsFilePath = filePath.replace(".jsonl", "_dynamics.jsonl");
        File file = new File(dynamicsFilePath);

        if (!file.exists()) {
            return null;
        }

        List<String> lines = Files.readAllLines(Paths.get(dynamicsFilePath));

        for (String line : lines) {
            @SuppressWarnings("unchecked")
            Map<String, Object> wrapperData = json.readValue(line, Map.class);

            if (wrapperData.containsKey("sessionID")
                    && wrapperData.get("sessionID").equals(sessionID)) {

                if (wrapperData.containsKey("teamDynamics")) {
                    @SuppressWarnings("unchecked")
                    Map<String, List<Object>> teamDynamics = (Map<String, List<Object>>) wrapperData.get("teamDynamics");
                    return teamDynamics;
                }
            }
        }

        return null; 
    }
}
