
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author user
 */
public class AccountFrozenException extends Exception{
    // Thrown when a transaction involves an account whose frozen status is true.
    public AccountFrozenException(String message){
        super(message);
    }
}
