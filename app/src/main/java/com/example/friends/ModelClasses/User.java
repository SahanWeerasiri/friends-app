package com.example.friends.ModelClasses;

public class User {
    String user_name;
    String user_id;
    String email;
    String password;
    String status;
    String user_image;
    String msg;
    String type;
    String number;
    String ONLINE;


    public String getONLINE() {
        return ONLINE;
    }

    public void setONLINE(String ONLINE) {
        this.ONLINE = ONLINE;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    String time;
    String background;

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    String about;
    public User(){

    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public User(String msg, String time) {
        this.msg = msg;
        this.time = time;
    }

    public User(String user_name, String user_id, String email, String password, String status, String user_image) {
        this.user_name = user_name;
        this.user_id = user_id;
        this.email = email;
        this.password = password;
        this.status = status;
        this.user_image = user_image;

    }



    public User(String user_name, String email, String password) {
        this.user_name = user_name;
        this.email = email;
        this.password = password;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }
}
