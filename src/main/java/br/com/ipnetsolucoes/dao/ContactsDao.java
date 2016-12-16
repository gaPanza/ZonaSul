package br.com.ipnetsolucoes.dao;

import java.net.URL;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;

public class ContactsDao {
	
	private String URLCONTACTS = "https://www.google.com/m8/feeds/contacts/default/full?max-results=2147483647";
	
	
	public ContactFeed  getAllContactsByCredential(ContactsService service) throws Exception{
		URL feedUrl = new URL(URLCONTACTS);
		Query myQuery = new Query(feedUrl);
	    return service.query(myQuery, ContactFeed.class);
	    
	}
	
	public void deleteContact(ContactEntry contactEntry) throws Exception{
		contactEntry.delete();
	}
	
	public ContactEntry createContact(ContactsService service, ContactEntry entry)throws Exception{
		URL postURL = new URL(URLCONTACTS);
		entry.getEtag();
		
		return service.insert(postURL, entry);
	}
	
}
