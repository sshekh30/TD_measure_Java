package dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import model.*;

public class MongoResultStorageDAOImpl implements ResultStorageDAO {

    private final ObjectMapper json;
    private final String mongoUri;
    private final String mongoDatabase;
    private final String mongoCollection;

    private final String ENTROPY_COLLECTION_SUFFIX = "-entropy";
    private final String DYNAMICS_COLLECTION_SUFFIX = "-dynamics";

    public MongoResultStorageDAOImpl(
        String mongoUri,
        String mongoDatabase,
        String mongoCollection
    ) {
        this.mongoUri = mongoUri;
        this.mongoDatabase = mongoDatabase;
        this.mongoCollection = mongoCollection;
        this.json = new ObjectMapper();
    }

    private MongoCollection<Document> getCollection(String suffix) {
        MongoClient mongoClient = MongoClients.create(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoDatabase);
        return db.getCollection(mongoCollection + suffix);
    }
    
    @Override
    public void writeEntropy(SessionEntropyData sessionEntropyData)
            throws IOException {
        
        String collectionName = mongoCollection + ENTROPY_COLLECTION_SUFFIX;
        
        try {
            String jsonString = json.writeValueAsString(sessionEntropyData);
            
            MongoCollection<Document> collection = getCollection(ENTROPY_COLLECTION_SUFFIX);
            Document doc = Document.parse(jsonString);
            collection.insertOne(doc);
            System.out.println("MongoDB: Successfully wrote SessionEntropyData to collection: " + collectionName);
            
        } catch (Exception e) {
            throw new IOException("MongoDB Error writing entropy data: " + e.getMessage(), e);
        }
    }

    @Override
    public void writeTeamDynamics(String sessionID, Map<String, List<Object>> teamDynamics)
            throws IOException {
        
        String collectionName = mongoCollection + DYNAMICS_COLLECTION_SUFFIX;
        
        try {
            Map<String, Object> record = new java.util.LinkedHashMap<>();
            record.put("sessionID", sessionID);
            record.put("teamDynamics", teamDynamics); 

            String jsonString = json.writeValueAsString(record);

            MongoCollection<Document> collection = getCollection(DYNAMICS_COLLECTION_SUFFIX);
            Document doc = Document.parse(jsonString);
            collection.insertOne(doc);
            System.out.println("MongoDB: Successfully wrote Team Dynamics to collection: " + collectionName);
            
        } catch (Exception e) {
            throw new IOException("MongoDB Error writing team dynamics: " + e.getMessage(), e);
        }
    }


    @Override
    public SessionEntropyData readEntropy(String sessionID) throws IOException {
        
        String collectionName = mongoCollection + ENTROPY_COLLECTION_SUFFIX;
        
        try {
            MongoCollection<Document> collection = getCollection(ENTROPY_COLLECTION_SUFFIX);
            Document query = new Document("sessionID", sessionID);
            Document result = collection.find(query).first();

            if (result != null) {
                String jsonString = result.toJson();
                return json.readValue(jsonString, SessionEntropyData.class);
            }
            System.out.println("MongoDB: Read attempt for SessionEntropyData from collection: " + collectionName);
            return null; 
            
        } catch (Exception e) {
            throw new IOException("MongoDB Error reading entropy data: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, List<Object>> readTeamDynamics(String sessionID) throws IOException {
        
        String collectionName = mongoCollection + DYNAMICS_COLLECTION_SUFFIX;
        
        try {
            MongoCollection<Document> collection = getCollection(DYNAMICS_COLLECTION_SUFFIX);
            Document query = new Document("sessionID", sessionID);
            Document result = collection.find(query).first();

            if (result != null) {
                String jsonString = result.toJson();
                @SuppressWarnings("unchecked")
                Map<String, Object> wrapperData = json.readValue(jsonString, Map.class);
                
                if (wrapperData.containsKey("teamDynamics")) {
                    @SuppressWarnings("unchecked")
                    Map<String, List<Object>> teamDynamics = (Map<String, List<Object>>) wrapperData.get("teamDynamics");
                    return teamDynamics;
                }
            }
            System.out.println("MongoDB: Read attempt for Team Dynamics from collection: " + collectionName);
            return null; 
            
        } catch (Exception e) {
            throw new IOException("MongoDB Error reading team dynamics: " + e.getMessage(), e);
        }
    }
}