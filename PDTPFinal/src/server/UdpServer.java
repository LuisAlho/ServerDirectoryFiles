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
			System.out.println("Err - " + e.getMessage());
			System.exit(1);			
		}
		
	}
	
	protected void init() throws IOException{
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    oos.writeObject(hb);
	    oos.flush();
		
	    byte[] buf = baos.toByteArray();
	    
		packet = new DatagramPacket(buf, buf.length, addrUdpMulticast, portUdp);
		
		socket.send(packet);
		
		byte[] receive = new byte[256];
		packet.setData(receive,0,receive.length);
		socket.receive(packet);
		
		String msg = new String(packet.getData(), 0, packet.getLength());
		System.out.println(msg);
		
		socket.close();
	}
}
