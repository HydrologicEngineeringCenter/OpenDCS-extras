/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
/**
 *  Contains water year Julian day adjusted for leap year
 *  for various days
 * 
 * @author L2EDDMAN
 */
public class Dates {
    /**
     *
     * @param cur_day
     */       
    public Dates( Date cur_day ){
        //check leap year, adjust dates
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime( cur_day );
        if( cal.isLeapYear(cal.get( Calendar.YEAR)))
        {
            February29 += 1;
            March01 += 1;
            March02 += 1;
            March03 += 1;
            March04 += 1;
            March05 += 1;
            March06 += 1;
            March07 += 1;
            March08 += 1;
            March09 += 1;
            March10 += 1;
            March11 += 1;
            March12 += 1;
            March13 += 1;
            March14 += 1;
            March15 += 1;
            March16 += 1;
            March17 += 1;
            March18 += 1;
            March19 += 1;
            March20 += 1;
            March21 += 1;
            March22 += 1;
            March23 += 1;
            March24 += 1;
            March25 += 1;
            March26 += 1;
            March27 += 1;
            March28 += 1;
            March29 += 1;
            March30 += 1;
            March31 += 1;
            April01 += 1;
            April02 += 1;
            April03 += 1;
            April04 += 1;
            April05 += 1;
            April06 += 1;
            April07 += 1;
            April08 += 1;
            April09 += 1;
            April10 += 1;
            April11 += 1;
            April12 += 1;
            April13 += 1;
            April14 += 1;
            April15 += 1;
            April16 += 1;
            April17 += 1;
            April18 += 1;
            April19 += 1;
            April20 += 1;
            April21 += 1;
            April22 += 1;
            April23 += 1;
            April24 += 1;
            April25 += 1;
            April26 += 1;
            April27 += 1;
            April28 += 1;
            April29 += 1;
            April30 += 1;
            May01 += 1;
            May02 += 1;
            May03 += 1;
            May04 += 1;
            May05 += 1;
            May06 += 1;
            May07 += 1;
            May08 += 1;
            May09 += 1;
            May10 += 1;
            May11 += 1;
            May12 += 1;
            May13 += 1;
            May14 += 1;
            May15 += 1;
            May16 += 1;
            May17 += 1;
            May18 += 1;
            May19 += 1;
            May20 += 1;
            May21 += 1;
            May22 += 1;
            May23 += 1;
            May24 += 1;
            May25 += 1;
            May26 += 1;
            May27 += 1;
            May28 += 1;
            May29 += 1;
            May30 += 1;
            May31 += 1;
            June01 += 1;
            June02 += 1;
            June03 += 1;
            June04 += 1;
            June05 += 1;
            June06 += 1;
            June07 += 1;
            June08 += 1;
            June09 += 1;
            June10 += 1;
            June11 += 1;
            June12 += 1;
            June13 += 1;
            June14 += 1;
            June15 += 1;
            June16 += 1;
            June17 += 1;
            June18 += 1;
            June19 += 1;
            June20 += 1;
            June21 += 1;
            June22 += 1;
            June23 += 1;
            June24 += 1;
            June25 += 1;
            June26 += 1;
            June27 += 1;
            June28 += 1;
            June29 += 1;
            June30 += 1;
            July01 += 1;
            July02 += 1;
            July03 += 1;
            July04 += 1;
            July05 += 1;
            July06 += 1;
            July07 += 1;
            July08 += 1;
            July09 += 1;
            July10 += 1;
            July11 += 1;
            July12 += 1;
            July13 += 1;
            July14 += 1;
            July15 += 1;
            July16 += 1;
            July17 += 1;
            July18 += 1;
            July19 += 1;
            July20 += 1;
            July21 += 1;
            July22 += 1;
            July23 += 1;
            July24 += 1;
            July25 += 1;
            July26 += 1;
            July27 += 1;
            July28 += 1;
            July29 += 1;
            July30 += 1;
            July31 += 1;
            August01 += 1;
            August02 += 1;
            August03 += 1;
            August04 += 1;
            August05 += 1;
            August06 += 1;
            August07 += 1;
            August08 += 1;
            August09 += 1;
            August10 += 1;
            August11 += 1;
            August12 += 1;
            August13 += 1;
            August14 += 1;
            August15 += 1;
            August16 += 1;
            August17 += 1;
            August18 += 1;
            August19 += 1;
            August20 += 1;
            August21 += 1;
            August22 += 1;
            August23 += 1;
            August24 += 1;
            August25 += 1;
            August26 += 1;
            August27 += 1;
            August28 += 1;
            August29 += 1;
            August30 += 1;
            August31 += 1;
            September01 += 1;
            September02 += 1;
            September03 += 1;
            September04 += 1;
            September05 += 1;
            September06 += 1;
            September07 += 1;
            September08 += 1;
            September09 += 1;
            September10 += 1;
            September11 += 1;
            September12 += 1;
            September13 += 1;
            September14 += 1;
            September15 += 1;
            September16 += 1;
            September17 += 1;
            September18 += 1;
            September19 += 1;
            September20 += 1;
            September21 += 1;
            September22 += 1;
            September23 += 1;
            September24 += 1;
            September25 += 1;
            September26 += 1;
            September27 += 1;
            September28 += 1;
            September29 += 1;
            September30 += 1;
        }

    }
    
