package N4M.app;

import static java.util.Arrays.copyOf;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Formatter;
import java.util.Scanner;

public class P6TestN4M {
    
    private static final int QUERYMAX = 65535;
    private static final String CHAR = "ASCII";
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            throw new IllegalArgumentException( "Parameter(s): <Server> <Port>");

        InetAddress server = InetAddress.getByName(args[0]);
        int servPort = Integer.parseInt(args[1]);

        try (DatagramSocket n4mSocket = new DatagramSocket()) {
            n4mSocket.connect(server, servPort);
        
            test1(n4mSocket);
            
            test2(n4mSocket, server, servPort);
            
            test3(n4mSocket, server, servPort);
            
            test4(n4mSocket, server, servPort);
            
            test5(n4mSocket);
            
            test6();
        }
    }
    
    private static void test1(DatagramSocket n4mSocket) throws IOException {
        System.out.println("Test 1");
        System.out.println("**********");
        System.out.println("Sending N4M Query");
        send(n4mSocket, "20120362697a");
        System.out.println("RCVD: " + receive(n4mSocket));
        System.out.println("EXPD: " + "28120000000000");
    }
    
    private static void test2(DatagramSocket n4mSocket, InetAddress server, int servPort) throws IOException {
        System.out.println("Test 2");
        System.out.println("**********");
        poll(server, servPort);
        System.out.println("Sending Query");
        send(n4mSocket, "20000162");
        System.out.println("RCVD: " + receive(n4mSocket));
        System.out.println("EXPD: " + "2800XXXXXXXX01000104506f6c6c");
        System.out.printf("NOW:      %s%n", Long.toHexString(new Date().getTime()/1000));
    }
    
    private static void test3(DatagramSocket n4mSocket, InetAddress server, int servPort) throws IOException {
        System.out.println("Test 3");
        System.out.println("**********");
        poll(server, servPort);
        System.out.println("Sending Query");
        send(n4mSocket, "20FF0162");
        System.out.println("RCVD: " + receive(n4mSocket));
        System.out.println("EXPD: " + "28ffXXXXXXXX01000204506f6c6c");
        System.out.printf("NOW:      %s%n", Long.toHexString(new Date().getTime()/1000));
    }
    
    private static void test4(DatagramSocket n4mSocket, InetAddress server, int servPort) throws IOException {
        System.out.println("Test 4");
        System.out.println("**********");
        for (int i=0; i < 4000; i++) {
            poll(server, servPort);
        }
        System.out.println("Sending Query");
        send(n4mSocket, "201E0162");
        System.out.println("RCVD: " + receive(n4mSocket));
        System.out.println("EXPD: " + "281eXXXXXXXX010fa204506f6c6c");
        System.out.printf("NOW:      %s%n", Long.toHexString(new Date().getTime()/1000));
    }
 
    private static void test5(DatagramSocket n4mSocket) throws IOException {
        System.out.println("Test 5");
        System.out.println("**********");
        System.out.println("Sending N4M Query");
        send(n4mSocket, "2012036269");
        System.out.println("RCVD: " + receive(n4mSocket));
        System.out.println("EXPD: " + "2a000000000000");
    }
    
    private static void test6() {
        System.out.println("Test 6");
        System.out.println("**********");
        System.out.println("Run manually");
    }
    /**
     * @param server
     * @param servPort
     */
    private static void poll(InetAddress server, int servPort) {
        try (Socket g8rSocket = new Socket(server, servPort); Scanner in = new Scanner(g8rSocket.getInputStream(),CHAR)) {
            in.useDelimiter("\r\n\r\n");
            //System.out.println("Sending G8R Request");
            g8rSocket.getOutputStream().write("G8R/1.0 Q RUN Poll\r\n\r\n".getBytes(CHAR));
            in.next();
            g8rSocket.getOutputStream().write("G8R/1.0 Q RUN NameStep Bob Smith\r\n\r\n".getBytes(CHAR));
            in.next();
            g8rSocket.getOutputStream().write("G8R/1.0 Q RUN FoodStep Mexican\r\nFName=Bob\r\nLName=Smith\r\n\r\n".getBytes(CHAR));
            in.next();
        } catch (Exception e) {
            System.err.println("Problem communicating with G8R Server: " + e.getMessage());
        }
    }
    private static void send(DatagramSocket socket, String hex) throws IOException {
        byte[] sndBuffer = hex2Bin(hex);
        DatagramPacket sendPacket = new DatagramPacket(sndBuffer, sndBuffer.length);
        socket.send(sendPacket);
    }
    
    private static String receive(DatagramSocket socket) throws IOException {
        DatagramPacket rcvDatagram = new DatagramPacket(new byte[QUERYMAX], QUERYMAX);
        socket.receive(rcvDatagram);
        return bin2Hex(copyOf(rcvDatagram.getData(), rcvDatagram.getLength()));
    }
    
    // https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java/140861#140861
    public static byte[] hex2Bin(String hex) {
        int len = hex.length();
        if (len % 2 != 0 || len == 0) {
            throw new ArithmeticException("Hex string must have positive, even length");
        }
        byte[] rv = new byte[len / 2];
        for (int i = 0; i < rv.length; i++) {
            rv[i] = (byte) ((Character.digit(hex.charAt(2*i), 16) << 4)
                                 + Character.digit(hex.charAt(2*i+1), 16));
        }
        return rv;
    }
    
    public static String bin2Hex(byte[] bin) {
        Formatter formatter = new Formatter();
        for (byte b : bin) {
            formatter.format("%02x", b);
        }
        String rv = formatter.toString();
        formatter.close();
        return rv;
    }
}
