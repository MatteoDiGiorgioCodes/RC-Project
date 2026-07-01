import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerFollowerImpl implements ServerFollowerInterface{

    private Map<String, FollowerCallbackInterface> clients;

    public ServerFollowerImpl()throws RemoteException {
        super( );
        clients = new ConcurrentHashMap<>();
    }

    public void registerForCallback(String username, FollowerCallbackInterface Client) throws RemoteException {
        if (!clients.containsKey(username)) {
            clients.put(username, Client);
            System.out.println("New client registered" );
        }
    }

    public void unregisterForCallback(String username, FollowerCallbackInterface Client) throws RemoteException {
        if (clients.remove(username) != null)
            System.out.println("Client unregistered");
        else
            System.out.println("Unable to unregister client.");
    }

    public synchronized void doCallbacks(String followed,String follower, boolean type) throws RemoteException {
        clients.get(followed).notifyEvent(type, follower);
    }
}
