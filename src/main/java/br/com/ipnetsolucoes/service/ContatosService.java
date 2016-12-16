package br.com.ipnetsolucoes.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.AdditionalName;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.Name;
import com.opencsv.CSVReader;

import br.com.ipnetsolucoes.beans.GoogleCredentialObject;
import br.com.ipnetsolucoes.beans.Retorno;
import br.com.ipnetsolucoes.dao.ContactsDao;
import br.com.ipnetsolucoes.util.Configuracao;

public class ContatosService {

	public ContatosService() {
	}

	public void ProcessAtualization(final Retorno<Configuracao> config, String userMail, File csv) {
		// INICIO DA ATUALIZAÇÃO
		System.out.println("ATUALIZANDO");
		// PEGANDO OS CONTATOS DO DESTINO

		Retorno<GoogleCredentialObject<ContactsService>> destinyContacts = getContactsServiceByUserMail(userMail,
				config.getObjeto());

		//Retorno<ContactFeed> allContactsDestino = ContatosService.this.getContactsFromEmail(destinyContacts.getObjeto(),
			//	config.getObjeto());

		// DELETANDO OS CONTATOS @ZONASUL

		// if (allContactsDestino.getObjeto().getEntries().size() > 0) {
		// for (ContactEntry contactEntryOrigem :
		// allContactsDestino.getObjeto().getEntries()) {
		// if (contactEntryOrigem.getId() != null &&
		// contactEntryOrigem.getCanEdit()) {
		// List<Email> x = contactEntryOrigem.getEmailAddresses();
		// for (int i = 0; i < x.size(); i++) {
		// if (x.get(i).getAddress().contains("@zonasul")) {
		// deleteContact(destinyContacts.getObjeto(), contactEntryOrigem,
		// config.getObjeto());
		// i = x.size() - 1;
		// }
		// }
		// }
		// }
		// }

		// PEGANDO CRIANDO E PERSISTINDO OS CONTATOS DO CSV

		loadAndMapCsv(destinyContacts.getObjeto(), destinyContacts.getObjeto().getObject(), config.getObjeto(), csv);

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

	public void loadAndMapCsv(GoogleCredentialObject<ContactsService> service2, final ContactsService service,
			Configuracao config, final File file) {
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						System.out.println("LENDO CSV");
						CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), "UTF-16"),
								',', '\'', 1);

						String[] nextLine;

						final String NO_YOMI = null;

						ContactEntry entry = new ContactEntry();

						ContactsDao dao = new ContactsDao();

						while ((nextLine = reader.readNext()) != null) {

							// PARA CADA LINHA DO CSV
							System.out.println(nextLine[0]);
							// DEFINE O NOME
							Name name = new Name();
							name.setFullName(new FullName(nextLine[0], NO_YOMI));
							name.setGivenName(new GivenName(nextLine[1], NO_YOMI));
							name.setAdditionalName(new AdditionalName(nextLine[2], NO_YOMI));
							name.setFamilyName(new FamilyName(nextLine[3], NO_YOMI));
							entry.setName(name);
							// DEFINE O EMAIL
							Email email = new Email();
							email.setAddress(nextLine[28]);
							email.setPrimary(true);
							entry.addEmailAddress(email);
							// FAZ O INSERT
							dao.createContact(service, entry);

						}
						reader.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			try {
				Thread.sleep(2000);
				if (e.getMessage() != null && e.getMessage().contains("403 Forbidden")) {
					service2 = refreshTokenContacts(service2, config);
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
}
