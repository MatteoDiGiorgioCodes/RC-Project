import java.rmi.RemoteException;

public class FollowerCallbackImpl implements FollowerCallbackInterface{

    public String username;

    public FollowerCallbackImpl()throws RemoteException {
        super( );
        username = ClientMain.username;
    }

    //metodo per notificare follow o unfollow da parte di altri utenti
    public void notifyEvent(boolean type, String user) throws RemoteException {
        if(type) {
            ClientMain.followersList.add(user);
            System.out.println(user+" follows you");
        }
        else {
            ClientMain.followersList.remove(user);
            System.out.println(user+" unfollowed you");
        }
    }
}
