package G8R.test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class G8RServerTest {

    private static final String SERVER = "localhost";
    private static final int PORT = 12345;
    private static final String ENC = "ASCII";
    private static final int SLOWDELAYMS = 100;

    private Socket clientSocket;

    @BeforeEach
    protected void before() throws UnknownHostException, IOException {
        clientSocket = new Socket(SERVER, PORT);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        int v = clientSocket.getInputStream().read();
                        if (v == -1) {
                            System.out.println();
                            return;
                        }
                        System.out.print((char) v);
                    }
                } catch (@SuppressWarnings("unused") IOException e) {
                }
            }
        }).start();
    }

    @AfterEach
    protected void after() throws IOException {
        clientSocket.close();
    }

    @BeforeAll
    protected static void announcement() {
        System.out.println("1.  Ignore order of cookies in response.");
        System.out.println("2.  Asterisk (*) by cookie means optional.");
        System.out.println("3.  The + x% is optional.");
    }

    @Test
    protected void TestBasic() throws Exception {
        printTest("BC [Basic] - 10 points");

        printExpected("G8R/1.0 R OK NameStep Name (First Last)>");
        sendSlowly(clientSocket, "G8R/1.0 Q RUN Poll\r\n\r\n");

        printExpected("G8R/1.0 R OK FoodStep Bob's Food Mood>\nFName=Bob\nLName=Smith\n");
        send(clientSocket, "G8R/1.0 Q RUN NameStep Bob Smith\r\n\r\n");

        printExpected(
                "G8R/1.0 R OK NULL 25% + 1% off at Pastatic\nFName=Bob\nLName=Smith\nRepeat=1");
        send(clientSocket, "G8R/1.0 Q RUN FoodStep Italian\r\nLName=Smith\r\nFName=Bob\r\n\r\n");
    }

    @Test
    protected void TestName() throws Exception {
        printTest("NM [Name] - 10 points");
        
        printExpected("G8R/1.0 R OK FoodStep Bob's Food Mood>\nFName=Bob\nLName=Smith\nRepeat=1");
        send(clientSocket, "G8R/1.0 Q RUN Poll\r\nFName=Bob\r\nLName=Smith\r\nRepeat=1\r\n\r\n");
        
        printExpected("G8R/1.0 R OK NULL 20% + 2% off at Tacopia\nFName=Bob\nLName=Smith\nRepeat=2");
        send(clientSocket, "G8R/1.0 Q RUN FoodStep Mexican\r\nFName=Bob\r\nLName=Smith\r\nRepeat=1\r\n\r\n");
    }

    @Test
    protected void TestRepeat() throws Exception {
        printTest("RP [Repeat] - 10 points");
        
        printExpected("G8R/1.0 R OK FoodStep Bob's Food Mood>\nFName=Bob\nLName=Smith\nRepeat=1");
        send(clientSocket, "G8R/1.0 Q RUN Poll\r\nFName=Bob\r\nLName=Smith\r\nRepeat=1\r\n\r\n");
        
        printExpected("G8R/1.0 R OK NULL 10% + 3% off at McDonalds\nFName=Bob\nLName=Smith\nRepeat=2");
        send(clientSocket, "G8R/1.0 Q RUN FoodStep X\r\nFName=Bob\r\nLName=Smith\r\nRepeat=1\r\n\r\n");
    }

    @Test
    protected void TestTransitionBad() throws Exception {
        printTest("TB [Transition Bad] - 10 points");
        
        printExpected("G8R/1.0 R OK NameStep Name (First Last)>");
        send(clientSocket, "G8R/1.0 Q RUN Poll\r\n\r\n");
        
        printExpected("G8R/1.0 R ERROR NULL <Some message about unexpected>");
        send(clientSocket, "G8R/1.0 Q RUN FoodStep Chicken\r\nFName=Bob\r\nLName=Smith\r\n\r\n");
    }

    @Test
    protected void TestCommandBad() throws Exception {
        printTest("CB [Command Bad] - 10 points");
        
        printExpected("G8R/1.0 R ERROR NULL <Some message about unexpected command>");
        send(clientSocket, "G8R/1.0 Q YURP Poll\r\n\r\n");
    }

    @Test
    protected void TestParameterBad() throws Exception {
        printTest("PB [Parameter Bad] - 10 points");
        
        printExpected("G8R/1.0 R OK NameStep Name (First Last)>");
        send(clientSocket, "G8R/1.0 Q RUN Poll\r\n\r\n");
        
        printExpected("G8R/1.0 R ERROR NameStep <Some error about name>. Name (First Last)>");
        send(clientSocket, "G8R/1.0 Q RUN NameStep Yurp\r\n\r\n");
        
        printExpected("G8R/1.0 R OK FoodStep Bob's Food Mood>\nFName=Bob\nLName=Smith");
        send(clientSocket, "G8R/1.0 Q RUN NameStep Bob Smith\r\n\r\n");
        
        printExpected(
                "G8R/1.0 R OK NULL 25% off at Pastatic\nFName=Bob\nLName=Smith\nRepeat=1");
        send(clientSocket, "G8R/1.0 Q RUN FoodStep Italian\r\nFName=Bob\r\nLName=Smith\r\n\r\n");
    }

    @Test
    protected void TestRepeatBad() throws Exception {
        printTest("RB [Repeat Bad] - 10 points");
        
        printExpected("Should gracefully handle bad Repeat value");
        send(clientSocket, "G8R/1.0 Q RUN Poll\r\nFName=Bob\r\nLName=Smith\r\nRepeat=Z\r\n\r\n");
        send(clientSocket, "G8R/1.0 Q RUN FoodStep Mexican\r\nFName=Bob\r\nLName=Smith\r\nRepeat=Z\r\n\r\n");
    }
    
    @Test
    protected void TestThreadPool() throws Exception {
        printTest("TP [Thread Pool] - 20 points");
        
        printExpected("Run pool test (see test document)");
    }
    
    private synchronized static void printTest(String testName) {
        System.err.println("***************************");
        System.err.println(testName);
        System.err.println("***************************");
    }

    private synchronized static void sendSlowly(Socket clientSocket, String msg) throws Exception {
        for (byte b : msg.getBytes(ENC)) {
            clientSocket.getOutputStream().write(b);
            TimeUnit.MILLISECONDS.sleep(SLOWDELAYMS);
        }
    }

    private synchronized static void send(Socket clientSocket, String msg) throws Exception {
        clientSocket.getOutputStream().write(msg.getBytes(ENC));
        TimeUnit.MILLISECONDS.sleep(10);
    }

    private synchronized void printExpected(String msg) throws Exception {
        System.err.println(msg);
        System.err.print(">?");
        while (System.in.read() != '\n') {
        }
    }
}
