package com.example.requestfriend.Models;

public class Friend {
    String userID, userName, userEmail, profilePic;

    public Friend() {
    }

    public Friend(String userID, String userName, String userEmail, String profilePic) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.profilePic = profilePic;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
