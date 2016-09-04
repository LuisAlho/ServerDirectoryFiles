package directory;

import java.io.ByteArrayInputStream;

/*
 * Esquecer servidor ao fim de 3 HB
 * Actualizar interface remota de modo a enviar o ip dos servidores registados 
 *  - (Metodo Round-Robin)
 *  
 *  How to know the time in seconds between this two dates
 *  private long secondsBetween(Date first, Date sedond){
 *  	return 
 *  }
 *  int seconds = (date1.getTime() - date2.getTime()) / 1000;
 *  
 *  Implementar class HearBeat e DadosServidor 
 * */

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

import utils.DadosServidor;
import utils.HeartBeat;
import utils.User;

public class Directory extends UnicastRemoteObject implements DirService {

	private static final long serialVersionUID = 1L;
	private Authentication auth = new Authentication();
	private ArrayList<DadosServidor> srvList = null; // new ArrayList<String>();
	
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
		//Iniciar lista de users

		auth.createList("C:\\Users\\Alho\\Documents\\GitHub\\ServerDirectoryFiles\\PDTPFinal\\bin\\directory\\users\\user.txt");
		auth.listarLista();
		initRegistry();
		//Thread multicast = new Thread(new DirMulticast(700), "MulticastDir");
		//multicast.start();
		//initMulticast();
		
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
				byte[] data = incomingPacket.getData();
				ByteArrayInputStream in = new ByteArrayInputStream(data);
				ObjectInputStream is = new ObjectInputStream(in);
				
				HeartBeat hb = (HeartBeat) is.readObject();
				System.out.println("HeartBeat object received = " + hb.toString());
				System.out.println("From: " + incomingPacket.getAddress().toString() + " : " + incomingPacket.getPort());
				
				byte[] send = new byte[256];
				
				send = "HeartBeat Recebido com sucesso".getBytes();
	

				incomingPacket.setData(send, 0, send.length);
				socket.send(incomingPacket);
			
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			
			/*try {
				
				System.out.println("Multicast waiting");
			
				socket.receive(packet);
				
				String msg = new String(packet.getData(),0,packet.getLength());
				System.out.println(msg);
				
				tratarPacketMulticast(packet);
				
				socket.send(packet);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}

	private void tratarPacketMulticast(DatagramPacket packet){
		
		srvList.add(new DadosServidor("Servidor 1","ip", new Date(), 25));
		
	}

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
	
	public void initRegistry(){
		try{

            Registry r;

            try{

                System.out.println("Tentativa de lancamento do registry no porto " + Registry.REGISTRY_PORT + "...");

                r = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

                System.out.println("Registry lancado!");

            }catch(RemoteException e){
                System.out.println("Registry provavelmente ja' em execucao!");
                r = LocateRegistry.getRegistry();
            }

            // Cria e lanca o servico,


            System.out.println("Servico RemoteTime criado e em execucao ("+this.getRef().remoteToString()+"...");

            // Regista o servico para que os clientes possam encontra'-lo, ou seja,
            // obter a sua referencia remota (endereco IP, porto de escuta, etc.).

            r.rebind("DirService", this);

            System.out.println("Servico de Directoria registado no registry...");

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
	public String getIpServer(User s) throws RemoteException {

		if(!auth.exist(s)){
			return "Fail";
		}
		return "O ip do servidor e: localhost";
	}
	
	/*************/
	/*MAIN METODO*/
	/*************/
	public static void main(String[] args) throws RemoteException {
		// TODO Auto-generated method stub

		Directory dir = new Directory();
		dir.init();
	}
}
