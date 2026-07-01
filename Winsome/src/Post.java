//package Server.Entities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class Post {

    public String title;
    public String content;
    public String ID;
    public String author;
    public Map<String, List<Comment>> comments;
    public Map<String, Long> positiveReviews;
    public Map<String, Long> negativeReviews;
    public long times_rewarded;

    public Post (String a, String t, String c)
    {
        author=a;
        title=t;
        content=c;
        positiveReviews = new ConcurrentHashMap<>();
        negativeReviews = new ConcurrentHashMap<>();
        comments = new ConcurrentHashMap<>();
        times_rewarded=0;
    }

    public void SetID (Integer i)
    {
        ID = i.toString();
    }

    public String toString()
    {
        String reply = "";
        if(this.ID == null)
            return "post not yet published";
        else return reply.concat(" ID: "+this.ID+"\n AUTHOR: "+this.author+"\n TITLE: "+this.title
                +"\n CONTENT: "+this.content+"\n POSITIVE VOTES:"+ ((Integer) this.positiveReviews.size()).toString()
                +"\n NEGATIVE VOTES: "+((Integer) this.negativeReviews.size()).toString()+"\n COMMENTS: "+this.comments.entrySet().toString());
    }

    //metodo per aggiungere voto positivo
    public void addPositiveVote (String user)
    {
        positiveReviews.put(user, System.currentTimeMillis());
    }

    //metodo per aggiungere voto negativo
    public void addNegativeVote (String user)
    {
        positiveReviews.put(user, System.currentTimeMillis());
    }

}
