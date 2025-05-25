package servlets;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author HATSUMY
 */
@WebServlet(name = "LoginGitHub", urlPatterns = {"/logingithub"})
public class LoginGitHub extends HttpServlet {

    private static final String CLIENT_ID = "Ov23liolfyqfgkImHR1K";
    private static final String REDIRECT_URI = "http://localhost:8080/Test3/callbackgithub";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = "https://github.com/login/oauth/authorize" +
                "?client_id=" + CLIENT_ID +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") +
                "&scope=user:email";  // permisos que pides (email público)

        resp.sendRedirect(url);
    }
}