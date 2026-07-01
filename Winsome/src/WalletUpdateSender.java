import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WalletUpdateSender implements Runnable {

    public static InetAddress ia;
    public static int port;
    public static int reward_percentage;
    public static int reward_time;

    public static Long lastUpdateTime = (long) -1;

    public WalletUpdateSender(String ia, int port, int rp, int rt) throws UnknownHostException {
        this.ia = InetAddress.getByName(ia);
        this.port = port;
        this.reward_percentage = rp;
        this.reward_time = rt;
    }


    @Override
    public void run() {
        try {
            //InetAddress ia = InetAddress.getByName("230.230.230.230");
            byte[] data;
            data = "Wallet updated".getBytes(StandardCharsets.UTF_8);
            //int port = 9000;
            DatagramPacket dp = new DatagramPacket(data, data.length, ia, port);
            DatagramSocket ms = new DatagramSocket();
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(reward_time);
                updateWallet();
                ms.send(dp);
                System.out.println("Wallets updated");
            }
            ms.close();
        } catch (Exception ex) {
            if(ex.getClass().equals(InterruptedException.class))
                System.out.println("Wallet update sender interrupted");
            else ex.printStackTrace();
        }
    }

    //metodo per calcolare le ricompense
    private void updateWallet() {
        Post[] p = new Post[0];

        lastUpdateTime = ServerMain.socialNetwork.last_time_rewarded;
        long time = System.currentTimeMillis();
        ServerMain.socialNetwork.last_time_rewarded = time;
        p = ServerMain.socialNetwork.posts.values().toArray(p);

        for (Post i : p) {
            ServerMain.socialNetwork.posts.get(i.ID).times_rewarded++;

            List<String> curators = new ArrayList();
            long comments_credits = 0;
            long newVotes = 0;

            for (String u : i.positiveReviews.keySet()) {
                if (time> i.positiveReviews.get(u) && i.positiveReviews.get(u) >= lastUpdateTime) {
                    newVotes++;
                    if(!curators.contains(u))
                        curators.add(u);
                }
            }
            for (String u : i.negativeReviews.keySet()) {
                if (time> i.negativeReviews.get(u) && i.negativeReviews.get(u) >= lastUpdateTime)
                    newVotes--;
            }
            for (String a : i.comments.keySet()) {
                int current_Cp = i.comments.get(a).size();
                boolean recently_commented = false;
                for (Comment c : i.comments.get(a)) {
                    if (time > c.time && c.time >= lastUpdateTime) {
                        recently_commented = true;
                        break;
                    }
                }
                if(recently_commented){
                    comments_credits = (long) (comments_credits + (2/(1+Math.pow(Math.E, -(current_Cp - 1)))));
                    if(!curators.contains(a))
                        curators.add(a);
                }
            }

            double reward = (Math.log((Math.max(newVotes, 0))+1) + Math.log(comments_credits +1))/i.times_rewarded;
            double author_reward = (reward/100)*reward_percentage;
            double curator_reward = (reward - author_reward)/curators.size();
            ServerMain.socialNetwork.users.get(i.author).wallet+=author_reward;
            for(String cu : curators)
                ServerMain.socialNetwork.users.get(cu).wallet+=curator_reward;
        }
    }
}
