package co.edu.uptc.network.server;

import co.edu.uptc.config.AppConfig;
import co.edu.uptc.interfaces.PresenterInterface;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer implements Runnable {

    private ServerSocket     serverSocket;
    private PresenterInterface presenter;
    private volatile boolean running;

    public GameServer(PresenterInterface presenter) {
        this.presenter = presenter;
    }

    @Override
    public void run() {
        startListening();
    }

    private void startListening() {
        try {
            serverSocket = new ServerSocket(AppConfig.getPort());
            running      = true;
            System.out.println("Server listening on port " + AppConfig.getPort());
            while (running) {
                acceptClient();
            }
        } catch (IOException e) {
            if (running) System.err.println("Server error: " + e.getMessage());
        }
    }

    private void acceptClient() throws IOException {
        Socket client    = serverSocket.accept();
        ClientHandler handler = new ClientHandler(client, presenter);
        new Thread(handler).start();
    }

    public void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); }
        catch (IOException ignored) {}
    }
}