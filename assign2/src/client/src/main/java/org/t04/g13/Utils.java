package client.src.main.java.org.t04.g13;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.Socket;

public class Utils {
    private static final String EOF_MARKER = "<EOF>";
    public static String readResponse(Socket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);

            StringBuilder response = new StringBuilder();
            StringBuilder currentLine = new StringBuilder();

            int currentChar;
            int markerIndex = 0;

            while ((currentChar = reader.read()) != -1) {
                char ch = (char) currentChar;
                response.append(ch);
                currentLine.append(ch);

                if (ch == EOF_MARKER.charAt(markerIndex)) {
                    markerIndex++;
                    if (markerIndex == EOF_MARKER.length()) {
                        return response.substring(0, response.length() - EOF_MARKER.length());
                    }
                } else {
                    markerIndex = 0;
                }
            }

            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

