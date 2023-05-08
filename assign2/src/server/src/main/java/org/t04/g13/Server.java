package server.src.main.java.org.t04.g13;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static server.src.main.java.org.t04.g13.Utils.*;


public class Server {
    private final int PORT;
    private final List<Game> games;
    private final Queue<Player> waitingClients;
    private ExecutorService auth_pool;

    public Server(int port) {

        this.PORT = port;

        games = new ArrayList<>();
        waitingClients = new ArrayDeque<>();

        //New thread pool with at most MAX_TRHEADS authentication attempts
        auth_pool = Executors.newFixedThreadPool(MAX_TRHEADS);
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            int i = 1;
            System.out.println("Server started on port " + PORT);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n(#" + i + ") New client connected from IP: " + clientSocket.getInetAddress().getHostAddress());
                Player player = new Player(clientSocket);
                addToQueue(player);
                sendMessage(clientSocket,ENQUEUE);
                //Runnable auth = new Auth(clientSocket, this, i++);

                //TODO catch exception -> client disconnects before or during his auth process
                //TODO stop auth process if it's taking too long
                //auth_pool.execute(auth);
            }

        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            auth_pool.shutdown();
        }
    }

    void addToQueue(Player player) {
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

        int port = 8000;
        if(args.length != 0){
            try{
                port = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e){
                System.out.printf("First argument has to be an integer, %s not allowed!%n", args[0]);
                System.out.printf("Using default port: %d%n%n", port);
            }
        }

        Server server = new Server(port);
        server.start();
    }

}