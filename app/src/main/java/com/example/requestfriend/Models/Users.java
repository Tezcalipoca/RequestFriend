package com.example.requestfriend.Models;

import java.util.List;

public class Users {
    String profilePic, userName, email, password, userID;
    List<String> friends, blocked;

    public Users() {

    }

    public Users(String profilePic, String userName, String email, String password, String userID, String describe, String gender, List<String> friends, List<String> blocked) {
        this.profilePic = profilePic;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.userID = userID;
        this.friends = friends;
        this.blocked = blocked;
    }

    public Users(String userName, String email, String password, String userID, String statusActivity) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.userID = userID;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getBlocked() {
        return blocked;
    }

    public void setBlocked(List<String> blocked) {
        this.blocked = blocked;
    }
}
