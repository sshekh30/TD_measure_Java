package service;

import config.*;
import dao.*;
import entropy.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.*;
import parser.*;
import teamDynamics.DynamicsCalculator;

public class SessionEntropyService {

    private ConfigManager defaultConfig;
    private ResultStorageDAO defaultResultStorage;

    public SessionEntropyService(ConfigManager configManager) {
        this.defaultConfig = configManager;
        this.defaultResultStorage = ResultStorageFactory.createResultStorage(
                defaultConfig,
                defaultConfig.getDataSourceType()
        );
    }

    private static String[][][] convertToArray(
            List<List<List<String>>> listData
    ) {
        String[][][] arrayData = new String[listData.size()][][];
        for (int i = 0; i < listData.size(); i++) {
            List<List<String>> timePoint = listData.get(i);
            arrayData[i] = new String[timePoint.size()][];
            for (int j = 0; j < timePoint.size(); j++) {
                List<String> layer = timePoint.get(j);
                arrayData[i][j] = layer.toArray(new String[0]);
            }
        }
        return arrayData;
    }

    private String[][][] sliceLayerData(
            String[][][] layerData,
            int startIdx,
            int endIdx
    ) {
        int sliceLength = endIdx - startIdx;
        String[][][] sliced = new String[sliceLength][][];

        for (int i = 0; i < sliceLength; i++) {
            sliced[i] = layerData[startIdx + i];
        }

        return sliced;
    }

    private int determineEndIdx(
            Map<String, Integer> allIds,
            String currentId,
            int startIdx,
            int totalLength
    ) {
        int nextStartIdx = totalLength;

        for (Integer idx : allIds.values()) {
            if (idx > startIdx && idx < nextStartIdx) {
                nextStartIdx = idx;
            }
        }

        return nextStartIdx;
    }

    public SessionEntropyData getEntireSessionEntropy(String sessionId)
            throws IOException {
        return defaultResultStorage.readEntropy(sessionId);
    }

    public Map<String,List<Object>> getEntireSessionTeamDynamics(String sessionId)
            throws IOException {
        return defaultResultStorage.readTeamDynamics(sessionId);
    }

    public EntropyObject getEntropyForScenario(
            String sessionId,
            String scenarioId
    ) throws IOException {
        SessionEntropyData data = defaultResultStorage.readEntropy(sessionId);
        if (data == null) {
            return null;
        }
        return data.getScenarioEntropy().get(scenarioId);
    }

    public EntropyObject getEntropyForPerturbation(
            String sessionId,
            String pertubationId
    ) throws IOException {
        SessionEntropyData data = defaultResultStorage.readEntropy(sessionId);
        if (data == null) {
            return null;
        }
        return data.getPertubationEntropy().get(pertubationId);
    }

    public EntropyObject getEntropyAtTime(String sessionId, int time)
            throws IOException {
        SessionEntropyData data = defaultResultStorage.readEntropy(sessionId);
        if (data == null) {
            return null;
        }

        Map<EntropyLayer, double[]> sliced = new HashMap<>();
        for (Map.Entry<EntropyLayer, double[]> entry : data
                .getSession_entropy()
                .getLayerEntropies()
                .entrySet()) {
            double[] values = entry.getValue();
            if (time < values.length) {
                sliced.put(entry.getKey(), new double[]{values[time]});
            }
        }
        return new EntropyObject(sliced);
    }

    public EntropyObject getEntropyInTimeRange(
            String sessionId,
            int from,
            int to
    ) throws IOException {
        SessionEntropyData data = defaultResultStorage.readEntropy(sessionId);
        if (data == null) {
            return null;
        }

        Map<EntropyLayer, double[]> sliced = new HashMap<>();
        for (Map.Entry<EntropyLayer, double[]> entry : data
                .getSession_entropy()
                .getLayerEntropies()
                .entrySet()) {
            double[] values = entry.getValue();
            int length = Math.min(to, values.length) - from;
            if (length > 0) {
                double[] range = new double[length];
                System.arraycopy(values, from, range, 0, length);
                sliced.put(entry.getKey(), range);
            }
        }
        return new EntropyObject(sliced);
    }

    public void CalculateEntropy(String sessionID, String dataSourceType)
            throws IOException {
        if (dataSourceType == null || dataSourceType.isEmpty()) {
            dataSourceType = defaultConfig.getDataSourceType();
        }

        DataSourceDAO dataSource = DataSourceFactory.createDataSource(
                defaultConfig,
                dataSourceType
        );
        DataParser parser = ParserFactory.createParser(dataSourceType);
        ResultStorageDAO resultStorageDAO
                = ResultStorageFactory.createResultStorage(
                        defaultConfig,
                        dataSourceType
                );

        List<String> rawData = dataSource.readData(sessionID);
        List<List<List<String>>> layers = parser.parseToSTTCLayers(rawData);
        SessionMetadata sessionMetadata = parser.getSessionMetadata();
        String[][][] layerData = convertToArray(layers);

        Map<EntropyLayer, double[]> sessionEntropyMap = new HashMap<>();

        sessionEntropyMap.put(
                EntropyLayer.COMMUNICATION,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new LayerAggregationStrategy(0)
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.VISUAL,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new LayerAggregationStrategy(1)
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.CASUALTY,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new LayerAggregationStrategy(2)
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.MOVEMENT,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new LayerAggregationStrategy(3)
                )
        );

