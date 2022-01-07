package Server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import Shared.StreamConst;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread extends Thread {

    DataInputStream input = null;
    DataOutputStream output = null;
    Socket socket;
    Info info;
    ClientThread temp_matched_client;
    Room joined_room;
    boolean listening;

    public ClientThread() {
    }

    ClientThread(Socket s) {
        this.socket = s;
        info = new Info("");
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setTempClient(ClientThread c) {
        this.temp_matched_client = c;
    }

    public void setJoinedRoom(Room r) {
        this.joined_room = r;
    }

    @Override
    public void run() {
        listening = true;
        while (listening) {
            try {
                String received = input.readUTF();
                System.out.println(received);
                StreamConst.Type type = StreamConst.getTypeFromData(received.split(";")[0]);
//                String mess = recieved.split(";")[0];
                switch (type) {
                    case NICKNAME_CHECK:
                        onNicknameCheck(received);
                        break;
                    case WAITING:
                        onWaiting();
                        break;
                    case MATCH_ACCEPT:
                        onMatchAccept(received);
                        break;
                    case MATCH_DECLINE:
                        onMatchDecline(received);
                        break;
                    case MESSAGE:
                        onMessageReceived(received);
                        break;
                    case LEFT_ROOM:
                        onLeftRoom(received);
                        break;
                    case APP_CLOSED:
                        onAppClosed(received);
                        break;
                }
            } catch (IOException ex) {
                System.out.println("Mat ket noi");
                listening = false;
            }
        }
    }

    private void onAppClosed(String data) {
        synchronized (RunServer.takenNameList) {
            synchronized (RunServer.waitingList) {
                RunServer.takenNameList.remove(info.getUsername());
                if (RunServer.waitingList.contains(this)) {
                    RunServer.waitingList.remove(this);
                }
            }
        }
        for (int i = 0; i < RunServer.allClients.size(); i++) {
            if (RunServer.allClients.get(i).info.declined(info.getUsername())) {
                RunServer.allClients.get(i).info.removeDeclined(info.getUsername());
            }
        }
        RunServer.allClients.remove(this);
        listening = false;
        this.temp_matched_client.setTempClient(null);
    }

    private void onLeftRoom(String data) {
        joined_room.endChat(info.getUsername());
        joined_room = null;
        temp_matched_client.joined_room = null;
    }

    private void onMessageReceived(String data) {
        String text = data.split(";")[1];
        joined_room.broadcast(info.getUsername() + " : " + text);
    }

    private void decide() {
        if (temp_matched_client != null) {
            if (this.info.getStatus() != Info.STATUS.WAITING
                    && temp_matched_client.info.getStatus() != Info.STATUS.WAITING) {
                if (this.info.getStatus() == Info.STATUS.DECLINED
                        || temp_matched_client.info.getStatus() == Info.STATUS.DECLINED) {
                    info.addDeclined(temp_matched_client.info.getUsername());
                    temp_matched_client.info.addDeclined(this.info.getUsername());

                    sendMatchDeclined(temp_matched_client.info.getUsername());
                    temp_matched_client.sendMatchDeclined(this.info.getUsername());
                } else if (this.info.getStatus() == Info.STATUS.ACCEPTED
                        || temp_matched_client.info.getStatus() == Info.STATUS.ACCEPTED) {
                    addToRoom(this, temp_matched_client);
                }
            }
        } else {
            sendMatchDeclined("Someone");
        }
    }

    private void onMatchDecline(String data) {
        String name = data.split(";")[1];
        info.setStatus(Info.STATUS.DECLINED);
        decide();
    }

    private void onMatchAccept(String data) {
        String name = data.split(";")[1];
        this.info.setStatus(Info.STATUS.ACCEPTED);
        decide();
    }

    private void onNicknameCheck(String data) {
        String name = data.split(";")[1];
        synchronized (RunServer.takenNameList) {
            if (RunServer.takenNameList.contains(name)) {
                sendData(StreamConst.Type.NICKNAME_DECLINE, name);
            } else {
                RunServer.takenNameList.add(name);
                this.info.setUsername(name);
                sendData(StreamConst.Type.NICKNAME_ACCEPT, name);
            }
        }
    }

    private int onWaiting() {
        this.info.setStatus(Info.STATUS.WAITING);
        synchronized (RunServer.takenNameList) {
            synchronized (RunServer.waitingList) {
                if (!RunServer.waitingList.isEmpty()) {
                    for (int i = 0; i < RunServer.waitingList.size(); i++) {
                        temp_matched_client = RunServer.waitingList.get(i);
                        if (!temp_matched_client.info.getUsername().equalsIgnoreCase(this.info.getUsername())) {
                            if (!this.info.declined(RunServer.waitingList.get(i).info.getUsername())
                                    && !RunServer.waitingList.get(i).info.declined(this.info.getUsername())) {
                                temp_matched_client.setTempClient(this);
                                RunServer.waitingList.remove(temp_matched_client);
                                System.out.println("Temporary removed " + temp_matched_client.info.getUsername() + " from waiting room.");
                                sendMatchFound(temp_matched_client.info.getUsername());
                                temp_matched_client.sendMatchFound(this.info.getUsername());
                                return 1;
                            }
                        }
                    }
                }
                RunServer.waitingList.add(this);
                temp_matched_client = null;
                System.out.println("Added " + this.info.getUsername() + " to waiting room.");
                System.out.println("Currently in waitingroom: " + RunServer.waitingList.size());
                System.out.println("===================||====================");
                return 0;
            }
        }
    }

    public void addToRoom(ClientThread c1, ClientThread c2) {
        joined_room = new Room();
        joined_room.addClient(c1);
        joined_room.addClient(c2);
        c2.setJoinedRoom(joined_room);
        System.out.println("Currently in waitingroom: " + RunServer.waitingList.size());
        System.out.println("New ChatRoom: " + joined_room.clients.get(0).info.getUsername()
                + " |and| " + joined_room.clients.get(1).info.getUsername());
        System.out.println("===================||====================");
        sendRoomJoined(c2.info.getUsername());
        c2.sendRoomJoined(this.info.getUsername());
    }

    public void sendRoomJoined(String name) {
        sendData(StreamConst.Type.ROOM_JOINED, name);
    }

    public void sendMatchFound(String name) {
        sendData(StreamConst.Type.MATCH_FOUND, name);
    }

    public void sendMatchDeclined(String name) {
        sendData(StreamConst.Type.MATCH_DECLINE, name);
    }

    public void sendMessage(String data) {
        sendData(StreamConst.Type.MESSAGE, data);
    }

    public String sendData(StreamConst.Type type, String mess) {
        String data = type.toString() + ";" + mess;
        return sendData(data);
    }

    public String sendData(String data) {
        try {
            output.writeUTF(data);
            output.flush();
            return "success";
        } catch (IOException ex) {
//            System.err.println("Send data failed to " + this.loginPlayer.getEmail());
            return "failed;" + ex.getMessage();
        }
    }

    public static void main(String[] args) {
//        System.out.println(StreamConst.Type.MATCH_ACCEPT.toString());
        ClientThread sv = new ClientThread();
        sv.setName("abc");
        System.out.println(sv.getName());
    }
}
