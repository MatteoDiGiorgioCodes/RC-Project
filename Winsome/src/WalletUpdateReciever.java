import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class WalletUpdateReciever implements Runnable{

    private String udp_group_name;
    private String udp_port;

    public WalletUpdateReciever(String udp_group_name, String udp_port)
    {
        this.udp_group_name = udp_group_name;
        this.udp_port = udp_port;
    }


    @Override
    public void run() {
        InetAddress group = null;
        int port = 0;
        try {
            group = InetAddress.getByName(udp_group_name);
            port = Integer.parseInt(udp_port);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException |
                UnknownHostException ex)  {
            System.err.println( "Usage: java MulticastSniffer multicast_address port");
            ex.printStackTrace();
                    System.exit(1); }
        MulticastSocket ms = null;
        try {
            ms = new MulticastSocket(port);
            ms.joinGroup(group);
            byte[] buffer = new byte[8192];
            while (true) {
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                ms.receive(dp);
                String s = new String(dp.getData(), StandardCharsets.UTF_8);
                s = s.replace("\u0000", "");
                System.out.println(s); }
        } catch (Exception ex) { System.err.println(ex);
        } finally {
            if (ms != null) {
                try {
                    ms.leaveGroup(group);
                    ms.close(); } catch (IOException ex) { } } }
    }
}
