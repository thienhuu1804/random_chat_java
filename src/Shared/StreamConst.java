/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared;

/**
 *
 * @author nguye
 */
public class StreamConst {

    public enum Type {
        NICKNAME_CHECK,
        NICKNAME_ACCEPT,
        NICKNAME_DECLINE,
        MESSAGE,
        WAITING,
        ROOM_EMPTY,
        MATCH_FOUND,
        MATCH_ACCEPT,
        MATCH_DECLINE,
        LEFT_ROOM,
        ROOM_JOINED,
        END_CHAT,
        APP_CLOSED,
        UNKNOWN_TYPE;
    }

    public static Type getType(String typeName) {
        Type result = Type.UNKNOWN_TYPE;

        try {
            result = Enum.valueOf(Type.class, typeName);
        } catch (Exception e) {
            System.err.println("Unknow type: " + e.getMessage());
        }

        return result;
    }

    public static Type getTypeFromData(String data) {
        String typeStr = data.split(";")[0];
        return getType(typeStr);
    }
}
