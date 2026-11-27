/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author user
 */

/**
 * Represents an administrator account capable of managing student and merchant accounts.
 */
public class Admin {
    
    private String adminId;
    private String password;
    
    //constractor
    public Admin (String adminId, String password){
        this.adminId = adminId;
        this.password = password;
    }
    
    // method
    public String getAdminId(){
        return adminId;        
    }
    
     //password verification 
    public boolean checkPassword(String input){
        return input != null && this.password.equals(input);
    }
    
    public String toFileLine(){
        return adminId + "|" + password;
    }
}
