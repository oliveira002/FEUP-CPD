package utils;

import utils.UserState;

import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.TimerTask;

public class User {
    private SocketChannel channel;
    private String token;
    public String username;
    public int elo;
    public UserState state;
    private int queueTime = 0;
    private Timer timer;

    public User(){
        state = UserState.CONNECTED;
    }

    public User(String username, int elo, UserState state, SocketChannel channel){
        this.username = username;
        this.elo = elo;
        this.state = state;
        this.channel = channel;
    }

    public void startQueueTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                queueTime++;
            }
        }, 0, 1000);
    }

    public int getQueueTime() {
        return queueTime;
    }

    public void stopQueueTime(){
        timer.cancel();
    }

    public SocketChannel getChannel(){
        return this.channel;
    }

    public void setChannel(SocketChannel channel){
        this.channel = channel;
    }

    @Override
    public String toString(){
        return "Username: " + username + "\nElo: " + elo + "\nState: " + state;
    }

    public int getElo() {
        return this.elo;
    }
}
