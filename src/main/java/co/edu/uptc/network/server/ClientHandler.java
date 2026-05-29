package co.edu.uptc.network.server;

import co.edu.uptc.interfaces.PresenterInterface;
import co.edu.uptc.network.protocol.MessageParser;
import co.edu.uptc.network.protocol.Protocol;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    private final Socket             socket;
    private final PresenterInterface presenter;
    private PrintWriter              writer;
    private String                   studentCode;
    private boolean                  disconnected = false;

    public ClientHandler(Socket socket, PresenterInterface presenter) {
        this.socket    = socket;
        this.presenter = presenter;
    }

    @Override
    public void run() {
        try (BufferedReader reader = openReader()) {
            writer = openWriter();
            ClientManager.addClient(this);
            String line;
            while ((line = reader.readLine()) != null) {
                handleMessage(line);
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + studentCode);
        } finally {
            cleanup();
        }
    }

    private void handleMessage(String json) {
        String type = MessageParser.getType(json);
        switch (type) {
            case Protocol.CONNECT    -> handleConnect(json);
            case Protocol.MOVE       -> handleMove(json);
            case Protocol.DISCONNECT -> handleDisconnect();
        }
    }

    private void handleConnect(String json) {
        studentCode = MessageParser.getString(json, "studentCode");
        presenter.onPlayerConnect(studentCode, this);
    }

    private void handleMove(String json) {
        String direction = MessageParser.getString(json, "direction");
        presenter.onPlayerMove(studentCode, direction);
    }

    private void handleDisconnect() {
        disconnected = true;
        presenter.onPlayerDisconnect(studentCode);
    }

    private void cleanup() {
        ClientManager.removeClient(this);
        if (!disconnected) {
            disconnected = true;
            presenter.onPlayerDisconnect(studentCode);
        }
        try { socket.close(); } catch (IOException ignored) {}
    }

    public void sendMessage(String json) {
        if (writer != null) writer.println(json);
    }

    public String getStudentCode() { return studentCode; }

    private BufferedReader openReader() throws IOException {
        return new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    private PrintWriter openWriter() throws IOException {
        return new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }
}