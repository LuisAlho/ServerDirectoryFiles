

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Client extends UnicastRemoteObject implements RemoteClient {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String AUTH_FAIL = "FAIL_AUTH";
	private static final String NO_SERVER = "NO_SERVER";
	
	private static RemoteServidor remoteFileService;
	
	private FileOutputStream fout;
    
    public Client() throws RemoteException
    {
        fout = null;
    }

    public void setFout(FileOutputStream fout) {
        this.fout = fout;
    }

    
    public static void menu(){
		
		
		System.out.println("\n\nMENU");
		System.out.println("1 - Download");
		System.out.println("2 - Upload");
		System.out.println("3 - Delete");
		System.out.println("4 - Visualizacao");
		System.out.println("5 - Sair");

	}
    
    public static void main(String[] args){
        
        String objectUrl;        
        File localDirectory;
        String fileName;             
        int opcao;
        
        String localFilePath;
        FileOutputStream localFileOutputStream = null;     
        
        User user;
        Client myRemoteService = null;
        
        DirService srvDir;
        
        if ( args.length != 2 )
		{
			System.out.println("Dir: " +args[1] + "\nIP: " +args[0] );	
			System.out.println("Syntaxe: java Client \"Directoria\" \"IP\"");
			System.exit(1);
		}
		
		System.out.println("Dir: " +args[0] + "\nIP: " +args[1] );
		
		objectUrl = "rmi://"+ args[1] +"/DirService";       
        localDirectory = new File(args[0].trim());
        
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
 		
 		user = new User();
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Nome: ");
		user.setName(sc.nextLine());
		System.out.println("Password: ");
		user.setPassword(sc.nextLine());
        
		
        
        try {
			srvDir = (DirService)Naming.lookup(objectUrl);
			myRemoteService = new Client();
			
			String msg = srvDir.doAuthentication(user);
            if(msg.equals(AUTH_FAIL)){
            	System.out.println("User nao Registado");
            	System.exit(1);
            }else{ 
            	if(msg.equals(NO_SERVER)){
	            
	            	System.out.println("Por favor tente mais tarde nao existem servidores desponiveis..." + msg);
	            	System.exit(1);
	            }
            }
            
            
            remoteFileService = (RemoteServidor) UnicastRemoteObject.toStub(srvDir.getRemoteServerInterface());
            
            System.out.println("Server: " + remoteFileService.toString());
              
            
            /* REGISTAR A INTERFACE REMOTA DO CLIENTE*/
            remoteFileService.addObserver(myRemoteService);
            
			
			
			 /*INICAR MENU COM OPCOES A EFECTUAR PELO CIENTE*/
			do{
				do{
					System.out.println("Ficheiros:\n ");
		            String str[] = remoteFileService.getFileList();
					if(str!= null){
						for(String name:str){
							System.out.println("\t -" + name);
						}
					}
					menu();
					System.out.println("\n Opcao: ");
					opcao =Integer.parseInt( sc.nextLine());
					
				}while(opcao > 5 || opcao < 1);
				
				
				switch (opcao){
					
					case 1: try {
								
								System.out.println("Nome do ficheiro: ");
								 
								 fileName = sc.nextLine();
								 
								 
								  localFilePath = new File(localDirectory.getPath()+File.separator+fileName).getCanonicalPath();
						          localFileOutputStream = new FileOutputStream(localFilePath);
								 
						          myRemoteService.setFout(localFileOutputStream);
						         
						         System.out.println("Ficheiro " + localFilePath + " criado.");
								 
								if(remoteFileService.getFile(fileName, myRemoteService)){ 
					                System.out.println("Transferencia do ficheiro " + fileName + " concluida com sucesso.");
					            }else{
					                System.out.println("Transferencia do ficheiro " + fileName + " concluida SEM sucesso.");
					            }            
												
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}finally{
					            
					            
					        }
							break;
					case 2: break;
					case 3: break;
					case 4: break;
					case 5: System.exit(1); 
							break;
					default: break;
				}
            }while(opcao != 0);
			sc.close();
        }catch(RemoteException e){
            System.out.println("Erro remoto - " + e);
        }catch(NotBoundException e){
            System.out.println("Servico remoto desconhecido - " + e);
        }catch(IOException e){
            System.out.println("Erro E/S - " + e);
        }catch(Exception e){
            System.out.println("Erro - " + e);
            e.printStackTrace();
          }finally{
              if(localFileOutputStream != null){
                  /*
                   * Encerra o ficheiro.
                   */
                  try{
                      localFileOutputStream.close();
                  }catch(IOException e){}
              }
              
              if(myRemoteService != null){
                  /*
                    * Retira do servico local a referencia para o objecto localFileOutputStream
                    */
                  myRemoteService.setFout(null);
                  /*
                   * Termina o servi�o local
                   */
                  try{
                      UnicastRemoteObject.unexportObject(myRemoteService, true);
                  }catch(NoSuchObjectException e){}
              }
          }
    }
    
    
	@Override
	public void notifyFileList(String msg) throws RemoteException {
		
		if(msg.equals("list")){
			remoteFileService.getFileList();
		}
		else
		{
			System.out.println(msg);
			
		}
		
		// TODO Auto-generated method stub
		
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

