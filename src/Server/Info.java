package Server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.net.Socket;
import java.util.ArrayList;


public class Info {
    private String username;
    public enum STATUS{
        ACCEPTED,
        DECLINED,
        WAITING
    };
    
    private STATUS status = STATUS.WAITING;
    private ArrayList<String> declinedList = new ArrayList<>();

    public Info(String username) {
        this.username = username;
    }
    
    public void addDeclined(String name){
        declinedList.add(name);
    }
    
    public boolean declined(String nickname){
        return declinedList.contains(nickname);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }   

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public ArrayList<String> getDeclinedList() {
        return declinedList;
    }
    public void removeDeclined(String name){
        declinedList.remove(name);
    }
    
}
