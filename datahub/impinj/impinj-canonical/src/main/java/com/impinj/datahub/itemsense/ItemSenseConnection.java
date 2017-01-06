package com.impinj.datahub.itemsense;

import java.net.URI;

//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.ClientBuilder;

//import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.impinj.itemsense.client.coordinator.CoordinatorApiController;
//import com.impinj.itemsense.client.data.DataApiController;

import com.google.gson.Gson;
import com.impinj.itemsense.client.data.ItemApiLib;
import com.impinj.itemsense.client.coordinator.ControlApiLib;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;


/**
 * Created by jtieman on 12/15/16.
 */
public class ItemSenseConnection {
	private String baseUrl;
	private String username;
	private String password;

	private Boolean valid = false;
	//private DataApiController dataController;
	//private CoordinatorApiController coordinatorController;
	private Client client;

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemSenseConnection.class.getName());

	// not called
	 private ItemSenseConnection() {
		super();
	}

	public ItemSenseConnection (String baseUrl, String username, String password ) {
		this.baseUrl = baseUrl;
		this.username = username;
		this.password = password;
	}

	/** 
         * old style - com.sun.java - 
         */
        private Client createClient () {
            Client client= Client.create();
            client.addFilter( new HTTPBasicAuthFilter(getUsername(), getPassword()));

            return client;
        }
 
        public ControlApiLib getControlApiLib() {
            return new ControlApiLib(new Gson(), createClient(), URI.create(getBaseUrl()));
        }

        public ItemApiLib getItemApiLib() {
            return new ItemApiLib(new Gson(), createClient(), URI.create(getBaseUrl()));
        }

//	public DataApiController getDataController() {
//
//		if (dataController == null) {
//			dataController = new DataApiController(getClient(), URI.create(getBaseUrl()));
//		}
//		return dataController;
//	}

//	public CoordinatorApiController getCoordinatorController() {
//		if (coordinatorController == null) {
//			coordinatorController = new CoordinatorApiController(getClient(),
//					URI.create(getBaseUrl()));
//		}
//
//		return coordinatorController;
//	}

//	public Client getClient() {
//		// lazy instantiation
//		if (client == null) {
//			client = ClientBuilder.newClient().register(HttpAuthenticationFeature
//					.basic(getUsername(), getPassword()));
//		}
//
//		return client;
//	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	@Override
	public String toString() {
		return "ItemSenseConnection [username=" + username + ", password=" + password + ", baseUrl=" + baseUrl + "]";
	}
}
