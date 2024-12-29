package com.example.friends.ModelClasses;

public class Chats {
    String msg;
    String time;
    String sender;
    String receiver;

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    String date;
    String seen;
    String type;
    String group;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    public String getMsgID2() {
        return msgID2;
    }

    public void setMsgID2(String msgID2) {
        this.msgID2 = msgID2;
    }

    String msgID2;

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    String msgID;

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

    public String getSender() {
        return sender;
    }

    public Chats() {
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }






    public Chats(String msg, String time,String date, String sender, String receiver,String seen,String type) {
        this.msg = msg;
        this.type=type;
        this.seen=seen;
        this.date=date;
        this.time = time;
        this.receiver=receiver;
        this.sender=sender;
    }

}
