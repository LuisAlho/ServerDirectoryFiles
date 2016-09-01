package utils;

public class HeartBeat {
	
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
}
