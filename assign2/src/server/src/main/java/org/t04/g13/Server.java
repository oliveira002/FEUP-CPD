package server.src.main.java.org.t04.g13;

import server.src.main.java.org.t04.g13.Game;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import java.util.LinkedList;
import java.util.Queue;

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
            System.out.println("Server started on port " + PORT);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostName());

                String username = Utils.readResponse(clientSocket);
                System.out.printf("Received Username: %s%n", username);
                String password = Utils.readResponse(clientSocket);
                System.out.printf("Received Password: %s%n", password);
                Player player = new Player(clientSocket);
                player.setUser(username,password,0);

                Utils.sendMessage(clientSocket,"Added to Queue");
                addToQueue(player);
            }
        } catch (Exception e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }

    private void addToQueue(Player player) {
        waitingClients.offer(player);
        if (waitingClients.size() >= 2) {
            Game game = new Game();
            for (int i = 0; i < 2; i++) {
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