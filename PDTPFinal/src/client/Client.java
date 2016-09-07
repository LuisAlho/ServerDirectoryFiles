package client;


import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import directory.DirService;
import server.RemoteServidor;
import utils.User;

public class Client implements RemoteClient {

	
	private static final String AUTH_FAIL = "FAIL_AUTH";
	private static final String NO_SERVER = "NO_SERVER";
	private User user = null;
	private DirService dirSrv = null;
	//private String ipServer;
	private File directory =  null;
	private String ipDirService = null;
	
	private RemoteServidor serverInterface = null;
	
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

	public int menu(){
		
		int op;
		System.out.println("MENU");
		System.out.println("1 - Download");
		System.out.println("2 - Upload");
		System.out.println("3 - Delete");
		System.out.println("4 - Visualizacao");
		System.out.println("5 - Sair");
		
		Scanner sc = new Scanner(System.in);
		do{
			System.out.println("\n Opcao: ");
			op = sc.nextInt();	
			
		}while(op > 5 || op < 1);
			
		sc.close();
		return op;
	}

	public void trataOpcao(int op){
		
		switch (op){
			
			case 1: break;
			case 2: break;
			case 3: break;
			case 4: break;
			case 5: System.exit(1); 
					break;
			default: break;
					
		}
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
            
            DirService dirSrv = (DirService)Naming.lookup(objectUrl);
            String msg = dirSrv.doAuthentication(user);
            if(msg.equals(AUTH_FAIL)){
            	System.out.println("User nao Registado");
            	System.exit(1);
            }else{ 
            	if(msg.equals(NO_SERVER)){
	            
	            	System.out.println("Por favor tente mais tarde nao existem servidores desponiveis");
	            	System.exit(1);
	            }
            }

            serverInterface = dirSrv.getRemoteServerInterface();
            System.out.println("Server: " + serverInterface.toString());
            
 
            /* REGISTAR A INTERFACE REMOTA DO CLIENTE*/
            serverInterface.addObserver(this);
            System.out.println("" + serverInterface.getFileList());
 
            /* ADQUIRIR INTERFACE REMOTA DO SERVIDOR */

            /*INICAR MENU COM OPCOES A EFECTUAR PELO CIENTE*/
            int op = menu();
            trataOpcao(op);
            

        }catch(RemoteException e){
            System.out.println("Erro remoto - " + e);
        }catch(NotBoundException e){
            System.out.println("Servico remoto desconhecido - " + e);
        } catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e){
            System.out.println("Servidores nao disponiveis - " + e.getMessage());
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
	public void notifyFileList(String description) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean writeFileChunk(byte[] fileChunk, int nbytes) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	
	
	

}
