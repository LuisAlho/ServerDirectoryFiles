package server;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import client.RemoteClient;
import utils.HeartBeat;

/*
 * long t= System.currentTimeMillis();
long end = t+15000;
while(System.currentTimeMillis() < end) {
  // do something
  // pause to avoid churning
  
}
 * 
 * */

public class Server extends UnicastRemoteObject implements RemoteServer {
	
	
	private static final long serialVersionUID = 1L;

	public static final String SERVICE_NAME = "ServerFile"; //nome do servico disponibilizado pelo server
    public static final int MAX_CHUNCK_SIZE = 10000; //bytes
	
	private final int MULTICAST_PORT = 700;
	//private final int UDP_PORT = 2000;
	protected HeartBeat hb= null;
	//protected UdpServerJava multicast = null; 
	
	protected File localDirectory;
	
	
	
	List<RemoteClient> observers;
	
	public Server(File localDirectory) throws RemoteException{
		super();
		this.localDirectory = localDirectory;
		observers = new ArrayList<>();
	}
	
	public void init(){
		
		try {
			Thread multicast = new Thread(new UdpServer(new HeartBeat("Servidor2", true),MULTICAST_PORT));
			multicast.setDaemon(true);
			multicast.start();
			
			
		} catch (SocketException | UnknownHostException e) {
			
			e.printStackTrace();
		}
		
		try{
			
			/*
			 * LANCA O REGISTRY OU SE JA ESTIVER A CORRER OBTEM UM REFERENCIA PARA O MESMO
			 * */
            
            Registry r;
            
            try{
                
                System.out.println("Tentativa de lancamento do registry no porto " + Registry.REGISTRY_PORT + "...");
                
                r = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
                
                System.out.println("Registry lancado!");
                                
            }catch(RemoteException e){
                System.out.println("Registry provavelmente ja' em execucao!");
                r = LocateRegistry.getRegistry();          
            }
            
            System.out.println("Servico GetRemoteFile RemoteTime criado e em execucao (" + this.getRef().remoteToString()+"...");
            
            /*
             * Regista o servico no rmiregistry local para que os clientes possam localiza'-lo, ou seja,
             * obter a sua referencia remota (endereco IP, porto de escuta, etc.).
             */
            
            r.rebind(SERVICE_NAME, this);     
                   
            System.out.println("Servico " + SERVICE_NAME + " registado no registry...");
            
            /*
             * Para terminar um servico RMI do tipo UnicastRemoteObject:
             * 
             *  UnicastRemoteObject.unexportObject(timeService, true);
             */
            
        }catch(RemoteException e){
            System.out.println("Erro remoto - " + e);
            System.exit(1);
        }
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		File localDirectory;
		    
	    // Trata os argumentos da linha de comando
		 
		if(args.length != 1){
		    System.out.println("Sintaxe: java GetFileUdpServer localRootDirectory");
		    return;
		}        
		
		
		System.out.println("Dir: "+ Paths.get(".").toAbsolutePath().normalize().toString() +"\\"+args[0].trim());
		localDirectory = new File(Paths.get(".").toAbsolutePath().normalize().toString() +"\\"+args[0].trim());
		
		if(!localDirectory.exists()){
		   System.out.println("A directoria " + localDirectory + " nao existe!");
		   System.out.println("A criar a directoria...");
		   if(!localDirectory.mkdir())
		   {		        	
			   System.out.println("Erro ao criar directoria... ");
			   return;       
		   }
		}
			    
		
		
		if(!localDirectory.isDirectory()){
			System.out.println("O caminho " + localDirectory + " nao se refere a uma directoria!");
		    return;
		}
		
		if(!localDirectory.canRead()){
			System.out.println("Sem permissoes de leitura na directoria " + localDirectory + "!");
		    return;
		}
		
		
		
		
		Server s;
		try {
			s = new Server(localDirectory);
			s.init();
			
			
			Thread.sleep(45000);
			UnicastRemoteObject.unexportObject(s, true);
			
		} catch (RemoteException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		}
	}

	
	
