
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DirService extends Remote {
	
	public String doAuthentication(User s) throws RemoteException;
	public RemoteServidor getRemoteServerInterface() throws RemoteException;

} 
