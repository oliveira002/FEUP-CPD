package server.src.main.java.org.t04.g13;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import java.util.LinkedList;
import java.util.Queue;

import static server.src.main.java.org.t04.g13.Utils.*;


public class Server {
    private static final int PORT = 8000;
    private final List<Game> games;
    private final Queue<Player> waitingClients;

    public Server() {
        games = new ArrayList<>();
        waitingClients = new LinkedList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT + "\n");
            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostName());

                new Thread(() -> {
                    //

                    // Read the username and password from the client
                    String username = readResponse(clientSocket);
                    System.out.printf("Received Username: %s%n", username);
                    String password = readResponse(clientSocket);
                    System.out.printf("Received Password: %s%n", password);

                    // Create a new player object and add it to the queue
                    Player player = new Player(clientSocket);
                    player.setUser(username, password, 0);

                    sendMessage(clientSocket,ENQUEUE);
                    addToQueue(player);
                }).start();

            }
        } catch (Exception e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }

    private void addToQueue(Player player) {
        waitingClients.offer(player);
        if (waitingClients.size() >= MAX_PLAYERS) {
            Game game = new Game();
            for (int i = 0; i < MAX_PLAYERS; i++) {
                Player currPlayer = waitingClients.poll();
                game.addPlayer(currPlayer);
            }
            game.start();
            games.add(game);
        }
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

}