    public int October01 = 1;
    public int October02 = 2;
    public int October03 = 3;
    public int October04 = 4;
    public int October05 = 5;
    public int October06 = 6;
    public int October07 = 7;
    public int October08 = 8;
    public int October09 = 9;
    public int October10 = 10;
    public int October11 = 11;
    public int October12 = 12;
    public int October13 = 13;
    public int October14 = 14;
    public int October15 = 15;
    public int October16 = 16;
    public int October17 = 17;
    public int October18 = 18;
    public int October19 = 19;
    public int October20 = 20;
    public int October21 = 21;
    public int October22 = 22;
    public int October23 = 23;
    public int October24 = 24;
    public int October25 = 25;
    public int October26 = 26;
    public int October27 = 27;
    public int October28 = 28;
    public int October29 = 29;
    public int October30 = 30;
    public int October31 = 31;
    public int November01 = 32;
    public int November02 = 33;
    public int November03 = 34;
    public int November04 = 35;
    public int November05 = 36;
    public int November06 = 37;
    public int November07 = 38;
    public int November08 = 39;
    public int November09 = 40;
    public int November10 = 41;
    public int November11 = 42;
    public int November12 = 43;
    public int November13 = 44;
    public int November14 = 45;
    public int November15 = 46;
    public int November16 = 47;
    public int November17 = 48;
    public int November18 = 49;
    public int November19 = 50;
    public int November20 = 51;
    public int November21 = 52;
    public int November22 = 53;
    public int November23 = 54;
    public int November24 = 55;
    public int November25 = 56;
    public int November26 = 57;
    public int November27 = 58;
    public int November28 = 59;
    public int November29 = 60;
    public int November30 = 61;
    public int December01 = 62;
    public int December02 = 63;
    public int December03 = 64;
    public int December04 = 65;
    public int December05 = 66;
    public int December06 = 67;
    public int December07 = 68;
    public int December08 = 69;
    public int December09 = 70;
    public int December10 = 71;
    public int December11 = 72;
    public int December12 = 73;
    public int December13 = 74;
    public int December14 = 75;
    public int December15 = 76;
    public int December16 = 77;
    public int December17 = 78;
    public int December18 = 79;
    public int December19 = 80;
    public int December20 = 81;
    public int December21 = 82;
    public int December22 = 83;
    public int December23 = 84;
    public int December24 = 85;
    public int December25 = 86;
    public int December26 = 87;
    public int December27 = 88;
    public int December28 = 89;
    public int December29 = 90;
    public int December30 = 91;
    public int December31 = 92;
    public int January01 = 93;
    public int January02 = 94;
    public int January03 = 95;
    public int January04 = 96;
    public int January05 = 97;
    public int January06 = 98;
    public int January07 = 99;
    public int January08 = 100;
    public int January09 = 101;
    public int January10 = 102;
    public int January11 = 103;
    public int January12 = 104;
    public int January13 = 105;
    public int January14 = 106;
    public int January15 = 107;
    public int January16 = 108;
    public int January17 = 109;
    public int January18 = 110;
    public int January19 = 111;
    public int January20 = 112;
    public int January21 = 113;
    public int January22 = 114;
    public int January23 = 115;
    public int January24 = 116;
    public int January25 = 117;
    public int January26 = 118;
    public int January27 = 119;
    public int January28 = 120;
    public int January29 = 121;
    public int January30 = 122;
    public int January31 = 123;
    public int February01 = 124;
    public int February02 = 125;
    public int February03 = 126;
    public int February04 = 127;
    public int February05 = 128;
    public int February06 = 129;
    public int February07 = 130;
    public int February08 = 131;
    public int February09 = 132;
    public int February10 = 133;
    public int February11 = 134;
    public int February12 = 135;
    public int February13 = 136;
    public int February14 = 137;
    public int February15 = 138;
    public int February16 = 139;
    public int February17 = 140;
    public int February18 = 141;
    public int February19 = 142;
    public int February20 = 143;
    public int February21 = 144;
    public int February22 = 145;
    public int February23 = 146;
    public int February24 = 147;
    public int February25 = 148;
    public int February26 = 149;
    public int February27 = 150;
    public int February28 = 151;
    public int February29 = 151; // This lets us just have 1 added to it and the rest if it is leap year
    public int March01 = 152;
    public int March02 = 153;
    public int March03 = 154;
    public int March04 = 155;
    public int March05 = 156;
    public int March06 = 157;
    public int March07 = 158;
    public int March08 = 159;
    public int March09 = 160;
    public int March10 = 161;
    public int March11 = 162;
    public int March12 = 163;
    public int March13 = 164;
    public int March14 = 165;
    public int March15 = 166;
    public int March16 = 167;
    public int March17 = 168;
    public int March18 = 169;
    public int March19 = 170;
    public int March20 = 171;
    public int March21 = 172;
    public int March22 = 173;
    public int March23 = 174;
    public int March24 = 175;
    public int March25 = 176;
    public int March26 = 177;
    public int March27 = 178;
    public int March28 = 179;
    public int March29 = 180;
    public int March30 = 181;
    public int March31 = 182;
    public int April01 = 183;
    public int April02 = 184;
    public int April03 = 185;
    public int April04 = 186;
    public int April05 = 187;
    public int April06 = 188;
    public int April07 = 189;
    public int April08 = 190;
    public int April09 = 191;
    public int April10 = 192;
    public int April11 = 193;
    public int April12 = 194;
    public int April13 = 195;
    public int April14 = 196;
    public int April15 = 197;
    public int April16 = 198;
    public int April17 = 199;
    public int April18 = 200;
    public int April19 = 201;
    public int April20 = 202;
    public int April21 = 203;
    public int April22 = 204;
    public int April23 = 205;
    public int April24 = 206;
    public int April25 = 207;
    public int April26 = 208;
    public int April27 = 209;
    public int April28 = 210;
    public int April29 = 211;
    public int April30 = 212;
    public int May01 = 213;
    public int May02 = 214;
    public int May03 = 215;
    public int May04 = 216;
    public int May05 = 217;
    public int May06 = 218;
    public int May07 = 219;
    public int May08 = 220;
    public int May09 = 221;
    public int May10 = 222;
    public int May11 = 223;
    public int May12 = 224;
    public int May13 = 225;
    public int May14 = 226;
    public int May15 = 227;
    public int May16 = 228;
    public int May17 = 229;
    public int May18 = 230;
    public int May19 = 231;
    public int May20 = 232;
    public int May21 = 233;
    public int May22 = 234;
    public int May23 = 235;
    public int May24 = 236;
    public int May25 = 237;
    public int May26 = 238;
    public int May27 = 239;
    public int May28 = 240;
    public int May29 = 241;
    public int May30 = 242;
    public int May31 = 243;
    public int June01 = 244;
    public int June02 = 245;
    public int June03 = 246;
    public int June04 = 247;
    public int June05 = 248;
    public int June06 = 249;
    public int June07 = 250;
    public int June08 = 251;
    public int June09 = 252;
    public int June10 = 253;
    public int June11 = 254;
    public int June12 = 255;
    public int June13 = 256;
    public int June14 = 257;
    public int June15 = 258;
    public int June16 = 259;
    public int June17 = 260;
    public int June18 = 261;
    public int June19 = 262;
    public int June20 = 263;
    public int June21 = 264;
    public int June22 = 265;
    public int June23 = 266;
    public int June24 = 267;
    public int June25 = 268;
    public int June26 = 269;
    public int June27 = 270;
    public int June28 = 271;
    public int June29 = 272;
    public int June30 = 273;
    public int July01 = 274;
    public int July02 = 275;
    public int July03 = 276;
    public int July04 = 277;
    public int July05 = 278;
    public int July06 = 279;
    public int July07 = 280;
    public int July08 = 281;
    public int July09 = 282;
    public int July10 = 283;
    public int July11 = 284;
    public int July12 = 285;
    public int July13 = 286;
    public int July14 = 287;
    public int July15 = 288;
    public int July16 = 289;
    public int July17 = 290;
    public int July18 = 291;
    public int July19 = 292;
    public int July20 = 293;
    public int July21 = 294;
    public int July22 = 295;
    public int July23 = 296;
    public int July24 = 297;
    public int July25 = 298;
    public int July26 = 299;
    public int July27 = 300;
    public int July28 = 301;
    public int July29 = 302;
    public int July30 = 303;
    public int July31 = 304;
    public int August01 = 305;
    public int August02 = 306;
    public int August03 = 307;
    public int August04 = 308;
    public int August05 = 309;
    public int August06 = 310;
    public int August07 = 311;
    public int August08 = 312;
    public int August09 = 313;
    public int August10 = 314;
    public int August11 = 315;
    public int August12 = 316;
    public int August13 = 317;
    public int August14 = 318;
    public int August15 = 319;
    public int August16 = 320;
    public int August17 = 321;
    public int August18 = 322;
    public int August19 = 323;
    public int August20 = 324;
    public int August21 = 325;
    public int August22 = 326;
    public int August23 = 327;
    public int August24 = 328;
    public int August25 = 329;
    public int August26 = 330;
    public int August27 = 331;
    public int August28 = 332;
    public int August29 = 333;
    public int August30 = 334;
    public int August31 = 335;
    public int September01 = 336;
    public int September02 = 337;
    public int September03 = 338;
    public int September04 = 339;
    public int September05 = 340;
    public int September06 = 341;
    public int September07 = 342;
    public int September08 = 343;
    public int September09 = 344;
    public int September10 = 345;
    public int September11 = 346;
    public int September12 = 347;
    public int September13 = 348;
    public int September14 = 349;
    public int September15 = 350;
    public int September16 = 351;
    public int September17 = 352;
    public int September18 = 353;
    public int September19 = 354;
    public int September20 = 355;
    public int September21 = 356;
    public int September22 = 357;
    public int September23 = 358;
    public int September24 = 359;
    public int September25 = 360;
    public int September26 = 361;
    public int September27 = 362;
    public int September28 = 363;
    public int September29 = 364;
    public int September30 = 365;
    
}