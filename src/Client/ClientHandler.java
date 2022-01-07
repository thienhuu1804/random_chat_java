package Client;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import Shared.StreamConst;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class ClientHandler {

    public final int PORT = 4445;
    public final String IP = "127.0.0.1";
    static Socket socket = null;
    static DataInputStream input = null;
    static DataOutputStream output = null;

    private String reciever;
    private Thread listener;
    String user;

    public ClientHandler() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 4445), 5000);
            System.out.println("Connected to " + IP + ":" + PORT + ", localport:" + socket.getLocalPort());
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            listener = new Thread(this::listen);
            listener.start();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void listen() {
        boolean running = true;

        while (running) {
            try {
                String recieved = input.readUTF();
                System.out.println(recieved);
                StreamConst.Type type = StreamConst.getTypeFromData(recieved.split(";")[0]);
                String message = recieved.split(";")[1];
//                System.out.println("mess: " + message);
                switch (type) {
                    case NICKNAME_ACCEPT:
                        onReceiveNameAccept(message);
                        break;
                    case NICKNAME_DECLINE:
                        onReceiveNameDecline(message);
                        break;
                    case MATCH_FOUND:
                        onMatchFound(message);
                        break;
                    case ROOM_JOINED:
                        onRoomJoined(message);
                        break;
                    case MATCH_DECLINE:
                        onMatchDeclined(message);
                        break;
                    case MESSAGE:
                        onMessageReceived(message);
                        break;
                    case END_CHAT:
                        onReceiveEndChat(message);
                        break;
                }
            } catch (IOException ex) {
                System.out.println("Ngat ket noi");
                break;
            }
        }
    }

    private void onReceiveEndChat(String ender){
        if(RunClient.chatScene != null){
            JOptionPane.showMessageDialog(RunClient.chatScene.getContentPane(), ender + " has ended the chat!");
            RunClient.closeScene(RunClient.SceneName.CHAT);
            RunClient.openScene(RunClient.SceneName.WAITING);
            RunClient.waitingScene.setTitle(user);
        }
    }
    private void onMessageReceived(String text){
        RunClient.chatScene.appendmess(text);
    }
    private void onReceiveNameAccept(String mess) {
        RunClient.openScene(RunClient.SceneName.WAITING);
        user = mess;
        RunClient.waitingScene.setTitle(user);
        RunClient.waitingScene.setUsername(user);
        RunClient.closeScene(RunClient.SceneName.LOGIN);
    }

    private void onReceiveNameDecline(String message) {
        RunClient.loginScene.setNotice(message);
    }

    private void onMatchFound(String name) {
        int opt = JOptionPane.showInternalConfirmDialog(RunClient.waitingScene.getContentPane(), "Start chatting with " + name, "Match Found", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            sendMatchAccept(name);
        }
        if (opt == JOptionPane.NO_OPTION || opt == JOptionPane.CLOSED_OPTION) {
            sendMatchDecline(name);
        }
    }

    // user: current user  | name: the other one
    private void onRoomJoined(String name) {
        RunClient.openScene(RunClient.SceneName.CHAT);
        RunClient.chatScene.setTitle(user);
        RunClient.chatScene.setUserName(name);
        RunClient.closeScene(RunClient.SceneName.WAITING);
    }

    private void onMatchDeclined(String name) {
        JOptionPane.showMessageDialog(RunClient.waitingScene.getContentPane(),
                name + " refused to chat with you!");
        sendWaiting(name);
    }

    public void CloseClient() {
        try {
            System.out.println("Connection Closed: " + socket);
            input.close();
            output.close();
            socket.close();

        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("CloseClient err!");
        }
    }
    public void sendAppClose(){
        sendData(StreamConst.Type.APP_CLOSED, "");
    }
    public String sendMessage(String text){
        return sendData(StreamConst.Type.MESSAGE, text);
    }
    public String sendMatchAccept(String name) {
        return sendData(StreamConst.Type.MATCH_ACCEPT, name);
    }

    public String sendMatchDecline(String name) {
        return sendData(StreamConst.Type.MATCH_DECLINE, name);
    }

    public String sendNameCheck(String name) {
//        System.out.println("send name check");
        return sendData(StreamConst.Type.NICKNAME_CHECK, name);
    }

    public String sendWaiting(String name) {
        return sendData(StreamConst.Type.WAITING, name);
    }

    public String sendLeaving(String name) {
        return sendData(StreamConst.Type.LEFT_ROOM, name);
    }

    public String sendData(StreamConst.Type type, String text) {
        System.out.println("send mess");
        try {
            output.writeUTF(type.toString() + ";" + text);
            System.out.println(type.toString() + ";" + text);
            output.flush();
            return "success";
        } catch (IOException ex) {
            return "send failed" + type.toString() + "-" + text;
        }
    }
}
