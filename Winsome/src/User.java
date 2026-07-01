//package Server.Entities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class User {
    public String username;
    public String password;
    public Set<String> tags;
    public Set<String> followers;
    public Set<String> followed;
    public Set<String> blog;
    public boolean logged;
    public double wallet;

    public User(String username,String password, Set<String> tags)
    {
        this.username=username;
        this.password=password;
        this.tags=tags;

        this.followed = new LinkedHashSet<>() ;
        this.followers = new LinkedHashSet<>();
        this.blog = new LinkedHashSet<>();
        this.wallet = 0;
    }

    //metodo per il login, se il login va a buon fine  restituisce la lista dei followes
    //oltre al messaggio "logged in"
    public synchronized String login(String password)
    {
        if(password.equals(this.password))
            if(logged==false)
                logged = true;
            else return "this account in logged in from another device";
        else return "invalid password";
        return "logged in "+ServerMain.getUdpGroupName()+" "+ServerMain.getUdpPort()+" "+followers.toString();
    }

    //metodo per il logout
    public synchronized String logout()
    {
        if(logged==true)
            logged = false;
        else return "you are already logged out";
        return "logged out";
    }

    //metodo per aggiungere un utente user alla lista dei followed
    public synchronized boolean follow(String user) { return followed.add(user); }

    //metodo per rimuovere un utente user alla lista dei followed
    public synchronized boolean unfollow(String user)
    {
        return followed.remove(user);
    }

    //metodo per aggiungere un utente user alla lista dei followers
    public synchronized void addFollower(String follower)
    {
        followers.add(follower);
    }

    //metodo per rimuovere un utente user alla lista dei followers
    public synchronized void removeFollower(String follower)
    {
        followers.remove(follower);
    }

    //metodo per pubblicare un post
    public synchronized void post (String title, String content) {
        blog.add(ServerMain.socialNetwork.addPost(new Post(username,title,content)).toString());
        System.out.println("post "+blog.toString()+" posted");
    }

    //metodo per visualizzare il proprio blog
    public String blog(){

        int i;
        String reply = "";
        if(blog.isEmpty())
            return "blog empty";
        Post current;
        String currentID;
        Iterator<String> it = blog.iterator();
        while(it.hasNext()) {
            currentID = it.next();
            if(ServerMain.socialNetwork.posts.containsKey(currentID))
            {
                current = ServerMain.socialNetwork.posts.get(currentID);
                reply = reply.concat("ID: "+current.ID+"  AUTHOR: "+current.author+"  TITLE: "+current.title+"\n");
            }
        }
        return reply;
    }

    //metodo per visualizzare il proprio feed
    public String feed(){
        if(followed.isEmpty())
            return "you don't follow anyone yet";
        Iterator<String> it = followed.iterator();
        String reply = "";
        String current;
        while(it.hasNext())
        {
            current=it.next();
            if(ServerMain.socialNetwork.users.containsKey(current))
                reply = reply.concat(ServerMain.socialNetwork.users.get(current).blog());
        }
        return reply;
    }

    //metodo per visualizzare la lista dei followed
    public String getFollowing()
    {
        return String.join("", followed);
    };

    //metodo per eliminare un proprio post
    public synchronized String deletePost(String id)
    {
        if(!blog.contains(id))
            return "the given ID does't match any of your posts";
        else {
            blog.remove(id);
            ServerMain.socialNetwork.posts.remove(id);
            return "post deleted";
        }
    }

    //metodo per effettuare il rewin di un post
    public String rewin(String id)
    {
        Boolean check;
        if(ServerMain.socialNetwork.posts.containsKey(id))
            check = blog.add(id);
        else return "post not found";
        //vale la pena comunicargli se era già rewinnato?
        if(check)
            return "post rewinned";
        else return "post already rewinned";
    }

    //metodo per votare un post
    public String vote(String id, int vote)
    {
        if(ServerMain.socialNetwork.posts.containsKey(id))
        {
            if(followed.contains(ServerMain.socialNetwork.posts.get(id).author))
            {  if(!ServerMain.socialNetwork.posts.get(id).positiveReviews.containsKey(username) && !ServerMain.socialNetwork.posts.get(id).negativeReviews.containsKey(username))
                {   if(vote == -1)
                        ServerMain.socialNetwork.posts.get(id).addNegativeVote(username);
                    else if(vote == 1)
                        ServerMain.socialNetwork.posts.get(id).addPositiveVote(username);
                    else return "invalid vote";
                    return "you voted";
                }
                else return "you already voted";
            }
            else return "post not in your feed";
        }
        else return "given ID doesnt't matcht with a post";
    }

    //metodo per commentare un post
    public String comment(String id, String comment)
    {
        if(ServerMain.socialNetwork.posts.containsKey(id))
        {
            Post p = ServerMain.socialNetwork.posts.get(id);
            if(followed.contains(p.author))
            {
                if(!p.comments.containsKey(username))
                   p.comments.put(username, new ArrayList<>());
                p.comments.get(username).add(new Comment(username, comment));
                return "post commented";
            }
            else return "post not in your feed";
        }
        else return "given ID doesnt't matcht with a post";
    }

    //metodo per ottenre il proprio wallet in bitcoin
    public String bitcoinWallet() {
        URL randomOrg = null;
        String randomValue;
        try {
            randomOrg = new URL("https://www.random.org/decimal-fractions/?num=1&dec=10&col=2&format=plain&rnd=new");
            InputStream urlReader = randomOrg.openStream();
            BufferedReader buff = new BufferedReader(new InputStreamReader(urlReader));
            randomValue = buff.readLine();
            buff.close();
            urlReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "error while calculating";
        }

        return String.valueOf(Double.parseDouble(randomValue) * wallet);
    }

    public String toString(){
        return " USERNAME: "+username+"\n PASSWORD: "+password+"\n TAGS: "+tags+"\n FOLLOWERS: "+followers+"\n FOLLOWED: "+followed+"\n BLOG: "+blog+"\n WALLET: "+wallet;
    }

}
