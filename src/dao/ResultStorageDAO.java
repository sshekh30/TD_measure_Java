package dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import model.*;

public interface ResultStorageDAO {

    public void writeEntropy(SessionEntropyData sessionEntropyData)
            throws IOException;

    public SessionEntropyData readEntropy(String sessionID) throws IOException;

    public void writeTeamDynamics(String sessionID,String scenarioID, Map<String, List<Object>> teamDynamics)
            throws IOException;
    
    public Map<String, List<Object>> readTeamDynamics(String sessionID) throws IOException;
}
