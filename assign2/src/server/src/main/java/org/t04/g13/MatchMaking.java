package server.src.main.java.org.t04.g13;

public class MatchMaking extends Thread {
    private final Server server;
    private long matchmakingInterval = 1000;

    public MatchMaking(Server server, long matchmakingInterval) {
        this.server = server;
        this.matchmakingInterval = matchmakingInterval;
    }

    @Override
    public void run() {
        int i = 0;
        while (true) {
            try {
                Thread.sleep(1000);
                i++;
                server.matchMaking();
                System.out.println("Second: " + i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}