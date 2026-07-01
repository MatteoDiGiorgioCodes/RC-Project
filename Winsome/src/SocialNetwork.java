//package Server.Entities;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SocialNetwork {
    public Map<String, User> users;
    public Map<String, Post> posts;
    public AtomicInteger posts_quantity;
    public Long last_time_rewarded;

    public SocialNetwork()
    {
        this.users = new ConcurrentHashMap<>();
        this.posts = new ConcurrentHashMap<>();
        this.posts_quantity = new AtomicInteger(0);
        this.last_time_rewarded = Long.valueOf(0);
    }

    public SocialNetwork(Map<String, User> users, Map<String, Post> posts, AtomicInteger posts_quantity, Long last_time_saved)
    {
        this.users = users;
        this.posts = posts;
        this.posts_quantity = posts_quantity;
        this.last_time_rewarded = last_time_saved;
    }

    //metodo per far seguire followed da follower
    public String follow(String follower, String followed) throws NullPointerException
    {
        //se follower e' null e' perche' non ha effettuato il login
        if(follower == null)
            throw new NullPointerException();
        if(follower.equals(followed))
            return "you can not follow yourself";
        //aggiungo follower alla lista followers di followed
        try { users.get(followed).addFollower(follower); }
        catch (NullPointerException e){return "user not found";}
        //aggiungo followed alla lista dei followed di follower, se non era gia' presente notifico followed
        if(users.get(follower).follow(followed)){
            try {
                ServerMain.serverFollower.doCallbacks(followed, follower, true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return "success";
        }
        else return "you already follow this user";
    }

    //metodo per far smettere di seguire followed da follower
    public String unfollow(String follower, String followed) throws NullPointerException
    {
        //se follower e' null e' perche' non ha effettuato il login
        if(follower == null)
            throw new NullPointerException();
        if(follower.equals(followed))
            return "you can not unfollow yourself";
        //rimuovo follower dalla lista followers di followed
        try { users.get(followed).removeFollower(follower); }
        catch (NullPointerException e){return "user not found";}
        //rimuovo followed dalla lista dei followed di follower, se era presente notifico followed
        if(users.get(follower).unfollow(followed)){
            try {
                ServerMain.serverFollower.doCallbacks(followed, follower, false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return "success";
        }
        else return "you dont't follow this user";
    }

    //metodo che restituisce la lista di utenti con cui user ha almeno un tag in comune
    public String users(String user) throws NullPointerException
    {
        User user1 = users.get(user);
        User user2;
        String reply = "Users with tag in common: \n";
        Iterator<String> it = users.keySet().iterator();
        while(it.hasNext())
        {
            user2 = users.get(it.next());
            if(!user1.username.equals(user2.username) && !Collections.disjoint(user1.tags,user2.tags))
                reply = reply.concat(user2.username+"\n");
        }
        return reply;
    }

    //metodo per aggiungere un post
    public Integer addPost(Post p)
    {
        try {posts.put(String.valueOf(posts_quantity.getAndIncrement()), p);}
        catch (Exception e){ e.printStackTrace(); return -1;}
        System.out.println(String.valueOf(posts_quantity.get()));
        p.SetID(posts_quantity.get()-1);
        return posts_quantity.get()-1;
    }

}
