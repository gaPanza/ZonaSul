package br.com.ipnetsolucoes.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Collection;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import br.com.ipnetsolucoes.beans.Retorno;
import br.com.ipnetsolucoes.util.GoogleJsonSecrets;

public class AuthService {
	private JacksonFactory jsonFactory;
	private HttpTransport httpTransport;
	private GoogleJsonSecrets googleJsonSecrets;
	
	public AuthService(String jsonCredentialPath) throws Exception{
		GoogleJsonSecrets googleJsonSecrets = new GoogleJsonSecrets(jsonCredentialPath);
		this.googleJsonSecrets = googleJsonSecrets;
		httpTransport = new NetHttpTransport();
		jsonFactory = new JacksonFactory();
		
	}

	public Retorno<GoogleCredential> getCrendencialByEmail(String userEmail, Collection<String> scopes) {
		Retorno<GoogleCredential> retorno = new Retorno<GoogleCredential>();
		GoogleCredential credential;
		try {
			System.out.println("CREDENCIAL");
			credential = new GoogleCredential.Builder()
					.setServiceAccountPrivateKeyFromPemFile(googleJsonSecrets.getCredential())
					.setTransport(httpTransport).setJsonFactory(jsonFactory).setServiceAccountUser(userEmail)
					.setServiceAccountId(googleJsonSecrets.getClient_email()).setServiceAccountScopes(scopes).build();

			credential.refreshToken();
			credential.getAccessToken();
			retorno.setObjeto(credential);
			
			

		} catch (UnknownHostException e) {
			e.printStackTrace();
			retorno.setError("without connection to the internet.");

		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			retorno.setError("Falha de segurança.");
			
		} catch (IOException e) {
			e.printStackTrace();
			retorno.setError("Falha de IO.");
			return retorno;
			
		} catch (Exception e) {
			e.printStackTrace();
			if(e.getMessage()!=null && e.getMessage().contains("UnknownHostException")){		
			    return retorno;
		    }
			retorno.setError("Falha Desconhecida");
		}
		return retorno;
	}

}
