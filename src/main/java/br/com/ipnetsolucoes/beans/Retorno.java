package br.com.ipnetsolucoes.beans;

import java.util.List;

public class Retorno<T> {
	
	private String mensagem;
	private T objeto; 
	private List<T> listaObjeto;
	private String error;
	
	public String getMensagem() {
		return mensagem;
	}
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}
	public T getObjeto() {
		return objeto;
	}
	public void setObjeto(T objeto) {
		this.objeto = objeto;
	}
	public List<T> getListaObjeto() {
		return listaObjeto;
	}
	public void setListaObjeto(List<T> listaObjeto) {
		this.listaObjeto = listaObjeto;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
	

}
