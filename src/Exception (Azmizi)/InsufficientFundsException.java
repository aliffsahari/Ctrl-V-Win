/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author user
 */
public class InsufficientFundsException extends Exception{
    public double shortfall;  //shortfall will save duit tak cukup
    
    //constructor 1
    public InsufficientFundsException(String message){
        super(message);
        this.shortfall=0.0; 
    }
    
    //constructor 2
    public InsufficientFundsException(String message, double shortfall){
        super(message);
        this.shortfall = shortfall;
    }            
       
    public double getShortfall(){
        return shortfall;
    }    
}
