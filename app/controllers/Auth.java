package controllers;
 
import play.modules.gae.GAE;

import com.google.appengine.api.users.User;
 
public class Auth {
 
    public static boolean isLoggedIn() {
        return GAE.isLoggedIn();
    }
    public static String getEmail() {
        return GAE.getUser().getEmail();
    }
    public static User getUser() {
        return GAE.getUser();
    }
 
    public static void login(String action) {
        GAE.login(action);
    }
 
    public static void logout(String action) {
        GAE.logout(action);
    }
}