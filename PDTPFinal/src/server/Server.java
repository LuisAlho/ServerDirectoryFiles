package server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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

public class Server {
	
	private final int MULTICAST_PORT = 700;
	//private final int UDP_PORT = 2000;
	protected HeartBeat hb= null;
	//protected UdpServerJava multicast = null; 
	
	public void init(){
		
		try {
			Thread multicast = new Thread(new UdpServer(new HeartBeat("Servidor1", true),MULTICAST_PORT));
			multicast.start();
		} catch (SocketException | UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		try {
			InetAddress addr = InetAddress.getByName("225.15.15.15");
			//byte[] data = new byte[1024];
			
			byte[] data = "HeartBeat".getBytes();
			
			
			DatagramPacket packet = new DatagramPacket (data, data.length, addr, MULTICAST_PORT);
			DatagramSocket socket = new DatagramSocket(UDP_PORT);
			
			socket.send(packet);
			System.out.println("HeartBeat Enviado... ");
			System.out.println("A espera de resposta durante 15 segundos...");
			
			socket.setSoTimeout(15000);
			
			byte[] receive = new byte[256];
			packet.setData(receive,0,receive.length);
			socket.receive(packet);
			
			String msg = new String(packet.getData(), 0, packet.getLength());
			System.out.println(msg);
			
			
			socket.close();
			
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(InterruptedIOException e) {
			System.out.println("Receive Timeout -" + e.getMessage());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server s = new Server();
		s.init();
	}

}
