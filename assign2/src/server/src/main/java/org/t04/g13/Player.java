package server.src.main.java.org.t04.g13;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Player {

    private String username;
    private String password;
    private int elo = 0;

    private Socket clientSocket;

    public Player(Socket socket) {
        this.clientSocket = socket;
    }

    public void setUser(String username, String password, int elo) {
        this.username = username;
        this.password = password;
        this.elo = elo;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

}