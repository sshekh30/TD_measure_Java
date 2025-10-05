package servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.*;

@WebServlet(urlPatterns = { "/session/*" })
public class SessionServlet extends HttpServlet {

    private ObjectMapper json;
    private SessionEntropyService sessionEntropyService;

    public SessionServlet(SessionEntropyService sessionEntropyService) {
        this.sessionEntropyService = sessionEntropyService;
    }

    @Override
    public void init() throws ServletException {
        this.json = new ObjectMapper();
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        Map<String, String> body = json.readValue(
            req.getInputStream(),
            new TypeReference<Map<String, String>>() {}
        );
        String sessionID = body.get("sessionID");
        String dataSourceType = body.get("dataSourceType");

        if (sessionID == null) {
            resp.setStatus(400);
            sendJsonError(resp, "sessionID missing");
            return;
        }
        sessionEntropyService.CalculateEntropy(sessionID, dataSourceType);
        System.out.println("sessionID: " + sessionID);
    }

    private void sendJsonError(HttpServletResponse resp, Object data)
        throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.writeValueAsString(data));
    }
}
