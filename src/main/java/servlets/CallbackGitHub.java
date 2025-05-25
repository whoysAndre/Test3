package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.PrintWriter;

/**
 *
 * @author HATSUMY
 */
@WebServlet(name = "CallbackGitHub", urlPatterns = {"/callbackgithub"})
public class CallbackGitHub extends HttpServlet {

    private static final String CLIENT_ID = "Ov23liolfyqfgkImHR1K";
    private static final String CLIENT_SECRET = "752771f6167d7dcc62994f58ad27f1dd3ffbf90d";
    private static final String REDIRECT_URI = "http://localhost:8080/Test3/callbackgithub";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getParameter("code");
        resp.setContentType("application/json;charset=UTF-8");

        if (code == null) {
            resp.getWriter().println("{\"error\": \"No se recibió código de autorización\"}");
            return;
        }

        // Obtener access token
        String tokenResponse = getAccessToken(code);
        String accessToken = parseAccessToken(tokenResponse);

        if (accessToken == null) {
            resp.getWriter().println("{\"error\": \"Error al obtener access token\"}");
            return;
        }

        // Obtener info usuario y email
        String userInfoJson = getUserInfo(accessToken);
        String email = extractEmail(userInfoJson);
        if (email == null || email.equals("usuario@github.com")) {
            email = getPrimaryEmail(accessToken);
            if (email == null) {
                email = "usuario@github.com";
            }
        }

        // Crear JWT y sesión
        String tokenJWT = utils.JwtUtil.generarToken(email);
        HttpSession session = req.getSession();
        session.setAttribute("usuario", email);

        // Redirigir a principal.html con token y email como query params
        String redirectUrl = String.format("dist/pages/index.html?token=%s&email=%s",
                URLEncoder.encode(tokenJWT, "UTF-8"),
                URLEncoder.encode(email, "UTF-8"));
        resp.sendRedirect(redirectUrl);
    }

    private String getPrimaryEmail(String accessToken) throws IOException {
        URL url = new URL("https://api.github.com/user/emails");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "token " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();

        JsonArray emails = new Gson().fromJson(sb.toString(), JsonArray.class);
        for (JsonElement el : emails) {
            JsonObject emailObj = el.getAsJsonObject();
            if (emailObj.get("primary").getAsBoolean() && emailObj.get("verified").getAsBoolean()) {
                return emailObj.get("email").getAsString();
            }
        }
        return null;
    }

    private String getAccessToken(String code) throws IOException {
        URL url = new URL("https://github.com/login/oauth/access_token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Accept", "application/json");

        String params = "client_id=" + CLIENT_ID
                + "&client_secret=" + CLIENT_SECRET
                + "&code=" + code
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");

        try ( OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes());
            os.flush();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();

        System.out.println("Respuesta token GitHub: " + sb.toString());  // <- Aquí        

        return sb.toString();
    }

    private String parseAccessToken(String json) {
        try {
            JsonObject obj = new Gson().fromJson(json, JsonObject.class);
            return obj.get("access_token").getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private String getUserInfo(String accessToken) throws IOException {
        URL url = new URL("https://api.github.com/user");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "token " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    private String extractEmail(String userInfoJson) {
        try {
            JsonObject obj = new Gson().fromJson(userInfoJson, JsonObject.class);
            if (obj.has("email") && !obj.get("email").isJsonNull()) {
                return obj.get("email").getAsString();
            }
            return "usuario@github.com"; // fallback
        } catch (Exception e) {
            return "usuario@github.com";
        }
    }
}
