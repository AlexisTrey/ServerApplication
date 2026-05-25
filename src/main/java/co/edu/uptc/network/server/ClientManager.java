package co.edu.uptc.network.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientManager {

    private static final List<ClientHandler> clients =
            Collections.synchronizedList(new ArrayList<>());

    private ClientManager() {}

    public static synchronized void addClient(ClientHandler client) {
        clients.add(client);
    }

    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static synchronized void broadcast(String json) {
        for (ClientHandler client : clients) {
            client.sendMessage(json);
        }
    }

    public static synchronized void sendTo(String studentCode, String json) {
        clients.stream()
                .filter(c -> studentCode.equals(c.getStudentCode()))
                .findFirst()
                .ifPresent(c -> c.sendMessage(json));
    }

    public static synchronized int getCount() {
        return clients.size();
    }

}
