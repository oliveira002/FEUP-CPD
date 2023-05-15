package server.src.main.java.org.t04.g13;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Utils {
    static final int MAX_THREADS = ManagementFactory.getThreadMXBean().getThreadCount();
    static final int MAX_PLAYERS = 2;
    static final int NUM_QUESTIONS = 3;
    static final int QUESTION_TIME = 80000;
    static final String ENQUEUE = "IN_QUEUE";
    static final String START_ROUND = "START_ROUND";
    static final String END_ROUND = "END_ROUND";
    static final String ANSWER_TIME = "ANSWER_TIME";
    static final String GAME_START = "GAME_START";
    static final String GAME_END = "GAME_FINISHED";
    private static final String EOF_MARKER = "<EOF>";

    public static String readResponse(SocketChannel socket) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            StringBuilder response = new StringBuilder();

            while (socket.read(buffer) != -1) {
                buffer.flip(); // Switch to read mode

                // Read the bytes from the buffer and append them to the response string
                while (buffer.hasRemaining()) {
                    response.append((char) buffer.get());
                }

                buffer.clear(); // Clear the buffer for the next read
            }
            System.out.println(response);
            return response.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMessage(SocketChannel socket, String message) {
        try {
            message += EOF_MARKER;
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.wrap(messageBytes);

            while (buffer.hasRemaining()) {
                socket.write(buffer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
