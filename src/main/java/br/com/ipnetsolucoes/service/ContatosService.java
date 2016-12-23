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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.Users;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.BillingInformation;
import com.google.gdata.data.contacts.Birthday;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.DirectoryServer;
import com.google.gdata.data.contacts.Initials;
import com.google.gdata.data.contacts.MaidenName;
import com.google.gdata.data.contacts.Mileage;
import com.google.gdata.data.contacts.Nickname;
import com.google.gdata.data.contacts.Occupation;
import com.google.gdata.data.contacts.ShortName;
import com.google.gdata.data.contacts.Subject;
import com.google.gdata.data.extensions.AdditionalName;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.NamePrefix;
import com.google.gdata.data.extensions.PhoneNumber;
import com.opencsv.CSVReader;

import br.com.ipnetsolucoes.beans.ActionStatus;
import br.com.ipnetsolucoes.beans.GoogleCredentialObject;
import br.com.ipnetsolucoes.beans.Retorno;
import br.com.ipnetsolucoes.dao.ContactsDao;
import br.com.ipnetsolucoes.util.Configuracao;
import br.com.ipnetsolucoes.util.GlobalLog;

public class ContatosService {
	private static final String APPLICATION_NAME = "IPNET Contacts";

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private static HttpTransport HTTP_TRANSPORT;

	private static final List<String> DIRECTORY_SCOPES = Arrays.asList(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);

	private static final List<String> CONTACTS_SCOPES = Arrays.asList("https://www.google.com/m8/feeds/");

	private static String private_key_id;

	private static String client_email;

	private static String private_key;

	private static String client_id;

	private static String type;

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	public ContatosService() {
	}

	public void ProcessAtualization(final Retorno<Configuracao> config, String userMail, File csv) {
		// INICIA O LOG
		GlobalLog logs = new GlobalLog();
		try {
			String logName = new SimpleDateFormat("dd_MM_YYYY_hh_mm_ss").format(new Date()) + "_ATUALIZAÇAO";
			System.setProperty("logfilename", logName);
			System.setProperty("logPath", GlobalLog.getJarPath(ContatosService.class) + "logs/");
			Logger aux = GlobalLog.generateLog(logName, System.getProperty("logPath"));

			logs.setLogGlobal(aux);
			logs.insertLog("LOGS INICIADOS", ActionStatus.SUCCESS, null);
		} catch (Exception e) {
			logs.insertLog("FALHA AO INICIAR LOG", ActionStatus.FAILED, e);
		}

		// INICIA A ATUALIZAÇÃO
		// PARSE DO CSV ACONTECE

		// PEGA OS USERS DO DIRETORIO
		try {
			logs.insertLog("CRIANDO O DIRECTORY SERVICE", ActionStatus.INFO, null);
			Directory service = getDirectoryService();

			Users result = service.users().list().setCustomer("my_customer").setOrderBy("email").execute();

			List<User> users = result.getUsers();

			if (users == null || users.size() == 0) {
				logs.insertLog("NÃO HÁ USUÁRIOS NO DIRETÓRIO", ActionStatus.WARNING, null);
			} else {
				for (User user : users) {
					logs.insertLog("USUÁRIO: " + user.getName().getFullName() + " SENDO ATUALIZADO.", ActionStatus.INFO,
							null);
					// PEGANDO OS CONTATOS
					try {
						URL feedURL = new URL(
								"https://www.google.com/m8/feeds/contacts/" + user.getPrimaryEmail() + "/full");

						ContactsService myService = new ContactsService(APPLICATION_NAME);
						myService.setOAuth2Credentials(authorizeFeed());

						ContactFeed resultFeed = myService.getFeed(feedURL, ContactFeed.class);

						for (ContactEntry entry : resultFeed.getEntries()) {
							if (!entry.getEmailAddresses().isEmpty()) {
								for (Email email : entry.getEmailAddresses()) {
									if (email.getAddress().contains("@zonasul")) { // -------------------
																					// PARTE
																					// COM
																					// HARDCODE
										try {
											entry.delete();
										} catch (Exception e) {
											logs.insertLog("NÃO FOI POSSÍVEL DELETAR O CONTATO:" + email,
													ActionStatus.FAILED, e);
										}
									}
								}
							}
						}

					} catch (Exception e) {
						logs.insertLog(
								"ERRO AO TENTAR RECUPERAR OS CONTATOS DO USUÁRIO: " + user.getName().getFullName(),
								ActionStatus.FATAL, e);
					}
				}
			}
		} catch (Exception e) {
			logs.insertLog("ERRO AO PEGAR OS USUÁRIOS DO DIRETÓRIO", ActionStatus.FATAL, e);
		}
		// CRIA UMA LISTA E COMEÇA UMA ITERACAO

		// PEGA OS CONTATOS DE CADA UM

		// DELETA OS CONTATOS @ZONASUL

		// PARSE DO CSV

		Retorno<GoogleCredentialObject<ContactsService>> destinyContacts = getContactsServiceByUserMail(userMail,
				config.getObjeto());

		// PEGANDO CRIANDO E PERSISTINDO OS CONTATOS DO CSV

		// FINALIZANDO A MIGRAÇÃO

		// Adicao de logs
	}

