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
import com.google.gdata.util.ServiceException;
import com.opencsv.CSVReader;
import br.com.ipnetsolucoes.beans.ActionStatus;
import br.com.ipnetsolucoes.beans.Retorno;
import br.com.ipnetsolucoes.util.Configuracao;
import br.com.ipnetsolucoes.util.GlobalLog;

public class ContatosService {
	private static final String APPLICATION_NAME = "IPNET Contacts";

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private static HttpTransport HTTP_TRANSPORT;

	private static final List<String> DIRECTORY_SCOPES = Arrays.asList(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);

	private static final List<String> CONTACTS_SCOPES = Arrays.asList("https://www.google.com/m8/feeds/");

	@SuppressWarnings("unused")
	private static String private_key_id;

	private static String client_email;

	private static String private_key;

	@SuppressWarnings("unused")
	private static String client_id;

	@SuppressWarnings("unused")
	private static String type;

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * Adicionar logs Adicionar metodos para trabalhar com email do usuario
	 * Criar documentacao Refatorar Deletar Classes GGWP
	 */
	public ContatosService() {
	}

	public void ProcessAtualization(final Retorno<Configuracao> config, File csv) {
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

		// REALIZANDO A ATUALIZAÇÃO
		try {
			logs.insertLog("CRIANDO O DIRECTORY SERVICE", ActionStatus.INFO, null);
			Directory service = getDirectoryService();

			Users result = service.users().list().setCustomer("my_customer").setOrderBy("email").execute();

			List<User> users = result.getUsers();

			if (users == null || users.size() == 0) {
				logs.insertLog("NÃO HÁ USUÁRIOS NO DIRETÓRIO", ActionStatus.WARNING, null);
			} else {
				for (User user : users) {
					if (user.getName().containsValue("Sul")) {

						logs.insertLog("USUÁRIO: " + user.getName().getFullName() + " SENDO ATUALIZADO.",
								ActionStatus.INFO, null);

						// RECUPERANDO OS CONTATOS
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
																						// ------------------
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

							// ADICIONA OS NOVOS CONTATINHOS
							loadAndMapCsv(csv, logs, myService, resultFeed);
							logs.insertLog("De volta da inserção de contatos", ActionStatus.SUCCESS, null);

						} catch (Exception e) {
							logs.insertLog(
									"ERRO AO TENTAR RECUPERAR OS CONTATOS DO USUÁRIO: " + user.getName().getFullName(),
									ActionStatus.FATAL, e);
						}
					}
				}
			}
		} catch (Exception e) {
			logs.insertLog("ERRO AO PEGAR OS USUÁRIOS DO DIRETÓRIO", ActionStatus.FATAL, e);
		}
		// FINALIZANDO A MIGRAÇÃO
	}

	public void loadAndMapCsv(final File file, GlobalLog log, ContactsService service, ContactFeed feed)
			throws IOException, ServiceException {
		try {
			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), "UTF-16"), ',', '\'', 0);

			URL postUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
			String[] nextLine = reader.readNext();
			final String NO_YOMI = null;
			log.insertLog("SETTING COLUMN ORDER", ActionStatus.INFO, null);

			HashMap<Integer, Integer> columns = setColumnOrder(nextLine, /* 00 */ "Name", /* 01 */ "Given Name",
					/* 02 */ "Additional Name", /* 03 */ "Family Name", /* 04 */ "Yomi Name",
					/* 05 */ "Given Name Yomi", /* 06 */ "Additional Name Yomi", /* 07 */ "Family Name Yomi",
					/* 08 */ "Name Prefix", /* 09 */ "Name Suffix", /* 10 */ "Initials", /* 11 */ "Nickname",
					/* 12 */ "Short Name", /* 13 */ "Maiden Name", /* 14 */ "Birthday", /* 15 */ "Gender",
					/* 16 */ "Location", /* 17 */ "Billing Information", /* 18 */ "Directory Server",
					/* 19 */ "Mileage", /* 20 */ "Occupation", /* 21 */ "Hobby", /* 22 */ "Sensitivity",
					/* 23 */ "Priority", /* 24 */ "Subject", /* 25 */ "Notes", /* 26 */ "Group Membership",
					/* 27 */ "E-mail 1 - Type", /* 28 */ "E-mail 1 - Value", /* 29 */ "Phone 1 - Type",
					/* 30 */ "Phone 1 - Value", /* 31 */ "Website 1 - Type", /* 32 */ "Website 1 - Value");

			while ((nextLine = reader.readNext()) != null) {
				try {
					ContactEntry entry = new ContactEntry();
					Name name = new Name();
					if (!nextLine[columns.get(0)].isEmpty())
						name.setFullName(new FullName(nextLine[columns.get(0)], NO_YOMI));
					if (!nextLine[columns.get(1)].isEmpty())
						name.setGivenName(new GivenName(nextLine[columns.get(1)], NO_YOMI));
					if (!nextLine[columns.get(2)].isEmpty())
						name.setAdditionalName(new AdditionalName(nextLine[columns.get(2)], NO_YOMI));
					if (!nextLine[columns.get(3)].isEmpty())
						name.setFamilyName(new FamilyName(nextLine[columns.get(3)], NO_YOMI));
					if (!nextLine[columns.get(8)].isEmpty())
						name.setNamePrefix(new NamePrefix(nextLine[columns.get(8)]));
					if (!nextLine[columns.get(9)].isEmpty())
						name.setNameSuffix(new com.google.gdata.data.extensions.NameSuffix(nextLine[columns.get(9)]));
					entry.setName(name);
					if (!nextLine[columns.get(10)].isEmpty())
						entry.setInitials(new Initials(nextLine[columns.get(10)]));
					if (!nextLine[columns.get(11)].isEmpty())
						entry.setNickname(new Nickname(nextLine[columns.get(11)]));
					if (!nextLine[columns.get(12)].isEmpty())
						entry.setShortName(new ShortName(nextLine[columns.get(12)]));
					if (!nextLine[columns.get(13)].isEmpty())
						entry.setMaidenName(new MaidenName(nextLine[columns.get(13)]));
					if (!nextLine[columns.get(14)].isEmpty())
						entry.setBirthday(new Birthday(nextLine[columns.get(14)]));
					if (!nextLine[columns.get(17)].isEmpty())
						entry.setBillingInformation(new BillingInformation(nextLine[columns.get(17)]));
					if (!nextLine[columns.get(18)].isEmpty())
						entry.setDirectoryServer(new DirectoryServer(nextLine[columns.get(18)]));
					if (!nextLine[columns.get(19)].isEmpty())
						entry.setMileage(new Mileage(nextLine[columns.get(19)]));
					if (!nextLine[columns.get(20)].isEmpty())
						entry.setOccupation(new Occupation(nextLine[columns.get(20)]));
					if (!nextLine[columns.get(24)].isEmpty())
						entry.setSubject(new Subject(nextLine[columns.get(24)]));
					Email email = new Email();
					if (!nextLine[columns.get(28)].isEmpty()) {
						email.setAddress(nextLine[columns.get(28)]);
						email.setRel("http://schemas.google.com/g/2005#work");
						email.setPrimary(true);
						entry.addEmailAddress(email);
					}
					PhoneNumber phoneNumber = new PhoneNumber();
					if (!nextLine[columns.get(30)].isEmpty()) {
						phoneNumber.setPhoneNumber(nextLine[columns.get(30)]);
						phoneNumber.setPrimary(true);
						phoneNumber.setRel("http://schemas.google.com/g/2005#work");
						entry.addPhoneNumber(phoneNumber);
					}

					try {
						// ADICIONAR ENTRY DEPOIS DE PARSEAR
						service.insert(postUrl, entry);
						log.insertLog("CONTATO INSERIDO: " + entry.getName().getFullName(), ActionStatus.INFO, null);
					} catch (Exception e) {
						log.insertLog("NÃO FOI POSSÍVEL INSERIR O CONTATO: " + entry.getName().getFullName(),
								ActionStatus.WARNING, e);

					}
				} catch (Exception e) {
					log.insertLog("ERRO AO CRIAR A ENTRY", ActionStatus.FAILED, e);
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
		List<String> columnsNames = Arrays.asList(columnName);
		for (int i = 0; i < arrayColumns.size(); i++) {
			for (int j = 0; j < columnsNames.size(); j++) {
				if (arrayColumns.get(i).trim().equals(columnsNames.get(j))) {
					result.put(result.size(), i);
				}
			}
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
		try {
			generateSecrets("service_account.json");

			GoogleCredential credential = new GoogleCredential.Builder()
					.setServiceAccountPrivateKeyFromPemFile(getCredential()).setTransport(HTTP_TRANSPORT)
					.setJsonFactory(JSON_FACTORY).setServiceAccountUser("admin@demo.ipnetsolucoes.com.br")
					.setServiceAccountId(client_email).setServiceAccountScopes(DIRECTORY_SCOPES).build();
			return credential;
		} catch (Exception e) {
			e.printStackTrace();
			Thread.sleep(3000);
			return authorize();
		}
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
				.setServiceAccountUser("zonasul@demo.ipnetsolucoes.com.br").setServiceAccountId(client_email)
				.setServiceAccountScopes(CONTACTS_SCOPES).build();
		credential.refreshToken();
		credential.getAccessToken();
		System.out.println("Success");
		System.out.println(credential.toString());
		System.out.println(client_email);
		return credential;

	}

}
