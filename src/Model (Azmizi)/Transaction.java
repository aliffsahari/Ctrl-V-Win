/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.insufficientfundexception;

/**
 *
 * @author user
 */
public class Transaction {
    private String transactionId;
    private String senderId;
    private String receiverId;
    private String type;
    private double amount;
    private double fee;
    private String status;
    private String dateTime;
    
    //constructor
    public Transaction(String transactionId, String senderId, String type, double amount, double fee, String status, String dateTime){
        this.transactionId = transactionId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.amount = amount;
        this.fee = fee;
        this.status = status;
        this.dateTime = dateTime;
    }
    
    //method getter
    public String getTransactionId(){
        return transactionId;
    }    
    public String getSenderId(){
        return senderId;
    }
    public String getReceiverId(){
        return receiverId;
    }
    public String getType(){
        return type;
    }
    public double getAmount(){
        return amount;
    }
    public double getFee(){
        return fee;
    }
    public String getStatus(){
        return status;
    }
    public String getDateTime(){
        return dateTime;
    }
    
    public String toFileLine(){
        return transactionId + "|" + senderId + "|" + receiverId + "|" + type + "|" + amount + "|" + status + "|" + dateTime;
    }
    
    //a better format transaction
    @Override
    public String toString(){
return String.format("[%s] %s | %-8s | From: %s -> To: %s | RM %.2f (Fee: RM %.2f) | %s",
                dateTime, transactionId, type, senderId, receiverId, amount, fee, status);
    }
}
