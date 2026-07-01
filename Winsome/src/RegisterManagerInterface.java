//package Server.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface RegisterManagerInterface extends Remote {

    public void register(String username, String password, Set<String> tags) throws RemoteException;

}