	public synchronized void notifyObservers(String msg)
    {
        int i;
        
        for(i=0; i < observers.size(); i++){
            try{       
                observers.get(i).notifyFileList(msg);
            }catch(RemoteException e){
                observers.remove(i--);
                System.out.println("- um observador (observador inacessivel).");
            }
        }
    }
	
	
	@Override
	public synchronized boolean getFile(String fileName, RemoteClient cli) throws RemoteException {
		
		
		String requestedCanonicalFilePath = null;
        FileInputStream requestedFileInputStream = null;
        byte [] fileChunck = new byte[MAX_CHUNCK_SIZE];
        int nbytes;
        
        fileName = fileName.trim();
        System.out.println("Recebido pedido para: " + fileName + ".");
        
        try{
            System.out.println("Origem do pedido: " + getClientHost()+ ".");
        }catch(ServerNotActiveException e){}
        
        System.out.println();
        
        try{

            /*
             * Verifica se o ficheiro solicitado existe e encontra-se por baixo da localDirectory.
             */
            
            requestedCanonicalFilePath = new File(localDirectory+File.separator+fileName).getCanonicalPath();

            if(!requestedCanonicalFilePath.startsWith(localDirectory.getCanonicalPath()+File.separator)){
                System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
                System.out.println("A directoria de base nao corresponde a " + localDirectory.getCanonicalPath()+"!");
                
                notifyObservers("Solicitado ficheiro nao permitido: " + requestedCanonicalFilePath);                    
                try{
                    notifyObservers(" por um cliente em " + getClientHost());
                }catch(ServerNotActiveException e){}
                notifyObservers(".\n\n");
                
                return false;
            }

            /*
             * Abre o ficheiro solicitado para leitura.
             */
            requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);            
            System.out.println("Ficheiro " + requestedCanonicalFilePath + " aberto para leitura.");
            
            /*
             * Obtem os bytes do ficheiro por blocos de bytes.
             */
            while((nbytes = requestedFileInputStream.read(fileChunck))!=-1){                         

                /*
                 * Escreve o bloco actual no cliente, invocando o metodo writeFileChunk da sua interface remota.
                 */
                if(!cli.writeFileChunk(fileChunck, nbytes)){
                    System.out.print("Surgiu um problema ao tentar escrever um bloco do ficheiro " + requestedCanonicalFilePath);
                    System.out.println(" com " + nbytes + " bytes no cliente!");
                    
                    return false;
                }                    
                
            }
                
            System.out.println("Ficheiro " + requestedCanonicalFilePath + " transferido para o cliente com sucesso.");
            System.out.println();
                    
            return true;
            
        }catch(FileNotFoundException e){   //Subclasse de IOException                 
            System.out.println("Ocorreu a excepcao {" + e + "} ao tentar abrir o ficheiro " + requestedCanonicalFilePath + "!"); 
                        
            notifyObservers("Nao foi possivel abrir o ficheiro " + requestedCanonicalFilePath + " solicitado");                    
            try{
                notifyObservers(" por um cliente em " + getClientHost());
            }catch(ServerNotActiveException ex){}
            notifyObservers(".\n\n");            
            
        }catch(IOException e){
            System.out.println("Ocorreu a excepcao de E/S: \n\t" + e);
            
            notifyObservers("Ocorreu um problema ao ler o ficheiro " + requestedCanonicalFilePath + " solicitado");                    
            try{
                notifyObservers(" por um cliente em " + getClientHost());
            }catch(ServerNotActiveException ex){}
            notifyObservers(".\n\n");
            
        }finally{
            if(requestedFileInputStream != null){
                try {
                    requestedFileInputStream.close();
                } catch (IOException e) {}
            }
        }
        
        return false;
	}

	@Override
	public synchronized void addObserver(RemoteClient observer) throws RemoteException {
		// TODO Auto-generated method stub
		
		if(!observers.contains(observer)){
            observers.add(observer);
            System.out.println("+ um observador.");
        }
	}

	@Override
	public void removeObserver(RemoteClient observer) throws RemoteException {
		// TODO Auto-generated method stub
		
		if(observers.remove(observer))
            System.out.println("- um observador.");
		
	}

}
