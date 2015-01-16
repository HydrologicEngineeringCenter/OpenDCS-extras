/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

/**
 * This class is to convert old HADA subroutines, maybe for testing, but mostly for syntax highlighting
 * and reorganization
 * @author L2EDDMAN
 */
public class HADA {
    //                  NOPRO      NUSRM   URTOTM   URESRI
    //                  Project    Number  Total    Reduction   SnoCO1  SnoCO2
    //                  # in DIFL  of Res  Credit
    static String data = "23        2       628     0           0       0"
    //                    NUMRES    URESG   URESP       DRESP        DREST         RAPER   USRSM1  USRSM2  FDAY    NAPPRA  NAPPSN
    //                     Res#    Res Max  Max Credit  % Of Total   % of Res              Max     Max     DAY     Affects Affects
    //                              Credit  %s          Replaced By  replaced By           Credit  Credit  TO      Rain    Snow
    //                                                  Res          Res                   Before  After   USE     Flood?  Flood
    //                                                                                     FDAY    FDAY    USRSM2
            +            "1  HH     360.0   .8          .7           2             0       360.0   360.0   124     0       1"
            +            "2  CV     268.0   .8          .3           2             0       268.0   268.0   124     0       1";
    
    /*
     * URESG = max_credit[]
     * URESP = max_percent_of_total[]
     * DRESP = percent_of_total[]
     * DREST = drest []
     * RAPER = percent_transferable[]
     * USRSM1 = max_space_before_fday[]
     * USRSM2 = max_space_after_fday[]
     * FDAY = fday[]
     * NPPRA = affects_rain_flood[]
     * NPPSN = affects_snow_flood[]
     * 
     */
    
    private double max_credit[];            // USREG
    private double max_percent_of_total[];  // URESP
    private double percent_of_total[];      // DRESP
    private double drest[];                 // DREST
    private double percent_transferable[];  // RAPER
    private double upstream_credit_transfer[]; //URTRAS
    private double max_space_before_fday[]; // USRSM1
    private double max_space_after_fday[];  // USRSM2
    private double fday[];                  // FDAY
    private int affects_rain_flood[];    // NPPRA
    private int affects_snow_flood[];    // NPPSN
    private double reduction;               // URESI
    private double snoco1;
    private double snoco2;
    private double max_total_credit;        // URTOTM
            
    
    public HADA(){
        /* 
         * this just sets up for Don Pedro as that's what I'm working on
         */
        
        max_credit = new double[2];
        max_credit[0] = 360000.0;
        max_credit[1] = 268000.0;
        
        max_percent_of_total = new double[2];
        max_percent_of_total[0] = .8;
        max_percent_of_total[1] = .8;
        
        percent_of_total = new double[2];
        percent_of_total[0] = .7;
        percent_of_total[1] = .3;
        
        drest = new double[2];
        drest[0] = 2;
        drest[0] = 2;
        
        percent_transferable = new double[2];
        percent_transferable[0] = 0.0;
        percent_transferable[1] = 0.0;
        
        max_space_before_fday = new double[2];
        max_space_before_fday[0] = 360000.0;
        max_space_before_fday[1] = 268000.0;
        
        max_space_after_fday = new double[2];
        max_space_after_fday[0] = 360000.0;
        max_space_after_fday[1] = 268000.0;
        
        fday = new double[2];
        fday[0] = 124;
        fday[1] = 124;
        
        affects_rain_flood = new int[2];
        affects_rain_flood[0] = 0;
        affects_rain_flood[1] = 0;
        
        affects_snow_flood = new int[2];
        affects_snow_flood[0] = 1;
        affects_snow_flood[1] = 1;
        
        max_total_credit = 628000.0;
        reduction = 0.0;
        snoco1 = 0;
        snoco2 = 0;
    }
    
    
            
    double [] subup( int day, double upstream_storage[], double space_available, double transferable_snow_space   )
    {
        // the index res will always mean the current upstream reservoir
        double upstream_credits[] = new double[4]; // values we calculated and pass back
        double total_credit = 0.0;
        // initialize credits
        for( int i = 0; i < upstream_credits.length; i++ ){
            upstream_credits[i] = 0.0;
        }
        
        // calculate the total credit
        for( int res = 0; res < upstream_storage.length; res++){
            total_credit += max_percent_of_total[res] * ( max_credit[res] - upstream_storage[res] );
        }
        
        
        for( int res=0; res < upstream_storage.length; res++) // for each upstream reservoir
        {               
               for( int n=0; n < upstream_credits.length; n++) // for each upstream credit calculation
               {
                   double tmp = 0.0; // this was dum in the old fortran code, I'm going to assume that stood for Dummy
                   if( (n==0 && affects_rain_flood[res] != 1 ) ||
                       (n==1 && affects_rain_flood[res] != 2 ) ||
                       (n==2 && affects_snow_flood[res] != 1 ) ||
                       (n==3 && affects_snow_flood[res] != 2 )
                     ){
                       //do nothing if not the right for this credit/resevoir combintation
                   }
                   else{
                       //do stuff
                       if( day < fday[res]){                           
                           tmp = max_percent_of_total[res] * ( Math.abs(max_space_before_fday[res]) - upstream_storage[res]  );
                           if( tmp < 0 && (fday[res] - day)<31 ){ 
                               //prorate tmp if negative
                               tmp = tmp * ( (fday[res]- day)/31.0 );      
                               if( max_space_before_fday[res] >= 0.0 && tmp < 0.0){                                   
                                   tmp = 0.0;
                               }                               
                           }                           
                       }
                       else{
                           tmp = max_percent_of_total[res] * ( Math.abs( max_space_after_fday[res] - upstream_storage[res] ) );
                           if( max_space_after_fday[res] >=0 && tmp < 0 ){                               
                                tmp =0.0;
                           }                                                      
                       }                                             
                       if( n >= 3 ){
                           // this is only used for commanche
                           tmp += percent_transferable[res] * upstream_credit_transfer[res];
                       }                       
                       if( tmp > max_credit[res] ){
                           tmp = max_credit[res];
                       }                       
                       // I strong feel this shouldn't be total credit here
                       // M. Neilson 4/22/2014
                       if( percent_of_total[res] <= 1 && tmp > percent_of_total[res] * total_credit){
                           tmp = percent_of_total[res] * total_credit;
                       }              
                       /*
                       if( drest[res] <= 1){
                           if( n <= 2 ){
                               if( tmp > drest[res]*space_available ){
                                   tmp = drest[res]*space_available;
                               }                               
                           }
                           else{
                               if( tmp > drest[res]*transferable_snow_space ){
                                   tmp = drest[res]*transferable_snow_space;
                               }
                           }
                       }
                       */
                       upstream_credits[n] += tmp;
                       
                           
                       
                   }
                   
                       
               }
            
               
            
            
            
        }
        // loops are done
               double uradj = Math.abs( reduction )*(  1.0 - Math.exp(snoco1*(snoco2-day )) );
               double uradj3 = uradj;
               double uradj1 = uradj;
               
               if( uradj > upstream_credits[2]){
                   uradj3 = upstream_credits[2];
               }
               
               upstream_credits[2] -= uradj3;
               
               if( reduction < 0.0 ){
                   if( uradj > upstream_credits[0]){
                       uradj1 = upstream_credits[0];
                   }
                   upstream_credits[0] -= uradj1;
               }
                      
               if( upstream_credits[0] > max_total_credit ){
                   upstream_credits[0] = max_total_credit;
               }
        
        return upstream_credits;
    }
    
    
    
    
}
