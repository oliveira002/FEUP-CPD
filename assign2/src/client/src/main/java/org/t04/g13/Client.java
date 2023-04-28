package client.src.main.java.org.t04.g13;

import server.src.main.java.org.t04.g13.Utils;

import java.net.*;
import java.io.*;
import java.util.Objects;

/**
 * This program used a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class Client {

    private static boolean gameStarted = false;
    private static boolean gameFinished = false;

    public static void main(String[] args) {
        if (args.length < 2) return;

        Client client = new Client();
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        client.startConnection(hostname,port);
    }

    public void startConnection(String hostname, int port) {
        try {
            Socket socket = new Socket(hostname, port);
            this.authentication(socket);
            this.gameLoop(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void authentication(Socket socket) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Enter Username");
            String username = consoleIn.readLine();
            out.println(username);

            System.out.println("Enter Password");
            String password = consoleIn.readLine();
            out.println(password);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameLoop(Socket socket) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
            boolean answerTime = false;

            while (!gameFinished) {
                String message = Utils.readResponse(socket);
                if(message == null) {
                    continue;
                }
                if (message.equals("IN_QUEUE")) {
                    System.out.println("Waiting for other players to join the game...");
                } else if (message.equals("GAME_START")) {
                    System.out.println("The game has started!");
                    gameStarted = true;
                } else if (message.equals("GAME_FINISHED")) {
                    System.out.println("The game has ended!");
                    gameFinished = true;
                }
                else if (message.equals("ANSWER_TIME")) {
                    System.out.println("Type your answer:");
                    answerTime = true;
                } else {
                    if (gameStarted && answerTime) {
                        String answer = consoleIn.readLine();
                        out.println(answer);
                        answerTime = false;
                    }
                    else if(gameStarted) {
                        System.out.println(message);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