	public Retorno<ContactFeed> getContactsFromEmail(GoogleCredentialObject<ContactsService> service,
			Configuracao config) {
		Retorno<ContactFeed> retorno = new Retorno<ContactFeed>();
		try {
			ContactsDao contactsDao = new ContactsDao();
			retorno.setObjeto(contactsDao.getAllContactsByCredential(service.getObject()));
			return retorno;
		} catch (Exception e) {
			try {
				Thread.sleep(2000);
				if (e.getMessage() != null && e.getMessage().contains("403 Forbidden")) {
					service = refreshTokenContacts(service, config);
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return getContactsFromEmail(service, config);
		}
	}

	public Retorno<GoogleCredentialObject<ContactsService>> getContactsServiceByUserMail(String userEmail,
			Configuracao configuracao) {

		Retorno<GoogleCredentialObject<ContactsService>> retorno = new Retorno<GoogleCredentialObject<ContactsService>>();

		try {
			System.out.println("USERMAIL");
			AuthService authService = new AuthService(configuracao.getPathClientSecretJson());
			System.out.println(configuracao.getPathClientSecretJson());

			Retorno<GoogleCredential> credencialService = authService.getCrendencialByEmail(userEmail,
					Arrays.asList("htttps://www.google.com/m8/feeds/"));

			GoogleOAuthParameters params2 = new GoogleOAuthParameters();
			params2.setOAuthToken(credencialService.getObjeto().getAccessToken());
			ContactsService service = new ContactsService("IPNET Atualizador");
			Credential credential = ((Credential) credencialService.getObjeto());
			service.setOAuth2Credentials(credential);
			GoogleCredentialObject<ContactsService> objetc = new GoogleCredentialObject<ContactsService>();
			objetc.setCredential(credencialService.getObjeto());
			objetc.setObject(service);
			retorno.setObjeto(objetc);
			return retorno;

		} catch (NullPointerException e) {

			retorno.setError("Sem Conexão a Internet");

		} catch (Exception e) {
			try {
				e.printStackTrace();
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return getContactsServiceByUserMail(userEmail, configuracao);

		}
		return retorno;

	}

	public GoogleCredentialObject<ContactsService> refreshTokenContacts(
			GoogleCredentialObject<ContactsService> contacts, Configuracao configs) {
		try {
			contacts.getCredential().refreshToken();
			Credential credential = ((Credential) contacts.getCredential());
			contacts.getObject().setOAuth2Credentials(credential);
			return contacts;
		} catch (Exception e) {
			try {
				Thread.sleep(2000);

			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return refreshTokenContacts(contacts, configs);
		}
	}

	public void deleteContact(GoogleCredentialObject<ContactsService> service, ContactEntry contactEntry,
			Configuracao config) {
		String email = "Nulo";
		if (contactEntry.getEmailAddresses().size() > 0) {
			email = contactEntry.getEmailAddresses().get(0).getAddress();
			if (email.contains("@zonasul")) {
				try {
					ContactsDao contactsDao = new ContactsDao();
					contactsDao.deleteContact(contactEntry);
				} catch (Exception e) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					deleteContact(service, contactEntry, config);
				}
			}
		}

	}

	public Retorno<ContactEntry> createContact(GoogleCredentialObject<ContactsService> service,
			ContactEntry contactEntry, Configuracao config) {
		Retorno<ContactEntry> retorno = new Retorno<ContactEntry>();
		try {
			ContactsDao contactsDao = new ContactsDao();
			ContactEntry contactCreated = contactsDao.createContact(service.getObject(), contactEntry);

			retorno.setObjeto(contactCreated);
			return retorno;

		} catch (Exception e) {
			try {
				Thread.sleep(2000);
				if (e.getMessage() != null && e.getMessage().contains("403 Forbidden")) {
					service = refreshTokenContacts(service, config);
				}

			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return createContact(service, contactEntry, config);
		}
	}

	public void loadAndMapCsv(final File file, GlobalLog log, ContactsService service, ContactFeed feed) {
		try {

			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), "UTF-16"), ',', '\'', 1);
			URL postUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
			String[] nextLine = null;
			final String NO_YOMI = null;

			log.insertLog("SETTING COLUMN ORDER", ActionStatus.INFO, null);

			HashMap<Integer, Integer> columns = setColumnOrder(nextLine, /* 00 */ "Name", /* 01 */ "Given Name",
					/* 02 */ "Additional Name", /* 03 */ "Family Name", /* 04 */ "Yomi Name",
					/* 05 */ "G i v e n   N a m e   Y o m i ", /* 06 */ "A d d i t i o n a l   N a m e   Y o m i  ",
					/* 07 */ " F a m i l y   N a m e   Y o m i  ", /* 08 */ " N a m e   P r e f i x   ",
					/* 09 */ "N a m e   S u f f i x", /* 10 */ " I n i t i a l s ", /* 11 */ "  N i c k n a m e  ",
					/* 12 */ "S h o r t   N a m e ", /* 13 */ "M a i d e n   N a m e  ", /* 14 */ " B i r t h d a y   ",
					/* 15 */ "  G e n d e r ", /* 16 */ "   L o c a t i o n  ",
					/* 17 */ "   B i l l i n g   I n f o r m a t i o n   ",
					/* 18 */ " D i r e c t o r y   S e r v e r ", /* 19 */ "  M i l e a g e  ",
					/* 20 */ "   O c c u p a t i o n   ", /* 21 */ "    H o b b y ",
					/* 22 */ " S e n s i t i v i t y  ", /* 23 */ "  P r i o r i t y ", /* 24 */ " S u b j e c t ",
					/* 25 */ "  N o t e s  ", /* 26 */ "  G r o u p   M e m b e r s h i p",
					/* 27 */ "   E - m a i l   1   -   T y p e", /* 28 */ "   E - m a i l   1   -   V a l u e ",
					/* 29 */ "P h o n e   1   -   T y p e ", /* 30 */ "   P h o n e   1   -   V a l u e",
					/* 31 */ "W e b s i t e   1   -   T y p e ", /* 32 */ "   W e b s i t e   1   -   V a l u e");

			while ((nextLine = reader.readNext()) != null) {
				try {
					ContactEntry entry = new ContactEntry();
					Name name = new Name();
					name.setFullName(new FullName(nextLine[columns.get(0)], NO_YOMI));
					name.setGivenName(new GivenName(nextLine[columns.get(1)], NO_YOMI));
					name.setAdditionalName(new AdditionalName(nextLine[columns.get(2)], NO_YOMI));
					name.setFamilyName(new FamilyName(nextLine[columns.get(3)], NO_YOMI));
					name.setNamePrefix(new NamePrefix(nextLine[columns.get(8)]));
					name.setNameSuffix(new com.google.gdata.data.extensions.NameSuffix(nextLine[columns.get(9)]));
					entry.setName(name);
					entry.setInitials(new Initials(nextLine[columns.get(10)]));
					entry.setNickname(new Nickname(nextLine[columns.get(11)]));
					entry.setShortName(new ShortName(nextLine[columns.get(12)]));
					entry.setMaidenName(new MaidenName(nextLine[columns.get(13)]));
					entry.setBirthday(new Birthday(nextLine[columns.get(14)]));
					entry.setBillingInformation(new BillingInformation(nextLine[columns.get(17)]));
					entry.setDirectoryServer(new DirectoryServer(nextLine[columns.get(18)]));
					entry.setMileage(new Mileage(nextLine[columns.get(19)]));
					entry.setOccupation(new Occupation(nextLine[columns.get(20)]));
					entry.setSubject(new Subject(nextLine[columns.get(24)]));
					Email email = new Email();
					email.setAddress(nextLine[columns.get(28)]);
					email.setPrimary(true);
					entry.addEmailAddress(email);
					PhoneNumber phoneNumber = new PhoneNumber();
					phoneNumber.setPhoneNumber(nextLine[columns.get(30)]);
					phoneNumber.setPrimary(true);
					entry.addPhoneNumber(phoneNumber);

					// ADICIONAR ENTRY DEPOIS DE PARSEAR
					
					try {
						service.insert(postUrl, entry);
					} catch (Exception e) {
						log.insertLog("NÃO FOI POSSÍVEL INSERIR O CONTATO: " + entry.getName().getFullName(), ActionStatus.WARNING, e);
					}
				} catch (Exception e) {
					log.insertLog("NÃO FOI POSSÍVEL LER AS COLUNAS", ActionStatus.FATAL, e);
				}
			}
			reader.close();

		} catch (Exception e) {
			log.insertLog("ALGO OCORREU", ActionStatus.RETRYING, e);
			loadAndMapCsv(file, log, service, feed);
		}

	}

	private HashMap<Integer, Integer> setColumnOrder(String[] columns, String... columnName) {

		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		List<String> arrayColumns = Arrays.asList(columns);

		for (int i = 0; i < arrayColumns.size(); i++) {
			if (arrayColumns.get(i).equals(columnName))
				result.put(result.size(), i);
		}
		return result;
	}

	// METODOS DE AUTENTICAÇÂO
	public static void generateSecrets(String jsonPath) throws Exception {
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

	public static void stuff(JSONObject objJson) {
		private_key_id = (String) objJson.get("private_key_id");
		client_email = (String) objJson.get("client_email");
		private_key = (String) objJson.get("private_key");
		client_id = (String) objJson.get("client_id");
		type = (String) objJson.get("type");
	}

	private static String getPrivate_key() {
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
				.setServiceAccountId("contacts-150418@appspot.gserviceaccount.com")
				.setServiceAccountScopes(DIRECTORY_SCOPES).build();
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
				.setServiceAccountId("contacts-150418@appspot.gserviceaccount.com")
				.setServiceAccountScopes(CONTACTS_SCOPES).build();
		credential.refreshToken();
		credential.getAccessToken();
		System.out.println("Success");
		return credential;

	}

}
