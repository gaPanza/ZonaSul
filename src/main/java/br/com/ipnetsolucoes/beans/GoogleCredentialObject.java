package br.com.ipnetsolucoes.beans;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public class GoogleCredentialObject<T> {
	private GoogleCredential credential;
	private T object;
	public GoogleCredential getCredential() {
		return credential;
	}
	public void setCredential(GoogleCredential credential) {
		this.credential = credential;
	}
	public T getObject() {
		return object;
	}
	public void setObject(T object) {
		this.object = object;
	}
	

}
