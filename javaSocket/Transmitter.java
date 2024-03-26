package javaSocket;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.lang.*;

public class Transmitter {

    private static final int MAX_PACKET_SIZE = 60 * 1024; // 60 KB max. Übertragungsgröße pro paket
    private static final String FILE_NAME = "/C://Users//Startklar//Downloads//2023-05-17-RA-Test3.pdf/"; // 49 Bytes lang
    private static final String DESTINATION_IP = "127.0.0.1";
    private static final int DESTINATION_PORT = 3000;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();

            sendFile(socket);

            socket.close();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(DatagramSocket socket) throws IOException, NoSuchAlgorithmException {
        FileInputStream fileInputStream = new FileInputStream(new File(FILE_NAME));
        byte[] buffer = new byte[MAX_PACKET_SIZE];
        int bytesRead = 0;
        int sequenceNumber = 0;
        int transmissionID = 1;

        MessageDigest md = MessageDigest.getInstance("MD5");

        int totalBytesRead = 0;
        long fileSize = new File(FILE_NAME).length();


        //erstes Paket
        byte[] transIDBytes = shortToBytes(transmissionID); // wandle transmission id in byte-array
        byte[] seqNumberBytes = intToBytes(sequenceNumber); // wandle sequence number in byte-array
        byte[] maxSeqNumber = intToBytes(Integer.MAX_VALUE);
        byte[] fileNameBytes = FILE_NAME.getBytes("UTF-8");
        byte[] firstPaket = new byte[266];  // größe zum übertragen des ersten pakets sind 266 byte maximal
        System.arraycopy(transIDBytes, 0, firstPaket, 0, 2); // kopiere transmission id in data[]
        System.arraycopy(seqNumberBytes, 0, firstPaket, 2, 4); // kopiere sequence number in data[]
        System.arraycopy(maxSeqNumber, 0, firstPaket, 6, 4); // kopiere max sequence number in data[]
        System.arraycopy(fileNameBytes, 0, firstPaket, 10, fileNameBytes.length); // kopiere file name in data[]

        DatagramPacket packet = new DatagramPacket(firstPaket, firstPaket.length, InetAddress.getByName(DESTINATION_IP), DESTINATION_PORT);
        socket.send(packet);  // sende paket

        sequenceNumber++;
        transmissionID++;


        // zweites bis n-1tes Paket
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {  // daten in buffer[] lesen
            totalBytesRead += bytesRead;
            transIDBytes = shortToBytes(transmissionID); // wandle transmission id in byte-array
            seqNumberBytes = intToBytes(sequenceNumber); // wandle sequence number in byte-array
            byte[] data = new byte[bytesRead + 6];  // 60 kb + 6 b
            System.arraycopy(transIDBytes, 0, data, 0, 2); // kopiere transmission id in data[]
            System.arraycopy(seqNumberBytes, 0, data, 2, 4); // kopiere sequence number in data[]
            System.arraycopy(buffer, 0, data, 6, bytesRead); // kopiere buffer in data[]

            packet = new DatagramPacket(data, data.length, InetAddress.getByName(DESTINATION_IP), DESTINATION_PORT);
            socket.send(packet);  // sende paket

            // Update MD5 checksum
            md.update(buffer, 0, bytesRead);

            sequenceNumber++;
            transmissionID++;

        }


        byte[] lastPacket = new byte[22]; // letztes Paket ist 22 Byte lang
        System.arraycopy(transIDBytes, 0, lastPacket, 0, 2); // kopiere transmission id in data[]
        System.arraycopy(seqNumberBytes, 0, lastPacket, 2, 4); // kopiere sequence number in data[]
        byte[] mdBytes = md.digest();
        System.arraycopy(mdBytes, 0, lastPacket, 6, 16);

        DatagramPacket eofPacket = new DatagramPacket(lastPacket, lastPacket.length, InetAddress.getByName(DESTINATION_IP), DESTINATION_PORT);
        socket.send(eofPacket);

        fileInputStream.close();

        System.out.println("MD5 checksum: " + bytesToHex(mdBytes));
    }

    private static byte[] intToBytes(int value) { // 4 Byte
        return new byte[] {
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }

    private static byte[] shortToBytes(int value) {  // 2 Byte
        return new byte[] {
                (byte) (value >> 8),
                (byte) value
        };
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }




















    /*
    public static void send(String message, String ipAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(ipAddress);

        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), address, port);
        socket.send(packet);
        socket.close();
    } */


}