/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlets;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.http.*;

import java.io.BufferedReader;
import java.util.Collections;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author HATSUMY
 */
@WebServlet(name = "LoginGoogle", urlPatterns = {"/logingoogle"})
public class LoginGoogle extends HttpServlet {

    private static final String CLIENT_ID = "590243711125-2iabip0tttfkah4ak2ecf8l9qs06h6c1.apps.googleusercontent.com";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Leer JSON recibido con id_token
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String jsonString = sb.toString();

        String idTokenString = "";
        try {
            JsonObject json = new Gson().fromJson(jsonString, JsonObject.class);
            idTokenString = json.get("id_token").getAsString();
        } catch (Exception e) {
            out.println("{\"resultado\":\"error\",\"mensaje\":\"JSON inválido\"}");
            return;
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                // Aquí puedes validar o registrar el usuario en tu BD si deseas

                // Generar JWT propio para tu app (asegúrate de tener JwtUtil)
                String token = utils.JwtUtil.generarToken(email);

                // Crear sesión y guardar email
                HttpSession sesion = request.getSession();
                sesion.setAttribute("usuario", email);

                JsonObject respJson = new JsonObject();
                respJson.addProperty("resultado", "ok");
                respJson.addProperty("token", token);
                respJson.addProperty("email", email);
                out.println(respJson.toString());
            } else {
                out.println("{\"resultado\":\"error\",\"mensaje\":\"Token inválido\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("{\"resultado\":\"error\",\"mensaje\":\"Excepción al verificar token\"}");
        }
    }
}