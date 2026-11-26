/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package co.ommco.ommcoemailsgateway.ommcoemailsgateway;
/**
 *
 * @author metal
 */
import java.io.*;
import java.net.ServerSocket; 
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class OMMCOEmailsGateway {

    private static final int PUERTO = 25;
    

    public static void main(String[] args) throws Exception {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {

            System.out.println(
                    "Servidor SMTP escuchando en puerto " + PUERTO
            );

            while (true) {

                Socket cliente = servidor.accept();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        procesarConexion(cliente);
                    }
                }).start();
            }
        }
    }

    private static void procesarConexion(Socket socket) {

       
//                socket;
//                ; 
//        
//        {
try{
BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );
BufferedWriter salida = new BufferedWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream(),
                                StandardCharsets.UTF_8
                        )
                ) ;
        
            responder(salida, "220 mail.ommco.co SMTP Ready");

            String remitente = null;
            String destinatario = null;

            String linea;

            while ((linea = entrada.readLine()) != null) {

                System.out.println("C: " + linea);

                String comando = linea.toUpperCase();

                if (comando.startsWith("EHLO")
                        || comando.startsWith("HELO")) {

                    responder(
                            salida,
                            "250 mail.ommco.co"
                    );

                } else if (comando.startsWith("MAIL FROM:")) {

                    remitente = linea.substring(10).trim();

                    responder(
                            salida,
                            "250 Sender OK"
                    );

                } else if (comando.startsWith("RCPT TO:")) {

                    destinatario = linea.substring(8).trim();

                    responder(
                            salida,
                            "250 Recipient OK"
                    );

                } else if (comando.equals("DATA")) {

                    responder(
                            salida,
                            "354 End data with <CR><LF>.<CR><LF>"
                    );

                    StringBuilder mensaje = new StringBuilder();

                    while ((linea = entrada.readLine()) != null) {

                        if (linea.equals(".")) {
                            break;
                        }

                        // SMTP dot-stuffing
                        if (linea.startsWith("..")) {
                            linea = linea.substring(1);
                        }

                        mensaje
                                .append(linea)
                                .append("\r\n");
                    }

                    guardarCorreo(
                            remitente,
                            destinatario,
                            mensaje.toString()
                    );

                    responder(
                            salida,
                            "250 Message accepted"
                    );

                } else if (comando.equals("RSET")) {

                    remitente = null;
                    destinatario = null;

                    responder(
                            salida,
                            "250 Reset OK"
                    );

                } else if (comando.equals("NOOP")) {

                    responder(
                            salida,
                            "250 OK"
                    );

                } else if (comando.equals("QUIT")) {

                    responder(
                            salida,
                            "221 Bye"
                    );

                    break;

                } else {

                    responder(
                            salida,
                            "502 Command not implemented"
                    );
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "Error procesando conexión SMTP:"
            );

            e.printStackTrace();
        }
    }

    private static void responder(
            BufferedWriter salida,
            String mensaje
    ) throws IOException {

        System.out.println("S: " + mensaje);

        salida.write(mensaje);
        salida.write("\r\n");
        salida.flush();
    }

    public static String intToStringTwoDigits(int number){
        if (number <= 9){
            return "0" + number;
        }
        return String.valueOf(number);
    }
    private static void guardarCorreo(
            String remitente,
            String destinatario,
            String mensaje
    ) throws IOException {

        String fecha = LocalDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMdd_HHmmss_SSS"
                        )
                );
       
        
        Calendar cal = Calendar.getInstance();
        System.out.println(cal.get(Calendar.YEAR));
        
        Path DIRECTORIO = Paths.get("/home/correos/" + 
                cal.get(Calendar.YEAR) 
                + intToStringTwoDigits(cal.get(Calendar.MONTH) +1) 
                + ""
                + intToStringTwoDigits(cal.get(Calendar.DAY_OF_MONTH))      
                +"/");
        Files.createDirectories(DIRECTORIO);
        
        Path archivo = DIRECTORIO.resolve(
              "correo_" + fecha + "_" + System.currentTimeMillis() + ".txt"
        );

        String contenido
                = "MAIL FROM: " + remitente + "\r\n"
                + "RCPT TO: " + destinatario + "\r\n"
                + "\r\n"
                + mensaje;

        Files.writeString(
                archivo,
                contenido,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW
        );

        System.out.println(
                "Correo guardado en: "
                + archivo.toAbsolutePath()
        );
    }
}
