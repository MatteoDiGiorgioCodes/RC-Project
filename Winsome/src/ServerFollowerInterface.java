import java.rmi.*;
public interface ServerFollowerInterface extends Remote{
        /* registrazione per la callback */
        public void registerForCallback(String username,FollowerCallbackInterface ClientImpl) throws RemoteException;
        /* cancella registrazione per la callback */
        public void unregisterForCallback (String username,FollowerCallbackInterface ClientImpl) throws RemoteException;
}

