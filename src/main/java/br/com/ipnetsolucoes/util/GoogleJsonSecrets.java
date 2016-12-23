package br.com.ipnetsolucoes.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class GoogleJsonSecrets {
	private String private_key_id;
	private String client_email;
	private String private_key;
	private String client_id;
	private String type;

	public GoogleJsonSecrets() {
	}

	public GoogleJsonSecrets(String jsonPath) throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = Class.class.getClassLoader();
		}
		File f = new File(jsonPath);
		InputStream resourceAsStream = new FileInputStream(f);
		BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}

		JSONParser parser = new JSONParser();
		Object obj = parser.parse(sb.toString());
		org.json.simple.JSONObject objJson = (org.json.simple.JSONObject) obj;
		stuff(objJson);
		br.close();
	}

	public void stuff(JSONObject objJson) {
		this.private_key_id = (String) objJson.get("private_key_id");
		this.client_email = (String) objJson.get("client_email");
		this.private_key = (String) objJson.get("private_key");
		this.client_id = (String) objJson.get("client_id");
		this.type = (String) objJson.get("type");
		System.out.println(objJson.toJSONString());
	}


	@Override
	public String toString() {
		return "GoogleJsonSecrets [private_key_id=" + private_key_id + ", client_email=" + client_email
				+ ", private_key=" + private_key + ", client_id=" + client_id + ", type=" + type + "]";
	}

	public String getPrivate_key_id() {
		return private_key_id;
	}

	public void setPrivate_key_id(String private_key_id) {
		this.private_key_id = private_key_id;
	}

	public String getClient_email() {
		return client_email;
	}

	public void setClient_email(String client_email) {
		this.client_email = client_email;
	}

	public String getPrivate_key() {
		return private_key;
	}

	public void setPrivate_key(String private_key) {
		this.private_key = private_key;
	}

	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public File getCredential() {
		File jsonFileTemp;
		try {
			jsonFileTemp = buildTempFile(getPrivate_key());
			return jsonFileTemp;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static File buildTempFile(String data) throws IOException {
		File temp = File.createTempFile("tempfile", ".tmp");
		BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
		bw.write(data);
		bw.close();
		return temp;
	}
}
