import java.io.IOException;
import java.util.Scanner;

public class ServerCloser implements Runnable{

    public Thread wallet;
    public Thread backUp;

    public ServerCloser(Thread wallet, Thread backUp) {
        this.wallet = wallet;
        this.backUp = backUp;
    }

    @Override
    public void run() {
        Scanner inputScanner = new Scanner(System.in);
        while(true) {
            System.out.println("exit to end Server");
            if(inputScanner.nextLine().equals("exit"))
                break;
        }

        wallet.interrupt();
        backUp.interrupt();

        try {
            ServerMain.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(wallet.isAlive() && backUp.isAlive())
        {
            try {
                wallet.join(1000);
                backUp.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            BackupManager.saveAll();
        } catch (IOException e) {
            System.out.println("Failed to save");
            e.printStackTrace();
        }
    }
}
