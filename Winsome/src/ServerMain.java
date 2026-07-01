//package Server;

//import Server.Entities.SocialNetwork;
//import Server.RMI.RegisterManagerImpl;
//import Server.RMI.RegisterManagerInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.channels.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerMain {

    private static String server_hostname = "localhost";
    private static String fol_serviceName = "TEST1";
    private static String reg_serviceName = "REGISTER";
    private static String udp_groupName = "230.230.230.230";
    private static int server_port = 8000;
    private static int fol_port = 1000;
    private static int reg_port = 200;
    private static int udp_port = 9000;

    public static int bufSize = 2048;
    public static int reward_percentage = 70;
    public static int reward_time = 120000;
    public static int backup_time = 120000;

    public static SocialNetwork socialNetwork;
    public static ServerFollowerImpl serverFollower;
    public static ServerSocketChannel serverSocket;


    public static void main(String[] args) {

        File CONFIG_FILE;
        
        if (args.length == 0) {
            System.out.println("SERVER: server starts with default configuration");
        } else {
            CONFIG_FILE = new File(args[0]);
            configServer(CONFIG_FILE);
        }

        BackupManager bu = new BackupManager(backup_time);
        socialNetwork =  new SocialNetwork(BackupManager.loadUsers(), BackupManager.loadPosts(), BackupManager.loadLastPostID(), BackupManager.loadLastTimeRewarded());

        try
        {
            //creo il registry
            LocateRegistry.createRegistry(reg_port);
            Registry r = LocateRegistry.getRegistry(reg_port);

            //esporto l'oggetto remoto per la registrazione
            RegisterManagerImpl man = new RegisterManagerImpl();
            RegisterManagerInterface stub = null;
            stub = (RegisterManagerInterface) UnicastRemoteObject.exportObject(man, 0);
            r.rebind(reg_serviceName, stub);

            //esporto l'oggeto remoto per le callback
            serverFollower = new ServerFollowerImpl( );
            ServerFollowerInterface stub_1 = null;
            stub_1 = (ServerFollowerInterface) UnicastRemoteObject.exportObject (serverFollower,fol_port);
            r.rebind (fol_serviceName, stub_1);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            System.err.println("Errore: " + exception.getMessage());
        }

        // preparo il threadpoolexecutor
        ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        //faccio partire i thread per il backup e per mandare le notifiche di wallet update
        Thread backUp = new Thread(bu);
        backUp.start();
        Thread wallet = null;
        try {
            wallet = new Thread(new WalletUpdateSender(udp_groupName,udp_port,reward_percentage,reward_time));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        wallet.start();
        Thread closer = new Thread(new ServerCloser(wallet, backUp));
        closer.start();
        //pool.submit(bu);
        //pool.submit(new WalletUpdateSender(udp_groupName,udp_port));

        //apro canale
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(server_hostname, server_port));
            serverSocket.configureBlocking(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //ciclo per accettare nuovi utenti
        while (true) {
            SocketChannel client = null;
            try {
                client = serverSocket.accept();
                pool.submit(new RequestManager(client, socialNetwork));
                System.out.println("Nuova connessione ricevuta");
            } catch (IOException ioException) {
                System.out.println("Socket TCP chiuso");
                break;
            }
        }
        pool.shutdownNow();

        try {
            closer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("chiusura Server");
        System.exit(1);
    }

    private static void configServer(File config_file) {
        try {
            Scanner scanner = new Scanner(config_file);
            while(scanner.hasNext()){
                String line = scanner.nextLine();
                if(!line.isEmpty()){
                    String[] data = line.split("=");
                    switch (data[0]){
                        case "Hostname server": {
                            server_hostname = data[1];
                            break;
                        }
                        case "Follower callback service name": {
                            fol_serviceName = data[1];
                            break;
                        }
                        case "Register service name": {
                            reg_serviceName = data[1];
                            break;
                        }
                        case "UDP group name": {
                            udp_groupName = data[1];
                            break;
                        }
                        case "Server port": {
                            server_port = Integer.parseInt(data[1]);
                            break;
                        }
                        case "Follower callback service port": {
                            fol_port = Integer.parseInt(data[1]);
                            break;
                        }
                        case "Registry port": {
                            reg_port = Integer.parseInt(data[1]);
                            break;
                        }
                        case "UDP port": {
                            udp_port = Integer.parseInt(data[1]);
                            break;
                        }
                        case "Reward author percentage": {
                            reward_percentage = Integer.parseInt(data[1]);
                            break;
                        }
                        case "Time lapse between rewards (ms)": {
                            reward_time = Integer.parseInt(data[1]);
                            break;
                        }
                        case "Time lapse between backups (ms)": {
                            backup_time = Integer.parseInt(data[1]);
                            break;
                        }
                        case "Size of the buffer": {
                            bufSize = Integer.parseInt(data[1]);
                            break;
                        }
                    }
                }
                else break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getUdpGroupName(){
        return udp_groupName;
    }

    public static int getUdpPort(){
        return udp_port;
    }
}
