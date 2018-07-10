/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.db.test;

import decodes.tsdb.DataCollection;

/**
 * base data for used by Unit Tests
 * handles loading data
 * @author L2EDDMAN
 */
public class Fixtures {
    private static Fixtures fixtures = null;
    private DataCollection dc = null;
    
    /**
     * 
     */
    
    
    private Fixtures(){
        dc = new DataCollection();
        
    }
    
    public static Fixtures getFixtures(){
        if( fixtures == null ){
            fixtures = new Fixtures();
            fixtures.loadData();
        } 
        return fixtures;
    }
    
    public DataCollection getDC(){
        return dc;
    }

    /**
     * load our data from files
     */
    private void loadData() {
        
    }
    
    
    
}
