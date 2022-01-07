/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Shared.StreamConst;
import java.util.ArrayList;

/**
 *
 * @author nguye
 */
public class Room {
    ArrayList<ClientThread> clients = new ArrayList<>();
    
    public void addClient(ClientThread c){
        clients.add(c);
    }
    // send this message to everyone in the room. <sender : massage>
    public void broadcast(String mess){
        for (ClientThread client : clients) {
            client.sendMessage(mess);
        }
    }
    // name: name of the user who left first
    public void endChat(String name){
        for (ClientThread client : clients) {
            client.sendData(StreamConst.Type.END_CHAT, name);
        }
    }
}
