package client;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import directory.DirService;
import utils.User;

public class Client {

	private User user = null;
	DirService dirSrv = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Client cli = new Client();
		cli.init();
	}

	public void init(){
		user = new User("luis", "1231");
		//String localizacao = "//localhost/DirService";

		try{
            String objectUrl = "rmi://127.0.0.1/DirService"; //rmiregistry on localhost

         //   if(args.length > 0){
           //     objectUrl = "rmi://"+args[0]+"/RemoteTime";
           // }

            DirService dirSrv = (DirService)Naming.lookup(objectUrl);

            System.out.println("Server: " + dirSrv.getIpServer(user));

        }catch(RemoteException e){
            System.out.println("Erro remoto - " + e);
        }catch(NotBoundException e){
            System.out.println("Servico remoto desconhecido - " + e);
        }catch(Exception e){
            System.out.println("Erro - " + e);
        }


//		try {
//			dirSrv = (DirService) Naming.lookup(localizacao);
//
//		} catch (MalformedURLException | RemoteException | NotBoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		try {
//			System.out.println(dirSrv.getIpServer(user));
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
