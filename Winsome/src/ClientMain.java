//package Client;

//import Server.RMI.RegisterManagerInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ClientMain {
    private static String server_hostName = "localhost";
    private static String fol_serviceName = "TEST1";
    private static String reg_serviceName = "REGISTER";
    private static int server_port = 8000;
    private static int reg_port = 200;

    private static ByteBuffer buffer;
    private static int bufSize = 2048;

    private static SocketChannel sc;

    public static Registry r;
    public static FollowerCallbackInterface stub;
    public static ServerFollowerInterface server;

    public static ArrayList<String> followersList = new ArrayList<>();
    public static String username;
    public static String username_temp;
    public static Thread wallet;

    public static void main(String[] args) {

        File CONFIG_FILE;

        if (args.length == 0) {
            System.out.println("SERVER: server starts with default configuration");
        } else {
            CONFIG_FILE = new File(args[0]);
            configClient(CONFIG_FILE);
        }

        buffer = ByteBuffer.allocate(bufSize);
        Scanner scanner = new Scanner(System.in);
        // Apro il SocketChannel per la comunicazione con il server.
        try {
            sc = SocketChannel.open(new InetSocketAddress(server_hostName, server_port));
        } catch (IOException e) {
            System.out.println("Winsome currently offline, try again later");
        }

        // Ottengo un riferimento al registry.
        try {
            r = LocateRegistry.getRegistry(reg_port);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        boolean b = true;
        try {
            while (b) {
                System.out.println("Insert command:");
                String inputStr = scanner.nextLine();
                switch (inputStr) {
                    case "exit": {
                        if (username != null)
                            sendCommand("logout");
                        exit();
                        sc.close();
                        b = false;
                        break;
                    }
                    case "register": {
                        register();
                        break;
                    }
                    case "list followers": {
                        if (username != null)
                            System.out.println(followersList);
                        else System.out.println("you have to login first");
                        break;
                    }
                    default:
                        sendCommand(inputStr);
                }
            }
        } catch (Exception e){
            if(e.getMessage().equals("Connection reset by peer"))
                System.out.println("Winsome closed by the server");
            else System.out.println("Something went wrong");
        }
        System.out.println("END");
        System.exit(1);
    }

    private static void register()
    {
        try {
            // Ottengo un riferimento al registry.
            //Registry r = LocateRegistry.getRegistry(reg_port);
            // Ottengo un riferimento alla lista remota.
            RegisterManagerInterface m = (RegisterManagerInterface) r.lookup(reg_serviceName);
            //server = (ServerFollowerInterface) r.lookup(fol_serviceName);
            Scanner inputScanner = new Scanner(System.in);
            System.out.println("Insert username:");
            String username = inputScanner.nextLine();
            System.out.println("Insert password:");
            String password = inputScanner.nextLine();
            Set<String> tags = new LinkedHashSet<>();

            while(true) {
                System.out.println("Insert tag, exit to stop:");
                String tag = inputScanner.nextLine();
                if(tag.equals("exit"))
                    break;
                else
                    tags.add(tag);
            }
            m.register(username, password, tags);
        }
        catch (Exception e) {
            System.out.println("Failed to register, please try again later");
        }
    }

    private static void sendCommand(String input) throws IOException {

        Scanner inputScanner = new Scanner(System.in);

        if(input.equals("follow")||input.equals("unfollow"))
        {
            System.out.println("Insert Username");
            input = input.concat(" "+inputScanner.nextLine());
        }
        else if(input.startsWith("delete")||input.startsWith("rewin")||input.startsWith("show post"))
        {
            System.out.println("Insert post ID");
            input = input.concat(" "+inputScanner.nextLine());
        }
        else if(input.startsWith("login"))
        {
            System.out.println("Insert username");
            username_temp = inputScanner.nextLine();
            input = input.concat(" "+username_temp);
            System.out.println("Insert password");
            input = input.concat(" "+inputScanner.nextLine());
        }
        else if(input.startsWith("post"))
        {
            System.out.println("Insert post title");
            input = input.concat(" "+inputScanner.nextLine()+" ###");
            System.out.println("Insert post content");
            input = input.concat(" "+inputScanner.nextLine());
        }
        else if(input.startsWith("rate"))
        {
            System.out.println("Insert post ID");
            input = input.concat(" "+inputScanner.nextLine());
            System.out.println("Insert rating");
            input = input.concat(" "+inputScanner.nextLine());
        }
        else if(input.startsWith("comment"))
        {
            System.out.println("Insert post ID");
            input = input.concat(" "+inputScanner.nextLine());
            System.out.println("Insert comment");
            input = input.concat(" "+inputScanner.nextLine());
        }
        byte[] message = input.getBytes();

        // Preparo il buffer per la scrittura.
        ((Buffer)buffer).clear();
        // Scrivo la lunghezza e poi il messaggio nel buffer.
        buffer.putInt(message.length);
        buffer.put(message);
        // Preparo il buffer per la lettura.
        ((Buffer)buffer).flip();
        // Leggo il messaggio dal buffer e lo invio al server.
        sc.write(buffer);
        // A questo punto devo attendere la risposta dal server.
        // Preparo il buffer per la scrittura.
        ((Buffer)buffer).clear();
        // Leggo il messaggio dal canale e lo scrivo nel buffer.
        sc.read(buffer);
        // Preparo il buffer per la lettura.
        ((Buffer)buffer).flip();
        int replyLength = buffer.getInt();
        byte[] replyBytes = new byte[replyLength];
        buffer.get(replyBytes);

        String replyString = new String(replyBytes);
        if(replyString.startsWith("logged in"))
        {
            //se il login è andato a buon fine il client si registra per le callback e avvia il thread per
            //ricevere le notifiche di aggiornamento wallet
            //inoltre insieme al messaggio di avvenuto login ricevo la lista dei follower che salvo in followerslist
            String[] input_array = replyString.substring("logged in ".length()).split(" ");
            followersList.addAll(Arrays.asList(input_array).subList(2, input_array.length));

            System.out.println("Received:\nlogged in");
            username = username_temp;
            FollowerCallbackInterface callbackObj = new FollowerCallbackImpl();
            try {
                server = (ServerFollowerInterface) r.lookup(fol_serviceName);
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
            stub = (FollowerCallbackInterface)
                    UnicastRemoteObject.exportObject(callbackObj, 0);
            server.registerForCallback(username, stub);

            WalletUpdateReciever wur = new WalletUpdateReciever(input_array[0], input_array[1]);
            wallet = new Thread(wur);
            wallet.start();
        }
        else if(replyString.startsWith("logged out"))
        {
            //se il logout è andato a buon fine il client si deregistra per le callback e interrompe il thread per
            //ricevere le notifiche di aggiornamento wallet
            followersList = new ArrayList<>();
            System.out.println("Received:\n" + replyString);
            server.unregisterForCallback(username, stub);
            username = null;
            wallet.interrupt();
        }
        else System.out.println("Received:\n" + replyString);
    }

    private static void exit() {
        byte[] message = "exit".getBytes();
        // Preparo il buffer per la scrittura.
        ((Buffer)buffer).clear();
        // Scrivo la lunghezza e poi il messaggio nel buffer.
        buffer.putInt(message.length);
        buffer.put(message);
        // Preparo il buffer per la lettura.
        ((Buffer)buffer).flip();
        // Leggo il messaggio dal buffer e lo invio al server.
        try {
            sc.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void configClient(File config_file) {
        try {
            Scanner scanner = new Scanner(config_file);
            while(scanner.hasNext()){
                String line = scanner.nextLine();
                if(!line.isEmpty()){
                    String[] data = line.split("=");
                    switch (data[0]){
                        case "Server hostname": {
                            server_hostName = data[1];
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
                        case "Server port": {
                            server_port = Integer.parseInt(data[1]);
                            break;
                        }
                        case "Registry port": {
                            reg_port = Integer.parseInt(data[1]);
                            break;
                        }
                        case "Size of the buffer": {
                            bufSize = Integer.parseInt(data[1]);
                            break;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
