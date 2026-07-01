//package Server;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BackupManager implements Runnable{

    private static final String WINSOME_FOLDER_NAME = "data";
    private static final String USERS_DATA_PATH = "users_data.json";
    private static final String POSTS_DATA_PATH = "posts_data.json";
    private static final String LAST_POST_ID_DATA_PATH = "last_post_id_data.json";
    private static final String LAST_TIME_SAVED_DATA_PATH = "last_time_saved_data.json";

    public static Gson gson;
    public static int backup_time;

    public BackupManager(int bt) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        backup_time = bt;
        if(!createFile(WINSOME_FOLDER_NAME+"/"+ USERS_DATA_PATH)) System.err.println("[!] Errore creazione file dati utente");
        if(!createFile(WINSOME_FOLDER_NAME+"/"+ POSTS_DATA_PATH)) System.err.println("[!] Errore creazione file relazione (posts)");
        if(!createFile(WINSOME_FOLDER_NAME+"/"+ LAST_POST_ID_DATA_PATH)) System.err.println("[!] Errore creazione file dati utente");
        if(!createFile(WINSOME_FOLDER_NAME+"/"+ LAST_TIME_SAVED_DATA_PATH)) System.err.println("[!] Errore creazione file dati utente");

    }

    //metodo per salvare il social network
    public synchronized static void saveAll() throws IOException
    {
        System.out.println("Tryng to save");
        saveInFile(WINSOME_FOLDER_NAME+"/"+USERS_DATA_PATH,ServerMain.socialNetwork.users.values());
        saveInFile(WINSOME_FOLDER_NAME+"/"+POSTS_DATA_PATH,ServerMain.socialNetwork.posts.values());
        saveInFile(WINSOME_FOLDER_NAME+"/"+LAST_POST_ID_DATA_PATH,ServerMain.socialNetwork.posts_quantity);
        saveInFile(WINSOME_FOLDER_NAME+"/"+LAST_TIME_SAVED_DATA_PATH,ServerMain.socialNetwork.last_time_rewarded);
        System.out.println("Saved");
    }

    //metodo per salvare una delle componenti del social network in un file json
    public static void saveInFile(String path, Object structure) throws IOException {
        String toSave = " ";
        try{toSave = gson.toJson(structure);}
        catch(Exception e){e.printStackTrace();}
        FileOutputStream fos = new FileOutputStream(path);
        OutputStreamWriter ow = new OutputStreamWriter(fos);
        ow.write(toSave);
        ow.flush();
    }

    //metodo per caricare gli utenti
    public static Map loadUsers(){
        try {
            return loadUsersFromFile(WINSOME_FOLDER_NAME+"/"+USERS_DATA_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ConcurrentHashMap();
    }

    //metodo per caricare i post
    public static Map loadPosts(){
        try {
            return loadPostsFromFile(WINSOME_FOLDER_NAME+"/"+POSTS_DATA_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ConcurrentHashMap();
    }

    //metodo per caricare l'id dell'ultimo post postato
    public static AtomicInteger loadLastPostID(){
        try {
            return loadIntFromFile(WINSOME_FOLDER_NAME+"/"+LAST_POST_ID_DATA_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AtomicInteger(0);
    }

    //metodo per caricare il momento dell'ultimo calcolo delle ricompense
    public static Long loadLastTimeRewarded(){
        try {
            return loadLongFromFile(WINSOME_FOLDER_NAME+"/"+LAST_TIME_SAVED_DATA_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public static Map loadUsersFromFile(String path) throws IOException {
        Map<String, Object> map = new ConcurrentHashMap<>();
        FileInputStream inputStream = new FileInputStream(path);
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        try{
            reader.beginArray();
            System.out.println("USERS:\n");
            while (reader.hasNext()) {
                User u = gson.fromJson(reader, User.class);
                u.logged = false;
                System.out.println(u+"\n");
                map.put(u.username, u);
            }
            reader.endArray();
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("epmty "+path);
        }
        return map;
    }

    public static Map loadPostsFromFile(String path) throws IOException {
        Map<String, Object> map = new ConcurrentHashMap<>();
        FileInputStream inputStream = new FileInputStream(path);
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        System.out.println("POSTS:\n");
        try{
            reader.beginArray();
            while (reader.hasNext()) {
                Post p = gson.fromJson(reader, Post.class);
                System.out.println(p+"\n");
                map.put(p.ID, p);
            }
            reader.endArray();
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("epmty "+path);
        }
        return map;
    }

    public static AtomicInteger loadIntFromFile(String path) throws IOException {
        AtomicInteger i = new AtomicInteger(0);
        FileInputStream inputStream = new FileInputStream(path);
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        try {
                i = new Gson().fromJson(reader, AtomicInteger.class);
                System.out.println("LAST POST ID: "+i);
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("epmty "+path);
        }
        if(i!=null)
            return i;
        else return new AtomicInteger(0);
    }

    public static Long loadLongFromFile(String path) throws IOException {
        Long i = new Long(0);
        FileInputStream inputStream = new FileInputStream(path);
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        try {
            i = new Gson().fromJson(reader, Long.class);
            System.out.println("LAST TIME REWARDED: "+i);
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("epmty "+path);
        }
        if(i!=null)
            return i;
        else return 0L;
    }

    //metodo per creare i file
    private boolean createFile(String path) {
        boolean ignored = new File(WINSOME_FOLDER_NAME).mkdirs(); // genero la cartella, se non esiste già

        try {
            File f = new File(path);
            // se il file non esiste e non è stato creato dò errore
            return f.exists() || f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    //la run implementa il salvataggio periodico
    public void run() {
        while(!Thread.currentThread().isInterrupted()){
            try {
                Thread.sleep(backup_time);
            } catch (InterruptedException e) {
                System.out.println("Backup manager interrupted");
            }
            try {
                this.saveAll();
            } catch (IOException e) {
                System.out.println("Failed to save");
                e.printStackTrace();
            }
        }
    }
}
