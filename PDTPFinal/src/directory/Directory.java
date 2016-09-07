package directory;

import java.io.ByteArrayInputStream;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import server.RemoteServidor;
import utils.DadosServidor;
import utils.HeartBeat;
import utils.User;

public class Directory extends UnicastRemoteObject implements DirService {

	private static final String AUTH_FAIL = "FAIL_AUTH";
	private static final String NO_SERVER = "NO_SERVER";
	private static final long serialVersionUID = 1L;
	private Authentication auth = new Authentication();
	private int srvListIndex = 0;
	
	/*  LISTA DOS SERVIDORES REGISTADOS*/
	protected ArrayList<DadosServidor> srvList = new ArrayList<>(); 
	
	//MULTICAST UDP VARIABLES
	private final int PORT = 700;
	private InetAddress group = null;
	private MulticastSocket socket = null;

	//CONSTRUCTOR
	protected Directory() throws RemoteException {
		super();
	}

	public void init(){
		System.out.println("Servico de directoria iniciado");
		
		/* INCIAR LSITA DE UTILIZADORES REGISTADOS */
		auth.createList("C:\\Users\\Alho\\Documents\\GitHub\\ServerDirectoryFiles\\PDTPFinal\\bin\\directory\\users\\user.txt");
		//auth.createList("user.txt");
		auth.listarLista();
		
		
		/*INICIAR O REGISTO DA INTERFACE REMOTA*/
		initRegistry();
		
		/*INCIA THREAD DE VERIFICACAO DE INACTIVIDADE DOS SERVIDORES*/
		
		Thread verifyServer = new Thread(new VerifyServer());
		verifyServer.setDaemon(true); //Set DAEMON true para  terminar thread quando a main tb terminar
		verifyServer.start();
		
		/* INICIAR SERVICO DE ESCUTA EM MULTICAST
		 * 
		 * IP: 225.15.15.15
		 * PORTA: 700
		 * 
		 * */
		try {
			
			group = InetAddress.getByName("225.15.15.15");
			socket = new MulticastSocket(PORT);			
			socket.joinGroup(group);
			//socket.setTimeToLive(1); //TTL	
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] response = new byte[1024];
		//DatagramPacket packet = new DatagramPacket(response, response.length);
		while(true){
			
			DatagramPacket incomingPacket = new DatagramPacket(response, response.length);
			try {
				socket.receive(incomingPacket);	
				// ADICIONAR / ACTUALIZAR SERVIDOR RECEBIDO		
				tratarHeartBeatRecebido(incomingPacket);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void tratarHeartBeatRecebido(DatagramPacket packet) throws IOException, ClassNotFoundException{
		
		
		byte[] data = packet.getData();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		
		HeartBeat hb = (HeartBeat) is.readObject();
		System.out.println("HeartBeat object received = " + hb.toString());
		System.out.println("From: " + packet.getAddress().toString() + " : " + packet.getPort());
		
		synchronized(srvList){
			if(!srvList.isEmpty()){
				int flag = 0;
				Iterator<DadosServidor> it = srvList.iterator();
				while(it.hasNext()){
					
					DadosServidor s = it.next();
					if(hb.getNome().equals(s.getNome())){
						s.setDate(new Date());
						flag = 1;
					}
				}
				if (flag == 0)
					srvList.add(new DadosServidor(hb.getNome(),packet.getAddress().toString(), new Date(), packet.getPort(), hb.getServerInterface()));
					
			}else{
				
				srvList.add(new DadosServidor(hb.getNome(),packet.getAddress().toString(), new Date(), packet.getPort(), hb.getServerInterface()));	
			}
		}
	}

	/*INICIA MULTICAST */
	public void initMulticast(){
		
		try {
			
			group = InetAddress.getByName("225.15.15.15");
			socket = new MulticastSocket(PORT);
			
			socket.joinGroup(group);
			//socket.setTimeToLive(1); //TTL	
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/* INICIA REGISTO DO SERVICO REMOTO */
	public void initRegistry(){
		try{

            Registry r;

            try{

                System.out.println("\nTentativa de lancamento do registry no porto " + Registry.REGISTRY_PORT + "...");

                r = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

                System.out.println("Registry lancado!");

            }catch(RemoteException e){
                System.out.println("Registry provavelmente ja' em execucao!");
                r = LocateRegistry.getRegistry();
            }

            // Cria e lanca o servico,


            System.out.println("Servico remoto criado e em execucao (" + this.getRef().remoteToString()+"...");

            // Regista o servico para que os clientes possam encontra'-lo, ou seja,
            // obter a sua referencia remota (endereco IP, porto de escuta, etc.).
            
            r.rebind("DirService", this);

            System.out.println("Servico de Directoria registado no registry...\n");

        }catch(RemoteException e){
            System.out.println("Erro remoto - " + e.getMessage());
            System.exit(1);
        }catch(Exception e){
            System.out.println("Erro - " + e.getMessage());
            System.exit(1);
        }		
	}

	
	/******************/
	/*INTERFACE REMOTA*/
	/******************/
	
	@Override
	public synchronized String doAuthentication(User s) throws RemoteException {
		
		if(!auth.exist(s))
			return AUTH_FAIL;
			
		synchronized(srvList){
			if (!srvList.isEmpty())
				return NO_SERVER;
		}
		return "OK";
	}
	
	
	@Override
	public synchronized RemoteServidor getRemoteServerInterface() throws RemoteException {
		RemoteServidor msg;

		/*ENVIA A INTERFACE REMOTA DO SERVIDOR SEGUINDO O METODO ROUND-ROBIN*/
		
			if (!srvList.isEmpty()){
				if(srvListIndex > srvList.size()){
					
					msg = srvList.get(srvListIndex).getServerInterface();
					srvListIndex++;
					
					return msg;
					
				}else{
					srvListIndex = 0;
					msg = srvList.get(srvListIndex).getServerInterface(); 		
					return msg;
				}
			}
			return null;
		
	}
	
	/*************/
	/*MAIN METODO*/
	/*************/
	public static void main(String[] args) throws RemoteException {
		// TODO Auto-generated method stub

		Directory dir = new Directory();
		dir.init();
	}
	
	
	/*
	 * IMPLEMENTA VERIFICACAO DE INACTIVIDADE DOS SERVIDORES
	 *  
	 * */
	
	class VerifyServer implements Runnable{

		@Override
		public  void run() {
			
			// VERIFICA SE NAO RECEBE HA MAIS DE 15 SEGUNDOS
			try {
				System.out.println("\nIniciada verificacao periodica dos servidores");
				while(true){

					synchronized(srvList){
						if( !srvList.isEmpty()){
							
							Iterator<DadosServidor> it = srvList.iterator();
							while (it.hasNext()) {
								
								DadosServidor s = it.next();
								//SE TEMPO FOR SUPERIOR A 15 SEGUNDOS ELIMINA REGISTO
								if(secondsBetween( new Date(), s.getDate()) > 15){
									System.out.println("Servidor \"" + s.getNome() + "\" removido da lista por inactividade");
									it.remove();
								}
							}
						}
					}
					//  ESPERA 5 SEGUNDOS ATE PROXIMA VERIFICACAO
					Thread.sleep(5000);
				}				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private int secondsBetween(Date first, Date second){
			
			int seconds = (int) (first.getTime() - second.getTime()) / 1000;
			System.out.println("\nSegundos: " + seconds);
			return seconds;
		}
	}


	
	
	
}
