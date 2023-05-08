package server.src.main.java.org.t04.g13;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Utils {

    static final int MAX_TRHEADS = 2;
    static final int MAX_PLAYERS = 2;
    static final int NUM_QUESTIONS = 3;
    static final int QUESTION_TIME = 80000;
    static final String ENQUEUE = "IN_QUEUE";
    static final String START_ROUND = "START_ROUND";
    static final String END_ROUND = "END_ROUND";
    static final String ANSWER_TIME = "ANSWER_TIME";
    static final String GAME_START = "GAME_START";
    static final String GAME_END = "GAME_FINISHED";

    public static String readResponse(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMessage(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
