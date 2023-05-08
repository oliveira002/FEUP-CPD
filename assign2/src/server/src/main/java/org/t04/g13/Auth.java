package server.src.main.java.org.t04.g13;

import java.io.IOException;
import java.net.Socket;

import static server.src.main.java.org.t04.g13.Utils.*;

public class Auth implements Runnable{

    Socket clientSocket;
    Server server;
    int i;

    public Auth(Socket clientSocket, Server server, int i){
        this.clientSocket = clientSocket;
        this.server = server;
        this.i = i;
    }

    @Override
    public void run() {

        System.out.println("(#" + i + ") Authentication started");

        //Authentication method selection
        sendMessage(clientSocket, """
                1. Register
                2. Login
                0. Disconnect
                Please choose one from the above:\s
                """);

        //Read the authentication method option selected
        int opt = Integer.parseInt(readResponse(clientSocket));
        switch (opt){
            case 0:
                try {
                    clientSocket.close();
                    System.out.println("(#" + i + ") Client disconnected");
                    return;
                } catch (IOException e) {
                    System.out.println("(#" + i + ") Error closing client socket");
                    throw new RuntimeException(e);
                }
            case 1:
                register();
            case 2:
                login();
        }


        // Read the username and password from the client
        /*String username = readResponse(clientSocket);
        System.out.printf("Received Username: %s%n", username);
        String password = readResponse(clientSocket);
        System.out.printf("Received Password: %s%n", password);*/

        // Create a new player object and add it to the queue
        Player player = new Player(clientSocket);
        player.setUser(username, password, 0);

        sendMessage(clientSocket, ENQUEUE);
        server.addToQueue(player);
        System.out.println("(#" + i + ") Authentication finished\n");
    }

    private void register(){
        System.out.println();
    }

    private void login(){

    }
}
