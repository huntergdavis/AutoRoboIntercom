package com.hunterdavis.autorobointercom.network;

/**
 * Created by hunter on 3/3/14.
 */
public class NetworkConstants {

    //
    /*
                                             ,
                                   ,   ,'|
                                 ,/|.-'   \.
                              .-'  '       |.
                        ,  .-'              |
                       /|,'                 |'
                      / '                    |  ,
                     /                       ,'/
                  .  |          _              /
                   \`' .-.    ,' `.           |
                    \ /   \ /      \          /
                     \|    V        |        |  ,
                      (           ) /.--.   ''"/
                      "b.`. ,' _.ee'' 6)|   ,-'
                        \"= --""  )   ' /.-'
                         \ / `---"   ."|'
      it's OVER 9000!     \"..-    .'  |.
                           `-__..-','   |
                         __.) ' .-'/    /\._
                   _.--'/----..--------. _.-""-._
                .-'_)   \.   /     __..-'     _.-'--.
               / -'/      """""""""         ,'-.   . `.
              | ' /                        /    `   `. \
              |   |                        |         | |
               \ .'\                       |     \     |
              / '  | ,'               . -  \`.    |  / /
             / /   | |                      `/"--. -' /\
            | |     \ \                     /     \     |
     _Seal_ | \      | \                  .-|      |    |

     */
    public static int DEFAULT_PORT = 9001;


    /*
                (

                )
              ( _   _._
               |_|-'_~_`-._
            _.-'-_~_-~_-~-_`-._
        _.-'_~-_~-_-~-_~_~-_~-_`-._
       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
         |  []  []   []   []  [] |
     jgs |           __    ___   |
       ._|  []  []  | .|  [___]  |_._._._._._._._._._._._._._._._._.
       |=|________()|__|()_______|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|=|
     ^^^^^^^^^^^^^^^ === ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         ___________   ===
        <_224.0.0.1_>    ===
              ^|^          ===
               |              ===
     */
    // Which multicast group address
    public static String DEFAULT_GROUP = "224.0.0.1";

    public static int DEFAULT_DATAGRAM_SIZE = 1400;

    public static String BROADCAST_ACTION = "networkbroadcastmessage";

    public static String BROADCAST_EXTRA_STRING_UDP_MESSAGE = "extrastringudpmessage";

    public static String BROADCAST_EXTRA_SPECIAL_CHARACTER_DELIMINATOR = ";;";

    public static String NON_SPOKEN_EXTRA_CHARACTER_DELIMINATOR = "--";


    // battery info
    public static String BATTERY_REQUEST = "batteryRequest";
    public static String BATTERY_CONFIRMATION = "batteryConfirmation";

}
