package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import utils.HeartBeat;

public class UdpServer implements Runnable {

	/**
	 * 
	 */
	
	protected HeartBeat hb = null;
	protected InetAddress addrUdpMulticast = null;
	protected DatagramPacket packet = null;
	protected DatagramSocket socket = null;
	protected int portUdp;
	
	public UdpServer(HeartBeat hb, int port) throws SocketException, UnknownHostException {
		
		this.hb = hb;
		this.portUdp = port;
		addrUdpMulticast = InetAddress.getByName("225.15.15.15");
		socket = new DatagramSocket();
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			
			init();
		} catch (IOException e) {
			System.out.println("Erro ao serializar HB object - " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			System.out.println("Thread Multicast HB interrompida - " + e.getMessage());
			socket.close();
		}finally{
			socket.close();
			System.exit(1);
		}
		
	}
	
	protected synchronized void init() throws IOException, InterruptedException{
	
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    oos.writeObject(hb);
	    oos.flush();
	    
	    
	    byte[] buf = baos.toByteArray();
	    
		packet = new DatagramPacket(buf, buf.length, addrUdpMulticast, portUdp);
		
		//ENVIA HEART BEATS A CADA 5 SEGUNDOS
		while(true){
			
			System.out.println("A enviar HeartBeat...");
			socket.send(packet);
			Thread.sleep(5000);
		}
		
	}
}
