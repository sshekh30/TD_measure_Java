package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.SessionEntropyService;

@WebServlet(urlPatterns = { "/entropy/*" })
public class EntropyServlet extends HttpServlet {

    private ObjectMapper json;
    private SessionEntropyService service;

    public EntropyServlet(SessionEntropyService service) {
        this.service = service;
    }

    @Override
    public void init() throws ServletException {
        this.json = new ObjectMapper();
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        String sessionId = req.getParameter("sessionId");
        String scenarioId = req.getParameter("scenarioId");
        String pertubationId = req.getParameter("pertubationId");
        String fromStr = req.getParameter("from");
        String toStr = req.getParameter("to");
        String timeStr = req.getParameter("time");

        if (sessionId == null || sessionId.isEmpty()) {
            resp.setStatus(400);
            sendJsonError(resp, "sessionId is required");
            return;
        }

        try {
            Integer from = fromStr != null ? Integer.parseInt(fromStr) : null;
            Integer to = toStr != null ? Integer.parseInt(toStr) : null;
            Integer time = timeStr != null ? Integer.parseInt(timeStr) : null;

            System.out.println(
                "GET /entropy - sessionId: " +
                    sessionId +
                    ", scenarioId: " +
                    scenarioId +
                    ", pertubationId: " +
                    pertubationId +
                    ", from: " +
                    from +
                    ", to: " +
                    to +
                    ", time: " +
                    time
            );

            if (from != null && to != null && from >= to) {
                resp.setStatus(400);
                sendJsonError(resp, "from must be less than to");
                return;
            }

            Object result;
            if (time != null) {
                System.out.println("Calling getEntropyAtTime");
                result = service.getEntropyAtTime(sessionId, time);
            } else if (from != null && to != null) {
                System.out.println("Calling getEntropyInTimeRange");
                result = service.getEntropyInTimeRange(sessionId, from, to);
            } else if (scenarioId != null) {
                System.out.println(
                    "Calling getEntropyForScenario with scenarioId: " +
                        scenarioId
                );
                result = service.getEntropyForScenario(sessionId, scenarioId);
            } else if (pertubationId != null) {
                System.out.println("Calling getEntropyForPerturbation");
                result = service.getEntropyForPerturbation(
                    sessionId,
                    pertubationId
                );
            } else {
                System.out.println("Calling getEntireSessionEntropy");
                result = service.getEntireSessionEntropy(sessionId);
            }

            System.out.println(
                "Result type: " +
                    (result != null
                            ? result.getClass().getSimpleName()
                            : "null")
            );

            if (result == null) {
                resp.setStatus(404);
                sendJsonError(resp, "Session ID not found");
                return;
            }

            sendJsonResponse(resp, result);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            sendJsonError(resp, "Invalid number format for query parameters");
        } catch (Exception e) {
            resp.setStatus(500);
            sendJsonError(resp, "Server error: " + e.getMessage());
        }
    }

    private void sendJsonResponse(HttpServletResponse resp, Object data)
        throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(200);
        resp.getWriter().write(json.writeValueAsString(data));
    }

    private void sendJsonError(HttpServletResponse resp, Object data)
        throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.writeValueAsString(data));
    }
}
