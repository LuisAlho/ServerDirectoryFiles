package client;


import java.io.File;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import directory.DirService;
import utils.User;

public class Client implements RemoteClient {

	
	private static final String AUTH_FAIL = "FAIL_AUTH";
	private static final String NO_SERVER = "NO_SERVER";
	private User user = null;
	private DirService dirSrv = null;
	private String ipServer;
	private File directory =  null;
	private String ipDirService = null;
	
	public Client(String ip, String nameDir){
		ipDirService = ip;
		directory = new File(nameDir);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if ( args.length != 2 )
		{
			System.out.println("Dir: " +args[1] + "\nIP: " +args[0] );	
			System.out.println("Syntaxe: java Client \"Directoria\" \"IP\"");
			System.exit(1);
		}
		
		System.out.println("Dir: " +args[0] + "\nIP: " +args[1] );

		Client cli = new Client(args[1], args[0]);
		cli.init();
	}

	public void init(){
		
		user = new User();
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Nome: ");
		user.setName(sc.nextLine());
		System.out.println("Password: ");
		user.setPassword(sc.nextLine());
		
		//user = new User("luis", "1231");
		
		
		sc.close();

		try{
            String objectUrl = "rmi://"+ ipDirService +"/DirService"; //rmiregistry on localhost

         //   if(args.length > 0){
           //     objectUrl = "rmi://"+args[0]+"/RemoteTime";
           // }

            
            DirService dirSrv = (DirService)Naming.lookup(objectUrl);
            ipServer = dirSrv.getIpServer(user);
            System.out.println("Server: " + ipServer);
            
            if(ipServer.equals(AUTH_FAIL)){
            	System.out.println("User nao Registado");
            }else{ 
            	if(ipServer.equals(NO_SERVER)){
	            
	            	System.out.println("Por favor tente mais tarde nao existem servidores desponiveis");
	            	System.exit(1);
	            }else {
	            	
	            	System.out.println("Servidor no ip: " + ipServer);
	            }
            }
            
            /* REGISTAR A INTERFACE REMOTA DO CLIENTE*/
            
            
            /* ADQUIRIR INTERFACE REMOTA DO SERVIDOR */
            
            /*
             * CODIGO A IMPLEMENTAR
             * 
             * */
            
            /*INICAR MENU COM OPCOES A EFECTUAR PELO CIENTE*/
            
            /*
             * MENU
             * - DOWNLOAD
             * - UPLOAD
             * - ELIMINAR
             * - VISUALIZAR
             * 
             * */

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

	
	/******************/
	/*INTERFACE REMOTA*/
	/******************/
	@Override
	public void notifyClient() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

}
