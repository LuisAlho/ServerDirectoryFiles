package utils;


import java.util.Date;

import server.RemoteServidor;

public class DadosServidor {

	private String nome;
	private String ip;
	private Date date;
	private RemoteServidor serverInterface = null;
	private int portoUDP;
	
	
	public DadosServidor(String nome, String ip, Date date, int portoUDP, RemoteServidor serverInterface) {
		this.nome = nome;
		this.ip = ip;
		this.date = date;
		this.portoUDP = portoUDP;
		this.serverInterface = serverInterface;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getPortoUDP() {
		return portoUDP;
	}
	public void setPortoUDP(int portoUDP) {
		this.portoUDP = portoUDP;
	}
	public RemoteServidor getServerInterface() {
		return serverInterface;
	}
	public void setServerInterface(RemoteServidor serverInterface) {
		this.serverInterface = serverInterface;
	}
}
