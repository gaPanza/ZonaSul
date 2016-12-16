package br.com.ipnetsolucoes.util;

import java.util.Properties;

public class Configuracao {
	private String csvPath;
	private String pathClientSecretJson;
	private Boolean deleteContactsDestiny;

	public Configuracao() {
	}

	public Configuracao(Properties p, String csvPath) {
		this.csvPath = csvPath;
		this.pathClientSecretJson = p.getProperty("main.path.client.secret.json").trim();
		this.deleteContactsDestiny = (p.getProperty("contacts.delete.contatos.destino") != null
				&& p.getProperty("contacts.delete.contatos.destino").trim().equalsIgnoreCase("true"));
	}

	public String getCsvPath() {
		return csvPath;
	}

	public void setCsvPath(String csvPath) {
		this.csvPath = csvPath;
	}

	public String getPathClientSecretJson() {
		return pathClientSecretJson;
	}

	public void setPathClientSecretJson(String pathClientSecretJson) {
		this.pathClientSecretJson = pathClientSecretJson;
	}

	public Boolean getDeleteContactsDestiny() {
		return deleteContactsDestiny;
	}

	public void setDeleteContactsDestiny(Boolean deleteContactsDestiny) {
		this.deleteContactsDestiny = deleteContactsDestiny;
	}
	
	
}
