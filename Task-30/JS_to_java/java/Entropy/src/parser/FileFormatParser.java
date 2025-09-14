package parser;

import java.io.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class FileFormatParser implements DataParser {

  private static Map<String, Boolean> triageStatus = new HashMap<>();
  private static Map<String, Boolean> treatmentStatus = new HashMap<>();
  private static double[] movementROI = null;

  private ObjectMapper mapper = new ObjectMapper();

  private void initializeFromScenarioDefinition(List<String> rawData) {
    for (String line : rawData) {
      try {
        JsonNode node = mapper.readTree(line);
        if ("ScenarioDefinition".equals(node.get("scenarioEvent").asText())) {
          initializeCasualties(node);
          initializeMovementROI(node);
          break;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void initializeCasualties(JsonNode jsonObject) {
    JsonNode casualties = jsonObject.get("Scenario").get("Casualties");

    for (JsonNode casualty: casualties) {
      String casualtyId = casualty.get("id").asText();
      triageStatus.put(casualtyId, false);
      treatmentStatus.put(casualtyId, false);
    }
  }

  public void initializeMovementROI(JsonNode jsonObject) {
      try {
          JsonNode scenario = jsonObject.get("Scenario");
          if (scenario == null) {
              System.err.println("No 'Scenario' field found in JSON");
              return;
          }
          
          JsonNode regions = scenario.get("RegionsOfInterest");
          if (regions == null) {
              System.err.println("No 'RegionsOfInterest' field found in Scenario");
              return;
          }
          
          for (JsonNode region : regions) {
              JsonNode idNode = region.get("id");
              if (idNode != null && "MovementLayerImportant".equals(idNode.asText())) {
                  double[] location = mapper.treeToValue(region.get("location"), double[].class);
                  
                  if (location.length < 6) {
                      System.err.println("Location array too short, expected at least 6 elements");
                      return;
                  }
                  
                  double centerX = location[0];
                  double centerZ = location[2];
                  double midX = location[3];
                  double midZ = location[5];
                  
                  movementROI = new double[]{centerX, centerZ, midX, midZ};
                  break;
              }
          }
      } catch (Exception e) {
          System.err.println("Error processing JSON for movement ROI: " + e.getMessage());
      }
  }

  public static List<String> extractCommunication(JsonNode jsonObject) {
    List<String> comms = new ArrayList<>();
    try {
        JsonNode trainees = jsonObject.get("trainees");
        if (trainees == null || !trainees.isArray()) {
          return comms;
        } 
        for (int i = 0; i < trainees.size(); i++) {
          JsonNode trainee = trainees.get(i);
          JsonNode commNode = trainee.get("Communication");
          comms.add(commNode.asBoolean() ? "1": "0");
        }
    } catch (Exception e) {
        System.err.println("Error extracting communication: " + e.getMessage());
    }
    return comms;
  }
  public static List<String> extractVisual(JsonNode jsonObject) {
    List<String> visual = new ArrayList<>();

    try {
      JsonNode trainees = jsonObject.get("trainees");
      for (int i = 0; i < trainees.size(); i++) {
        StringBuilder state = new StringBuilder();
        JsonNode trainee = trainees.get(i);
        

        if (trainee.get("VisualActivity") != null && trainee.get("VisualActivity").has("Trainee_Watched")) {
          state.append("1");
        } else {
          state.append("0");
        }

        if (trainee.get("VisualActivity") != null && trainee.get("VisualActivity").has("OOI_Watched")) {
          state.append("1");
        } else {
          state.append("0");
        }

        if (trainee.get("VisualActivity") != null && trainee.get("VisualActivity").has("ROI_Watched")) {
          state.append("1");
        } else {
          state.append("0");
        }

        visual.add(state.toString());
      }
    } catch (Exception e) {
      System.err.println("Error extracting visual: " + e.getMessage());
    }

    return visual;
  }

  public List<String> extractMovement(JsonNode jsonObject) {
    List<String> movementStates = new ArrayList<>();
    JsonNode trainees = jsonObject.get("trainees");
    try {
      for (int i = 0; i < trainees.size(); i++) {
        JsonNode traineeMovement = trainees.get(i).get("Movement");
        double[] headCoords = mapper.treeToValue(traineeMovement.get("Head"), double[].class);

        double headX = headCoords[0];
        double headZ = headCoords[2];

        double minX = movementROI[0] - movementROI[2] / 2;
        double maxX = movementROI[0] + movementROI[2] / 2;
        double minZ = movementROI[1] - movementROI[3] / 2;
        double maxZ = movementROI[1] + movementROI[3] / 2;


        boolean isInROI = (headX >= minX && headX <= maxX && headZ >= minZ && headZ <= maxZ);
        movementStates.add(isInROI ? "1" : "0");
      }
    } catch (Exception e) {
      System.err.println("Error extracting movement: " + e.getMessage());
    }
   
   return movementStates;
  }

  public static void updateCasualtyStatus(JsonNode jsonObject) {
    if ("triage".equals(jsonObject.get("subtype").asText()) && "stop".equals(jsonObject.get("event").asText())) {
      String subtypeId = jsonObject.get("subtype_id").asText();
      triageStatus.put(subtypeId, true);
    }

    if ("treatment".equals(jsonObject.get("subtype").asText()) && "stop".equals(jsonObject.get("event").asText())) {
      String subtypeId = jsonObject.get("subtype_id").asText();
      treatmentStatus.put(subtypeId, true);
    }
  }

  private List<String> getCurrentCasualtyState() {
      List<String> casualtyStates = new ArrayList<>();
      StringBuilder allCasualties = new StringBuilder();
      for (String casualtyId : triageStatus.keySet()) {
          String triageState = triageStatus.get(casualtyId) ? "1" : "0";
          String treatmentState = treatmentStatus.get(casualtyId) ? "1" : "0";
          allCasualties.append(triageState).append(treatmentState);
      }
      casualtyStates.add(allCasualties.toString());
      return casualtyStates;
  }
  @Override
  public List<List<List<String>>> parseToSTTCLayers(List<String> rawData) throws IOException {
    boolean logging = true;

    List<List<List<String>>> sttcLayers = new ArrayList<>();
    List<List<String>> visual = new ArrayList<>();
    List<List<String>> comm = new ArrayList<>();
    List<List<String>> casualty = new ArrayList<>();
    List<List<String>> movement = new ArrayList<>();
   
    initializeFromScenarioDefinition(rawData);
     for (String line : rawData) {
        JsonNode node = mapper.readTree(line);
        String eventType = node.get("scenarioEvent").asText();

         if ("Timer".equals(eventType)) {

            List<List<String>> dataFrame = new ArrayList<>();
            List<String> traineeComm = extractCommunication(node);
            List<String> traineeViz = extractVisual(node);
            List<String> traineeCas = getCurrentCasualtyState();
            List<String> traineeMov = extractMovement(node);            

            comm.add(traineeComm);
            visual.add(traineeViz);
            casualty.add(traineeCas);
            movement.add(traineeMov);

            dataFrame.add(traineeComm);
            dataFrame.add(traineeViz);
            dataFrame.add(traineeCas);
            dataFrame.add(traineeMov);
            
            sttcLayers.add(dataFrame);
         } else if ("Domain".equals(eventType)) {
            updateCasualtyStatus(node);
         }   
     }
     if (logging) {
       System.out.println("======Visual Layer: ======");
       System.out.println(visual);
       System.out.println("======Communication Layer: ======");
       System.out.println(comm);
       System.out.println("======Casualty Layer: ======");
       System.out.println(casualty);
       System.out.println("======Movement Layer: ======");
       System.out.println(movement);
      
       System.out.println("========STTC LAYERS:============");
        for (int t = 0; t < sttcLayers.size(); t++) {
           System.out.println("Time " + t + ":");
           List<List<String>> timeInstance = sttcLayers.get(t);
           
           if (timeInstance.size() >= 4) {
               System.out.println("  Communication: " + timeInstance.get(0));
               System.out.println("  Visual:        " + timeInstance.get(1));
               System.out.println("  Casualty:      " + timeInstance.get(2));
               System.out.println("  Movement:      " + timeInstance.get(3));
           } else {
               System.out.println("  Incomplete data - only " + timeInstance.size() + " layers");
               for (int layer = 0; layer < timeInstance.size(); layer++) {
                   System.out.println("  Layer " + layer + ": " + timeInstance.get(layer));
               }
           }
           System.out.println();
        }
     }
     return sttcLayers;
  }
}
