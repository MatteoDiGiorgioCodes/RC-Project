//package Server;

//import Server.Entities.Post;
//import Server.Entities.SocialNetwork;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;

public class RequestManager implements Runnable {

    int bufSize = 2048;
    ByteBuffer buffer;

    SocketChannel client;
    SocialNetwork social;
    String username;

    public RequestManager (SocketChannel c, SocialNetwork s)
    {
        client = c;
        buffer = ByteBuffer.allocate(bufSize);
        social = s;
        username = null;
    }

    @Override
    public void run() {
        loop: while(true)
        {
            String replyStr = "";
            ((Buffer)buffer).clear();
            try {
                client.read(buffer);
            } catch (IOException e) {
                if(e.getClass().equals(ClosedByInterruptException.class)){
                    break;
                }
                else e.printStackTrace();
            }
            ((Buffer)buffer).flip();
            int receivedLength = buffer.getInt();
            byte[] receivedBytes = new byte[receivedLength];
            buffer.get(receivedBytes);
            String receivedStr = new String(receivedBytes);
            System.out.println("Received: " + receivedStr);

            //gestione input, switch di casi
            String[] cmd = receivedStr.split(" ");
            System.out.println("Comando: " + cmd[0]);
            switch(cmd[0])
            {
                case "login":
                {
                    if(cmd.length==3)
                    {
                        if(username==null)
                        {
                            System.out.println("provo a fare il login");
                            try { replyStr = social.users.get(cmd[1]).login(cmd[2]);
                                if(replyStr.startsWith("logged in"))
                                    username = cmd[1];
                            }
                            catch(NullPointerException e){ replyStr = "invalid username";};
                        }
                        else replyStr = "you are already logged in";
                    }
                    else
                    {
                        if(cmd.length<3)
                            replyStr = "not enough arguments";
                        else replyStr = "too many arguments";
                    }
                    break;
                }
                case "logout":
                {
                    try { replyStr = social.users.get(username).logout();
                          username = null;
                    }
                    catch(NullPointerException e){ replyStr = "first you have to login";};
                    break;
                }
                case "list":
                {
                    if(cmd.length == 2)
                    {
                        switch (cmd[1]) {
                            case "users": {
                                try {
                                    replyStr = social.users(username);
                                } catch (NullPointerException e) {
                                    replyStr = "first you have to login";
                                }
                                break;
                            }
                            case "following": {
                                try {
                                    replyStr = social.users.get(username).getFollowing();
                                } catch (NullPointerException e) {
                                    replyStr = "first you have to login";
                                }
                                break;
                            }
                        }
                    }
                    else
                    {
                        if(cmd.length<2)
                            replyStr = "not enough arguments";
                        else replyStr = "too many arguments";
                    }
                    break;
                }
                case "follow":
                {
                    if(cmd.length == 2)
                    {
                        try { replyStr= social.follow(username, cmd[1]);
                        } catch(NullPointerException e){
                            replyStr = "first you have to login";
                        }
                    }
                    else
                    {
                        if(cmd.length<2)
                            replyStr = "not enough arguments";
                        else replyStr = "too many arguments";
                    }
                    break;
                }
                case "unfollow":
                {
                    if(cmd.length == 2)
                    {
                        try { replyStr = social.unfollow(username, cmd[1]);
                        }
                        catch(NullPointerException e){ replyStr = "first you have to login";}
                    }
                    else
                    {
                        if(cmd.length<2)
                            replyStr = "not enough arguments";
                        else replyStr = "too many arguments";
                    }
                    break;
                }
                case "blog":
                {
                    System.out.println(username+" check");
                    try {
                        replyStr = social.users.get(username).blog();
                    }
                    catch(NullPointerException e){ replyStr = "first you have to login";}
                    break;
                }
                case "post":
                {
                    if(cmd.length >= 3)
                    {
                        try {
                            String title = "";
                            String content = "";
                            int i =1;
                            while(!cmd[i].equals("###")){
                                System.out.println(cmd[i]);
                                title = title.concat(cmd[i]+" ");
                                i++;
                            }
                            i++;
                            while(i< cmd.length) {
                                content = content.concat(cmd[i]+" ");
                                i++;
                            }

                            if(title.length()<=20 )
                                if(content.length()<=500)
                                    social.users.get(username).post(title,content);
                                else replyStr = "content too long";
                            else replyStr = "title too long";
                        }
                        catch(NullPointerException e){ replyStr = "first you have to login";}
                    }
                    else {
                        replyStr = "not enough arguments";
                    }
                    break;
                }
                case "show": {
                    if (cmd.length >= 2 ) {
                        switch (cmd[1]) {
                            case "post": {
                                if(username != null){
                                    try {
                                        //qui in realtà non avrei una nullpointer ex se non sono loggato
                                        replyStr = social.posts.get(cmd[2]).toString();
                                    } catch (NullPointerException e) {
                                        replyStr = "invalid ID";
                                    }
                                }
                                else replyStr = "first you have to login";
                                break;
                            }
                            case "feed": {
                                try {
                                    replyStr = social.users.get(username).feed();
                                }
                                catch (NullPointerException e) {
                                    replyStr = "first you have to login";
                                }
                                break;
                            }
                            default:replyStr="invalid command";
                        }
                    }
                    else {
                        if (cmd.length < 2)
                            replyStr = "not enough arguments";
                        else replyStr = "too many arguments";
                    }
                    break;
                }
                case "delete":
                {
                    if (cmd.length == 2) {
                        try {
                            replyStr = social.users.get(username).deletePost(cmd[1]);
                        }
                        catch(NullPointerException e){ replyStr = "first you have to login";}
                    }
                    else {
                        if (cmd.length < 2)
                            replyStr = "not enough arguments";
                        else replyStr = "too many arguments";
                    }
                    break;
                }
                case "rewin":
                {
                    if (cmd.length == 2) {
                        try {
                            replyStr = social.users.get(username).rewin(cmd[1]);
                        }
                        catch(NullPointerException e){ replyStr = "first you have to login";}
                    }
                    else {
                        if (cmd.length < 2)
                            replyStr = "not enough arguments";
                        else replyStr = "too many arguments";
                    }
                    break;
                }
                case "rate":
                {
                    if (cmd.length == 3) {
                        try {
                            replyStr = social.users.get(username).vote(cmd[1],Integer.parseInt(cmd[2]));
                        }
                        catch(NullPointerException e){ replyStr = "first you have to login";}
                        catch(NumberFormatException e){ replyStr = "invalid vote";}
                    }
                    else {
                        if (cmd.length < 3)
                            replyStr = "not enough arguments";
                        else replyStr = "too many arguments";
                    }
                    break;
                }
                case "comment":
                {
                    if (cmd.length >= 3) {
                        String comment = "";
                        for(int i=2; i< cmd.length; i++)
                            comment = comment.concat(cmd[i]+" ");
                        try {
                            replyStr = social.users.get(username).comment(cmd[1],comment);
                        }
                        catch(NullPointerException e){ replyStr = "first you have to login";}
                    }
                    else {
                        replyStr = "not enough arguments";
                    }
                    break;
                }
                case "wallet":
                {

                    if(cmd.length == 2 )
                    {
                        if(cmd[1].equals("btc")){
                            try {
                                replyStr = social.users.get(username).bitcoinWallet();
                            }
                            catch(NullPointerException e){ replyStr = "first you have to login";}
                        }
                        else replyStr = "invalid command";
                    }
                    else if(cmd.length == 1){
                        try {
                            replyStr = String.valueOf(social.users.get(username).wallet);
                        }
                        catch(NullPointerException e){ replyStr = "first you have to login";}
                    }
                    else replyStr = "too many arguments";
                    break;
                }
                case "exit":
                {
                    break loop;
                }
                default: replyStr = "invalid command";


            }


            byte[] replyBytes = replyStr.getBytes();
            ((Buffer)buffer).clear();
            buffer.putInt(replyBytes.length);
            buffer.put(replyBytes);
            ((Buffer)buffer).flip();
            try {
                client.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
