package utils;

import java.io.Serializable;

import server.RemoteServidor;



public class HeartBeat implements Serializable {
	

	private static final long serialVersionUID = 1L;
	private String nome;
	private RemoteServidor serverInterface;
	//private Teste teste;
	private boolean primario;
	
	
	public HeartBeat(String nome, boolean primario, RemoteServidor rt) {
		this.nome = nome;
		this.primario = primario;
		//this.teste = rt;
		this.serverInterface = rt;
	}
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public boolean isPrimario() {
		return primario;
	}
	public void setPrimario(boolean primario) {
		this.primario = primario;
	}

	@Override
	public String toString() {
		String msg = "Primario";
		if (!primario){
			msg="Secundario";
		}
		return "HeartBeat [Nome=" + nome + " : " + msg + "]";
	}

	public RemoteServidor getServerInterface() {
		return serverInterface;
	}

	public void setServerInterface(RemoteServidor serverInterface) {
		this.serverInterface = serverInterface;
	}

/*
	public Teste getTeste() {
		return teste;
	}

	public void setTeste(Teste teste) {
		this.teste = teste;
	}*/
}
