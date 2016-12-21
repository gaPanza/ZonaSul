package br.com.ipnetsolucoes.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.*;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.ExtendedProperty;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.Name;
import com.google.api.services.admin.directory.Directory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.Certificate;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

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

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	public static Credential authorize() throws Exception {
		// Load client secrets.
		InputStream in = new FileInputStream("client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

	/**
	 * Build and return an authorized Admin SDK Directory client service.
	 * 
	 * @return an authorized Directory client service
	 * @throws Exception
	 */
	public static Directory getDirectoryService() throws Exception {
		Credential credential = authorize();
		return new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	public static Credential authorizeFeed() throws Exception {
		String pass = "notasecret";

		File file = new File("p12.p12");

		InputStream in = new FileInputStream(file);
		KeyStore store = KeyStore.getInstance("PKCS12");
		store.load(in, pass.toCharArray());

		PrivateKey key = (PrivateKey) store.getKey("privatekey", pass.toCharArray());

		GoogleCredential credential = new GoogleCredential.Builder().setServiceAccountPrivateKeyFromP12File(file)
				.setTransport(HTTP_TRANSPORT).setJsonFactory(JSON_FACTORY)
				.setServiceAccountUser("zonasul@demo.ipnetsolucoes.com.br")
				.setServiceAccountId("contacts-150418@appspot.gserviceaccount.com").setServiceAccountScopes(ESCOPOS)
				.build();
		credential.refreshToken();
		credential.getAccessToken();
		System.out.println("Sucess");
		return credential;

	}

	public static void main(String[] args) throws Exception {
		// Build a new authorized API client service.
		Directory service = getDirectoryService();

		// Retorna uma lista de 10 usuários do domínio sortados pelo email
		Users result = service.users().list().setCustomer("my_customer").setMaxResults(10).setOrderBy("email")
				.execute();

		List<User> users = result.getUsers();

		if (users == null || users.size() == 0) {
			System.out.println("No users found.");
		} else {
			System.out.println("Users:");
			for (User user : users) {
				if (user.getName().containsValue("Sul")) {

					// Request the feed
					URL feedUrl = new URL(
							"https://www.google.com/m8/feeds/contacts/" + user.getPrimaryEmail() + "/full");
					ContactsService myService = new ContactsService(APPLICATION_NAME);
					myService.setOAuth2Credentials(authorizeFeed());
					ContactFeed resultFeed = myService.getFeed(feedUrl, ContactFeed.class);

					// Print the results
					System.out.println(resultFeed.getTitle().getPlainText());
					for (ContactEntry entry : resultFeed.getEntries()) {
						entry.delete();
						System.out.println("Deletado");
						if (entry.hasName()) {
							Name name = entry.getName();
							if (name.hasFullName()) {
								String fullNameToDisplay = name.getFullName().getValue();
								if (name.getFullName().hasYomi()) {
									fullNameToDisplay += " (" + name.getFullName().getYomi() + ")";
								}
								System.out.println("\t\t" + fullNameToDisplay);
							} else {
								System.out.println("\t\t (no full name found)");
							}
							if (name.hasNamePrefix()) {
								System.out.println("\t\t" + name.getNamePrefix().getValue());
							} else {
								System.out.println("\t\t (no name prefix found)");
							}
							if (name.hasGivenName()) {
								String givenNameToDisplay = name.getGivenName().getValue();
								if (name.getGivenName().hasYomi()) {
									givenNameToDisplay += " (" + name.getGivenName().getYomi() + ")";
								}
								System.out.println("\t\t" + givenNameToDisplay);
							} else {
								System.out.println("\t\t (no given name found)");
							}
							if (name.hasAdditionalName()) {
								String additionalNameToDisplay = name.getAdditionalName().getValue();
								if (name.getAdditionalName().hasYomi()) {
									additionalNameToDisplay += " (" + name.getAdditionalName().getYomi() + ")";
								}
								System.out.println("\t\t" + additionalNameToDisplay);
							} else {
								System.out.println("\t\t (no additional name found)");
							}
							if (name.hasFamilyName()) {
								String familyNameToDisplay = name.getFamilyName().getValue();
								if (name.getFamilyName().hasYomi()) {
									familyNameToDisplay += " (" + name.getFamilyName().getYomi() + ")";
								}
								System.out.println("\t\t" + familyNameToDisplay);
							} else {
								System.out.println("\t\t (no family name found)");
							}
							if (name.hasNameSuffix()) {
								System.out.println("\t\t" + name.getNameSuffix().getValue());
							} else {
								System.out.println("\t\t (no name suffix found)");
							}
						} else {
							System.out.println("\t (no name found)");
						}
						System.out.println("Email addresses:");
						for (Email email : entry.getEmailAddresses()) {
							System.out.print(" " + email.getAddress());
							if (email.getRel() != null) {
								System.out.print(" rel:" + email.getRel());
							}
							if (email.getLabel() != null) {
								System.out.print(" label:" + email.getLabel());
							}
							if (email.getPrimary()) {
								System.out.print(" (primary) ");
							}
							System.out.print("\n");
						}
						System.out.println("IM addresses:");
						for (Im im : entry.getImAddresses()) {
							System.out.print(" " + im.getAddress());
							if (im.getLabel() != null) {
								System.out.print(" label:" + im.getLabel());
							}
							if (im.getRel() != null) {
								System.out.print(" rel:" + im.getRel());
							}
							if (im.getProtocol() != null) {
								System.out.print(" protocol:" + im.getProtocol());
							}
							if (im.getPrimary()) {
								System.out.print(" (primary) ");
							}
							System.out.print("\n");
						}
						System.out.println("Groups:");
						for (GroupMembershipInfo group : entry.getGroupMembershipInfos()) {
							String groupHref = group.getHref();
							System.out.println("  Id: " + groupHref);
						}
						System.out.println("Extended Properties:");
						for (ExtendedProperty property : entry.getExtendedProperties()) {
							if (property.getValue() != null) {
								System.out.println("  " + property.getName() + "(value) = " + property.getValue());
							} else if (property.getXmlBlob() != null) {
								System.out.println(
										"  " + property.getName() + "(xmlBlob)= " + property.getXmlBlob().getBlob());
							}
						}
						Link photoLink = entry.getContactPhotoLink();
						String photoLinkHref = photoLink.getHref();
						System.out.println("Photo Link: " + photoLinkHref);
						if (photoLink.getEtag() != null) {
							System.out.println("Contact Photo's ETag: " + photoLink.getEtag());
						}
						System.out.println("Contact's ETag: " + entry.getEtag());
					}

				}

				System.out.println(user.getName().getFullName());
			}
		}
	}

}
