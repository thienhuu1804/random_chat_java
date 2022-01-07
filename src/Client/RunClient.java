package Client;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
public class RunClient {

    public enum SceneName {
        WAITING,
        LOGIN,
        CHAT
    }

    public static LoginForm loginScene;
    public static ChatPrivate chatScene;
    public static WaitingRoom waitingScene;

    public static ClientHandler socketHandler;

    public RunClient() {
        socketHandler = new ClientHandler();
        openScene(SceneName.LOGIN);
    }

    public static void openScene(SceneName scene) {
        switch (scene) {
            case WAITING:
                waitingScene = new WaitingRoom();
                waitingScene.setVisible(true);
                break;
            case LOGIN:
                loginScene = new LoginForm();
                loginScene.setVisible(true);
                break;
            case CHAT:
                chatScene = new ChatPrivate();
                chatScene.setVisible(true);
                break;
        }
    }
    
    public static void closeScene(SceneName scene){
        switch (scene) {
            case WAITING:
                waitingScene.dispose();
                break;
            case LOGIN:
                loginScene.dispose();
                break;
            case CHAT:
                chatScene.dispose();
                break;
        }
    }

    public static void main(String args[]) {
        new RunClient();
    }
}
