package server;

public class RankedMatchmaking implements Runnable {

    private final Server server;
    private final int MATCHMAKING_INTERVAL_MILISECONDS = 1000;

    public RankedMatchmaking(Server server) {
        this.server = server;
    }


    @Override
    public void run() {
        int i = 0;
        while (true) {
            try {
                Thread.sleep(MATCHMAKING_INTERVAL_MILISECONDS);
                i++;
                server.rankedMatchMaking();
                //System.out.println("Second: " + i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
