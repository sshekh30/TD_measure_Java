package dao;

import java.io.IOException;
import model.*;

public interface ResultStorageDAO {
    public void writeResults(SessionEntropyData sessionEntropyData)
        throws IOException;

    public SessionEntropyData readResults(String sessionID) throws IOException;
}
