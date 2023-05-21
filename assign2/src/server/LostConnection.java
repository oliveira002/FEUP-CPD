package server;

public class LostConnection implements Runnable{

    private final Server server;
    private final int CLIENT_PURGE_MILISECONDS = 1000;

    public LostConnection(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        int i = 0;
        while (true) {
            try {
                Thread.sleep(CLIENT_PURGE_MILISECONDS);
                i++;
                server.purgeLostConnections();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
