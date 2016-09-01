package utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class DirMulticast implements Runnable {
	
	int port;
	InetAddress group = null;
	MulticastSocket socket = null;
	
	public DirMulticast(int port){
		
		try {
			this.port = port;
			group = InetAddress.getByName("225.15.15.15");
			socket = new MulticastSocket(port);
			
			socket.joinGroup(group);
			socket.setTimeToLive(1); //TTL	
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		byte[] response = new byte[1024];
		DatagramPacket packet = new DatagramPacket(response, response.length);
		try {
			socket.receive(packet);
			
			String msg = packet.getData().toString();
			System.out.println(msg);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
