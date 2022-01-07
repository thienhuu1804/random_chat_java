package Server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RunServer {

    static final int PORT = 4445;
    public static volatile ArrayList<String> takenNameList = new ArrayList<>();
    public static volatile ArrayList<ClientThread> waitingList = new ArrayList<>();
    public static volatile ArrayList<ClientThread> allClients = new ArrayList<>();

    public static void addUser(ClientThread item) {
        waitingList.add(item);
        takenNameList.add(item.info.getUsername());
    }

    public static void removeUser(Info u) {
        waitingList.remove(u);
        takenNameList.remove(u.getUsername());
    }

    public static void main(String args[]) {
        waitingList = new ArrayList<>();
        Socket socket = null;
        ServerSocket serverSocket = null;
        System.out.println("Server Listening on port " + PORT + "......");
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("Server error");
        }
        while (true) {
            try {
                socket = serverSocket.accept();
                System.out.println("connection Established: Address " + socket);
                ClientThread st = new ClientThread(socket);
                st.start();
                allClients.add(st);

            } catch (Exception e) {
                //System.out.println("Connection Error");

            }
        }
    }

}