        sessionEntropyMap.put(
                EntropyLayer.TRAINEE1,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new IndividualEntityAggregationStrategy(
                                new int[]{0, 1, 3},
                                0
                        )
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.TRAINEE2,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new IndividualEntityAggregationStrategy(
                                new int[]{0, 1, 3},
                                1
                        )
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.TRAINEE3,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new IndividualEntityAggregationStrategy(
                                new int[]{0, 1, 3},
                                2
                        )
                )
        );

        sessionEntropyMap.put(
                EntropyLayer.SYSTEM,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new CombinedLayerAggregationStrategy(new int[]{0, 1, 2, 3})
                )
        );
        sessionEntropyMap.put(
                EntropyLayer.TEAM,
                GeneralizedEntropyCalculator.computeWindowedEntropy(
                        layerData,
                        4,
                        new CombinedLayerAggregationStrategy(new int[]{0, 3})
                )
        );

        Map<String, EntropyObject> scenarioEntropyMap = new HashMap<>();
        for (Map.Entry<String, Integer> scenario : sessionMetadata
                .getScenarioIDs()
                .entrySet()) {
            String scenarioId = scenario.getKey();
            int startIdx = scenario.getValue();
            int endIdx = determineEndIdx(
                    sessionMetadata.getScenarioIDs(),
                    scenarioId,
                    startIdx,
                    layerData.length
            );

            Map<EntropyLayer, double[]> scenarioLayers = new HashMap<>();
            String[][][] slicedData = sliceLayerData(
                    layerData,
                    startIdx,
                    endIdx
            );

            scenarioLayers.put(
                    EntropyLayer.COMMUNICATION,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(0)
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.VISUAL,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(1)
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.CASUALTY,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(2)
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.MOVEMENT,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(3)
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.TRAINEE1,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    0
                            )
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.TRAINEE2,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    1
                            )
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.TRAINEE3,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    2
                            )
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.SYSTEM,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new CombinedLayerAggregationStrategy(
                                    new int[]{0, 1, 2, 3}
                            )
                    )
            );
            scenarioLayers.put(
                    EntropyLayer.TEAM,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new CombinedLayerAggregationStrategy(new int[]{0, 3})
                    )
            );

            scenarioEntropyMap.put(
                    scenarioId,
                    new EntropyObject(scenarioLayers)
            );
        }

        Map<String, EntropyObject> perturbationEntropyMap = new HashMap<>();
        for (Map.Entry<String, Integer> perturbation : sessionMetadata
                .getPertubationIDs()
                .entrySet()) {
            String perturbationId = perturbation.getKey();
            int startIdx = perturbation.getValue();
            int endIdx = determineEndIdx(
                    sessionMetadata.getPertubationIDs(),
                    perturbationId,
                    startIdx,
                    layerData.length
            );

            Map<EntropyLayer, double[]> perturbationLayers = new HashMap<>();
            String[][][] slicedData = sliceLayerData(
                    layerData,
                    startIdx,
                    endIdx
            );

            perturbationLayers.put(
                    EntropyLayer.COMMUNICATION,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(0)
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.VISUAL,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(1)
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.CASUALTY,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(2)
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.MOVEMENT,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new LayerAggregationStrategy(3)
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.TRAINEE1,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    0
                            )
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.TRAINEE2,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    1
                            )
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.TRAINEE3,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new IndividualEntityAggregationStrategy(
                                    new int[]{0, 1, 3},
                                    2
                            )
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.SYSTEM,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new CombinedLayerAggregationStrategy(
                                    new int[]{0, 1, 2, 3}
                            )
                    )
            );
            perturbationLayers.put(
                    EntropyLayer.TEAM,
                    GeneralizedEntropyCalculator.computeWindowedEntropy(
                            slicedData,
                            4,
                            new CombinedLayerAggregationStrategy(new int[]{0, 3})
                    )
            );

            perturbationEntropyMap.put(
                    perturbationId,
                    new EntropyObject(perturbationLayers)
            );
        }

        EntropyObject sessionEntropy = new EntropyObject(sessionEntropyMap);

        DynamicsCalculator dynamicsFacade = new DynamicsCalculator();
        Map<String, List<Object>> teamDynamics = dynamicsFacade.calculateDynamics(sessionEntropyMap, layers);

        SessionEntropyData sessionEntropyData = new SessionEntropyData(
                sessionID,
                sessionEntropy,
                scenarioEntropyMap,
                perturbationEntropyMap
        );
        resultStorageDAO.writeEntropy(sessionEntropyData);
        resultStorageDAO.writeTeamDynamics(sessionID, teamDynamics);
    }
}
