/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.exceptions;

/**
 *
 * @author L2EDDMAN
 */
public class FormatException extends java.lang.Exception{
    public FormatException( Exception err, int line_no){
        super(err.getLocalizedMessage() + " on line " + line_no, err);        
        
    }
}
