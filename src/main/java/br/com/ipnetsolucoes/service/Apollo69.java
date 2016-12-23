package br.com.ipnetsolucoes.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.Users;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;

public class Apollo69 {
	/** Application name. */
	private static final String APPLICATION_NAME = "IPNET Contacts";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
			".credentials/IPNET");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	private static final List<String> SCOPES = Arrays.asList(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);

	private static final List<String> ESCOPOS = Arrays.asList("https://www.google.com/m8/feeds/");
	
	private static String private_key_id;
	
	private static String client_email;
	
	private static String private_key;
	
	private static String client_id;
	
	private static String type;
	
	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
	public static void generateSecrets(String jsonPath) throws Exception{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		if(classLoader == null){
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
	
	public static void stuff(JSONObject objJson) {
		private_key_id = (String) objJson.get("private_key_id");
		client_email = (String) objJson.get("client_email");
		private_key = (String) objJson.get("private_key");
		client_id = (String) objJson.get("client_id");
		type = (String) objJson.get("type");
	}
	
	private static String getPrivate_key(){
		return private_key;
	}
	
	public static File getCredential() {
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

	public static Credential authorize() throws Exception {
		generateSecrets("service_account.json");
		
		GoogleCredential credential = new GoogleCredential.Builder()
				.setServiceAccountPrivateKeyFromPemFile(getCredential()).setTransport(HTTP_TRANSPORT)
				.setJsonFactory(JSON_FACTORY).setServiceAccountUser("admin@demo.ipnetsolucoes.com.br")
				.setServiceAccountId("contacts-150418@appspot.gserviceaccount.com").setServiceAccountScopes(SCOPES)
				.build();
		System.out.println("Credential");
		return credential;
	}

	public static Directory getDirectoryService() throws Exception {
		Credential credential = authorize();
		return new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	public static Credential authorizeFeed() throws Exception {
		File file = new File("p12.p12");

		GoogleCredential credential = new GoogleCredential.Builder().setServiceAccountPrivateKeyFromP12File(file)
				.setTransport(HTTP_TRANSPORT).setJsonFactory(JSON_FACTORY)
				.setServiceAccountUser("zonasul@demo.ipnetsolucoes.com.br")
				.setServiceAccountId("contacts-150418@appspot.gserviceaccount.com").setServiceAccountScopes(ESCOPOS)
				.build();
		credential.refreshToken();
		credential.getAccessToken();
		System.out.println("Success");
		return credential;

	}

	public static void main(String[] args) throws Exception {
		// CRIA UM SERVIÇO AUTORIZADO DO DIRETÓRIO
		Directory service = getDirectoryService();

		// LISTA OS USUARIOS DO DOMINIO
		Users result = service.users().list().setCustomer("my_customer").setMaxResults(10).setOrderBy("email")
				.execute();

		List<User> users = result.getUsers();

		if (users == null || users.size() == 0) {
			System.out.println("Nenhum usuário encontrado.");
		} else {
			// LISTA OS USUÁRIOS DO DOMINIO
			for (User user : users) {
				// FILTRA OS USUARIOS (substituir posteriormente com arquivo de
				System.out.println(user.getName().getFullName());
				// propriedades)
				if (user.getName().containsValue("Sul")) {

					URL feedUrl = new URL(
							"https://www.google.com/m8/feeds/contacts/" + user.getPrimaryEmail() + "/full");
					// CRIA O SERVIÇO DE CONTATOS
					ContactsService myService = new ContactsService(APPLICATION_NAME);
					myService.setOAuth2Credentials(authorizeFeed());

					ContactFeed resultFeed = myService.getFeed(feedUrl, ContactFeed.class);

					// BASEADO NO FEED, PEGA OS CONTATOS DE CADA USUARIO
					for (ContactEntry entry : resultFeed.getEntries()) {
						if (!entry.getEmailAddresses().isEmpty()) {
							for (Email email : entry.getEmailAddresses()) {
								if (email.getAddress().contains("@zonasul")) {
									entry.delete();
									
								}
							}
						}
					}

					for (;;) {

					}
				}
			}
		}
	}

}
