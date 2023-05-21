package utils;

import utils.UserState;

import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.TimerTask;

import static utils.Utils.MAX_ELO_DIFF;

public class User {
    private SocketChannel channel;
    public String username;
    public int elo;
    public int minElo;
    public int maxElo;
    public UserState state;
    private int queueTime = 0;
    private int lossConnectionTime = 0;
    private Timer timerQueue;
    private Timer timerDisconnect;

    public User(){
        state = UserState.CONNECTED;
    }

    public User(String username, int elo, UserState state, SocketChannel channel){
        this.username = username;
        this.elo = elo;
        this.minElo = elo - MAX_ELO_DIFF;
        this.maxElo = elo + MAX_ELO_DIFF;
        this.state = state;
        this.channel = channel;
    }

    public void startQueueTimer() {
        timerQueue = new Timer();
        timerQueue.scheduleAtFixedRate(new TimerTask() {
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
        queueTime = 0;
        timerQueue.cancel();
    }

    public void startLossConectionTimer(){
        timerDisconnect = new Timer();
        timerDisconnect.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                lossConnectionTime++;
            }
        }, 0, 1000);
    }

    public int getLossConnectionTime() {
        return lossConnectionTime;
    }
    public void stopLossConnectionTime(){
        lossConnectionTime = 0;
        timerDisconnect.cancel();
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
