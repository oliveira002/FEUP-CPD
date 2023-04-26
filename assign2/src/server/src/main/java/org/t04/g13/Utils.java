package server.src.main.java.org.t04.g13;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Utils {
    public static String readResponse(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String response = in.readLine();

            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMessage(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
