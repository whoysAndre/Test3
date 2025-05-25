package servlets;

import dao.ClienteJpaController;
import dto.Cliente;
import java.io.IOException;
import java.io.PrintWriter;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.JwtUtil;
import utils.BcryptJava;

@WebServlet(name = "ChangePassword", urlPatterns = {"/passchange"})
public class ChangePassword extends HttpServlet {

    ClienteJpaController clieDAO = new ClienteJpaController();

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");        

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String token = authHeader.substring("Bearer ".length());
        boolean isValidToken = JwtUtil.validarToken(token);
        if (!isValidToken) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        PrintWriter out = response.getWriter();

        try ( JsonReader jsonReader = Json.createReader(request.getReader())) {
            JsonObject jsonObject = jsonReader.readObject();

            String username = jsonObject.getString("username");
            String currentPassword = jsonObject.getString("currentPassword");
            String newPassword = jsonObject.getString("newPassword");

            Cliente cliente = clieDAO.findClienteByUsername(username);

            if (cliente == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Validar contraseña actual con Bcrypt
            if (!BcryptJava.checkPassword(currentPassword, cliente.getPasCli())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Hashear la nueva contraseña antes de guardar
            String newHashedPassword = BcryptJava.hashPassword(newPassword);
            cliente.setPasCli(newHashedPassword);

            clieDAO.edit(cliente);

            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("success", true)
                    .build();
            out.print(jsonResponse.toString());
            out.flush();

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            JsonObject errorResponse = Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al cambiar contraseña: " + e.getMessage())
                    .build();
            out.print(errorResponse.toString());
            out.flush();
        }
    }
}
