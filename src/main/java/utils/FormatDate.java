
package utils;

import dao.ClienteJpaController;
import dto.Cliente;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author yello
 */
public class FormatDate {

    public static String formatearFecha(Date fecha) {
        if (fecha == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(fecha);
    }
    
    public static int calcularEdad(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            return 0;
        }
        
        Date fechaActual = new Date();
        long diferenciaMilisegundos = fechaActual.getTime() - fechaNacimiento.getTime();
        long diasTranscurridos = diferenciaMilisegundos / (1000L * 60 * 60 * 24);
        
        return (int) (diasTranscurridos / 365);
    }
    
    public static void main(String[] args) {
        ClienteJpaController clieDAO = new ClienteJpaController();
        Cliente cl = clieDAO.findClienteByUsername("rod");
        
        String date = FormatDate.formatearFecha(cl.getFchNacCli());
        int edad = FormatDate.calcularEdad(cl.getFchNacCli());
        System.out.println(date);
        System.out.println(edad);
        
    }
}
