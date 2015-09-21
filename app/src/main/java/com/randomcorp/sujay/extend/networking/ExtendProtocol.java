package com.randomcorp.sujay.extend.networking;

/**
 * Created by sujay on 12/9/15.
 */
public interface ExtendProtocol
{
    String delimiter = " ";
    String serverStartHeader = "EXTENDSERVER";
    String clientStartHeader = "EXTENDCLIENT";
    String deviceNumberHeader = "DEVNO";
    String ackHeader = "ACK";
    String discoveryHeader = "DISCOVERY";
    String connectionRequestHeader = "CONNECT";
    String deviceInfoHeader = "DEVINFO";
    String deviceDetailsHeader = "DEVDETAILS";
    String syncHeader = "SYNC";
    String dataHeader = "DATA";
    String commandHeader = "COMMAND";
    String fileNameHeader = "FILE";
    String caliberationHeader = "CALIB";
    String commandWhite = "WHITE";
    String commandRed = "RED";
    String commandPlay = "PLAY";
    String commandPause = "PAUSE";
    String commandStop = "STOP";
    String commandSeek = "SEEK";
}
