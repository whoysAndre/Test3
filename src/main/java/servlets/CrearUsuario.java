/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlets;

import dao.ClienteJpaController;
import dto.Cliente;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.JwtUtil;

/**
 *
 * @author HATSUMY
 */
@WebServlet(name = "CrearUsuario", urlPatterns = {"/crearusuario"})
public class CrearUsuario extends HttpServlet {

    ClienteJpaController clienteDAO = new ClienteJpaController();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try (JsonReader jsonReader = Json.createReader(request.getReader())) {
            JsonObject jsonObject = jsonReader.readObject();

            String dniCli = jsonObject.getString("dniCli");
            String apaCli = jsonObject.getString("apaCli");
            String amaCli = jsonObject.getString("amaCli");
            String nomCli = jsonObject.getString("nomCli");
            String fchNacCliStr = jsonObject.getString("fchNacCli"); // formato yyyy-MM-dd
            String logiCli = jsonObject.getString("logiCli");
            String pasCli = jsonObject.getString("pasCli");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fchNacCli = sdf.parse(fchNacCliStr);

            // Verificar usuario existente
            Cliente existeUsuario = clienteDAO.findClienteByUsername(logiCli);
            if (existeUsuario != null) {
                out.print(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "El usuario ya existe.")
                    .build()
                    .toString());
                return;
            }

            // Generar codCli (por ej. max+1)
            int nuevoCodCli = clienteDAO.getClienteCount() + 1;

            Cliente nuevoCliente = new Cliente(nuevoCodCli, dniCli, apaCli, amaCli, nomCli, fchNacCli, logiCli, pasCli);

            clienteDAO.create(nuevoCliente);

            out.print(Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", "Usuario creado exitosamente")
                    .build()
                    .toString());

        } catch (Exception e) {
            e.printStackTrace();
            out.print(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al crear usuario: " + e.getMessage())
                    .build()
                    .toString());
        }
        out.flush();
    }
}