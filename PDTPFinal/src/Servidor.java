

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
//import java.util.Date;
import java.util.List;

public class Servidor  extends UnicastRemoteObject implements RemoteServidor {
	
	private static final long serialVersionUID = 1L;

	
    public static final int MAX_CHUNCK_SIZE = 10000; //bytes
	
	private static final int MULTICAST_PORT = 7000;
	private static final String MULTICAST_IP = "225.15.15.15";
	
	private FileOutputStream fout;
	File directory;
	List<RemoteClient> observers;
	
	
	
	public void setFout(FileOutputStream fout) {
		this.fout = fout;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}



	protected Servidor() throws RemoteException {
		super();
		fout = null;
		observers = new ArrayList<>();
		
	}

	

	public static void main(String[] args) {
		
		
		File localDirectory;
		String nome;
		Servidor srv;
		RemoteServidor srvRemote = null;
		InetAddress group = null;
	    MulticastSocket socket = null;
	    boolean primario = true;
	    //DadosServidor srvPrimario = null;
	    HeartBeat hblocal;
	    
		    
	    // Trata os argumentos da linha de comando
		 
		if(args.length != 2){
		    System.out.println("Sintaxe: java Server localRootDirectory NomeServidor");
		    return;
		}        

		nome = args[1].trim();
		
		System.out.println("Dir: "+ Paths.get(".").toAbsolutePath().normalize().toString() + File.separator +args[0].trim());
		localDirectory = new File(Paths.get(".").toAbsolutePath().normalize().toString() +File.separator+args[0].trim());
		
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

		
		try {
			srv = new Servidor();
			srv.setDirectory(localDirectory);
			
			
			/*****************/
			
			try {
				
				group = InetAddress.getByName(MULTICAST_IP);	
				socket = new MulticastSocket(MULTICAST_PORT);	
				socket.joinGroup(group);
				socket.setSoTimeout(15000);

				byte[] response = new byte[1024];
				long currentTime = System.currentTimeMillis();
				long endTime = currentTime + 15000;
				
				
				DatagramPacket incomingPacket = new DatagramPacket(response, response.length);
				
				while(System.currentTimeMillis() < endTime) {
				
					socket.receive(incomingPacket);
		
					byte[] data = incomingPacket.getData();
					ByteArrayInputStream in = new ByteArrayInputStream(data);
					ObjectInputStream is;
					
					is = new ObjectInputStream(in);
					hblocal = (HeartBeat) is.readObject();
					System.out.println("HeartBeat object received = " + hblocal.toString());
					System.out.println("From: " + incomingPacket.getAddress().toString() + " : " + incomingPacket.getPort());
					if (hblocal.isPrimario()){
						primario = false;
						//srvPrimario = new DadosServidor(hblocal.getNome(),incomingPacket.getAddress().toString(), new Date(), incomingPacket.getPort(), hblocal.getServerInterface());
						srvRemote = (RemoteServidor) UnicastRemoteObject.toStub( hblocal.getServerInterface());
						is.close();
						break;
					}
				}
			}catch (ClassNotFoundException e) {			
				System.exit(1);
			} catch (SocketException e) {
				System.out.println("Err - " + e.getMessage());
				System.exit(1);
			} catch (SocketTimeoutException e) {
				System.out.println("Nao Recebeu nenhum packet durante os 15 segundos - " + e.getMessage());	
			} catch (IOException e) {
				System.exit(1);
			}
			
			
			if (!primario){
				try {
					System.out.println("Actualizar directoria de trabalho...");
					String str[] = srvRemote.getFileList();
					
					for(String name:str){
						System.out.println(name);
						
						String localFilePath = new File(localDirectory.getPath()+File.separator+name).getCanonicalPath();
				        
						 
				        srv.setFout(new FileOutputStream(localFilePath));
				         
				         System.out.println("Ficheiro " + localFilePath + " criado.");
						 
						if( srvRemote.getFileServer(name, srv)){ 
			                System.out.println("Transferencia do ficheiro " + name + " concluida com sucesso.");
			            }else{
			                System.out.println("Transferencia do ficheiro " + name + " concluida SEM sucesso.");
			            }            
						
					}
					
				} catch (RemoteException e) {
					System.out.println("Erro ao actualizar directorio..." + e.getMessage());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					
					srv.setFout(null);
					
				}
			}
			
			
			/*INICIA ENVIO DE HB A CADA 5 SEGUNDOS*/
			try {
				hblocal = new HeartBeat(nome, primario, srv );
				Thread multicastThread = new Thread(new UdpServer(hblocal, MULTICAST_PORT));
				multicastThread.setDaemon(true);
				multicastThread.start();
				
				
			} catch (SocketException | UnknownHostException e) {
				
				e.printStackTrace();
			}
			
			
			Thread.sleep(450000);
			UnicastRemoteObject.unexportObject(srv, true);
			
		} catch (RemoteException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		}
	}
		
		
		
		/*
		 * INTERFACE REMOTA
		 * 
		 * */

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
            
            requestedCanonicalFilePath = new File(directory+File.separator+fileName).getCanonicalPath();

            if(!requestedCanonicalFilePath.startsWith(directory.getCanonicalPath()+File.separator)){
                System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
                System.out.println("A directoria de base nao corresponde a " + directory.getCanonicalPath()+"!");
                
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

	public synchronized String[] getFileList() throws RemoteException{
		
		
		System.out.println("A enviar a lista de ficheiros para o cliente ");
		
		String[] str = directory.list();
		return str;
	} 
	
	@Override
	public void removeObserver(RemoteClient observer) throws RemoteException {
		
		
		if(observers.remove(observer))
            System.out.println("- um observador.");
		
	}

	@Override
	public synchronized boolean getFileServer(String fileName, RemoteServidor srv) throws RemoteException {
		
		
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
            
            requestedCanonicalFilePath = new File(directory+File.separator+fileName).getCanonicalPath();

            if(!requestedCanonicalFilePath.startsWith(directory.getCanonicalPath()+File.separator)){
                System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
                System.out.println("A directoria de base nao corresponde a " + directory.getCanonicalPath()+"!");
                
                System.out.println("Solicitado ficheiro nao permitido: " + requestedCanonicalFilePath);                    
                try{
                	System.out.println(" por um server em " + getClientHost());
                }catch(ServerNotActiveException e){}
                System.out.println(".\n\n");
                
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
                if(!srv.writeFileChunk(fileChunck, nbytes)){
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
                        
            System.out.println("Nao foi possivel abrir o ficheiro " + requestedCanonicalFilePath + " solicitado");                    
            try{
            	System.out.println(" por um server em " + getClientHost());
            }catch(ServerNotActiveException ex){}
            System.out.println(".\n\n");            
            
        }catch(IOException e){
            System.out.println("Ocorreu a excepcao de E/S: \n\t" + e);
            
            System.out.println("Ocorreu um problema ao ler o ficheiro " + requestedCanonicalFilePath + " solicitado");                    
            try{
            	System.out.println(" por um server em " + getClientHost());
            }catch(ServerNotActiveException ex){}
            
            
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
	public boolean writeFileChunk(byte[] fileChunk, int nbytes) throws RemoteException {
		  if(fout == null){
	            System.out.println("Nao existe qualquer ficheiro aberto para escrita!");
	            return false;
	        }
	        
	        try {
	            fout.write(fileChunk, 0, nbytes);
	        } catch (IOException e) {
	            System.out.println("Excepcao ao escrever no ficheiro: " + e);
	            return false;
	        }
	        return true;
	}
	
}
