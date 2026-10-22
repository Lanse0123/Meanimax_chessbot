package lanse.worldwarchess.steam;

import com.codedisaster.steamworks.*;

import java.util.ArrayList;
import java.util.List;

public class Account implements SteamUserCallback, SteamFriendsCallback {
    //this initializes instantly so this class can always be used to get your own steam info.
    public static SteamUser user;
    private static SteamFriends friends;
    private static Account thiz;
    private static final List<String> devList = new ArrayList<>();

    static {
        devList.add("Lanse");
        devList.add("james.m.felder");
    }

    public static void init() {
        thiz = new Account();
        user = new SteamUser(thiz);
        friends = new SteamFriends(thiz);
    }

    public static boolean steamSetupWorked() {
        return user != null;
    }

    public static String username() {
        return friends.getPersonaName();
    }

    public static String username(SteamID friend) {return friends.getFriendPersonaName(friend);}

    public static SteamID getMySteamID(){ return user.getSteamID(); }

    public static boolean isDev(){
        return devList.contains(username());
    }
}
