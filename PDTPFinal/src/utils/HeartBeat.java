package utils;

import java.io.Serializable;

public class HeartBeat implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5399492312395349562L;
	private String nome;
	private boolean primario;
	
	
	public HeartBeat(String nome, boolean primario) {
		this.nome = nome;
		this.primario = primario;
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
	
	
}
