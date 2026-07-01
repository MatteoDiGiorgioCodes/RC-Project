public class Comment {
    public String author;
    public String content;
    public long time;

    public Comment(String a, String c)
    {
        author=a;
        content=c;
        time=System.currentTimeMillis();
    }

    public String toString()
    {
        return content;
    }
}
