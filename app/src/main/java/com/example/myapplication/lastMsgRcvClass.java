package com.example.myapplication;

public class lastMsgRcvClass {
    public String ok;
    public String update_id;
    public String message_id;
    public String author_signature;
    public String chat_id;
    public String chat_title;
    public String chat_type;
    public String date;
    public String text;

    lastMsgRcvClass(){
        ok = "";
        update_id = "";
        message_id = "";
        author_signature = "";
        chat_id = "";
        chat_title = "";
        chat_type = "";
        date = "";
        text = "";
    }

    void setValues(String okC,String update_idC, String message_idC, String author_signatureC, String chat_idC, String chat_titleC, String chat_typeC, String dateC, String textC ) {
        ok = okC;
        update_id = Integer.toString(Integer.parseInt(update_idC) + 1);//update_idC;
        message_id = message_idC;
        author_signature = author_signatureC;
        chat_id = chat_idC;
        chat_title = chat_titleC;
        chat_type = chat_typeC;
        date = dateC;
        text = textC;
    }
}
