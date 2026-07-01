//package Server.RMI;

//import Server.Entities.User;
//import Server.Server;

import java.lang.management.ManagementFactory;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RegisterManagerImpl implements RegisterManagerInterface {


    //metod to register to Winsome
    public void register(String username, String password, Set<String> tags) throws RemoteException
    {
        if(!ServerMain.socialNetwork.users.containsKey(username))
            ServerMain.socialNetwork.users.put(username, new User(username,password,tags));
    }
}
