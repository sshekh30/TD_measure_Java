package config;

import dao.*;

public class ResultStorageFactory {

    public static ResultStorageDAO createResultStorage(
        ConfigManager config,
        String sourceType
    ) {
        switch (sourceType.toLowerCase()) {
            case "file":
                return new FileResultStorageDAOImpl("results/result.jsonl");
            // case "mongo":
            //     return new MongoResultStorageDAOImpl();
            default:
                throw new IllegalArgumentException(
                    "Unknown data source type: " + sourceType
                );
        }
    }
}
