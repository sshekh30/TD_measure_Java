package dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import model.*;

public class FileResultStorageDAOImpl implements ResultStorageDAO {

    public FileResultStorageDAOImpl() {}

    private ObjectMapper json;
    private String filePath;

    public FileResultStorageDAOImpl(String filePath) {
        this.filePath = filePath;
        this.json = new ObjectMapper();
    }

    public void writeResults(SessionEntropyData sessionEntropyData)
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

    public SessionEntropyData readResults(String sessionID) throws IOException {
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
}
