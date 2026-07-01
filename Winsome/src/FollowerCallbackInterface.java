import java.rmi.*;

public interface FollowerCallbackInterface extends Remote {
    public String username = null;
    /* Metodo invocato dal server per notificare un evento ad un client remoto. */
    public void notifyEvent(boolean type, String user) throws RemoteException;
}



