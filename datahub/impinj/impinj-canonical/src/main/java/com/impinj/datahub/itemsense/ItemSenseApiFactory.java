package com.impinj.datahub.itemsense;
import com.google.gson.Gson;
import com.impinj.itemsense.client.ItemApiLib;
import com.impinj.itemsense.client.ControlApiLib;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;



import java.net.URI;


// Factory Created in order to facilitate multiple ItemSense API Lib endpoints
public class ItemSenseApiFactory {


    private ItemSenseApiFactory(){

    }

    //Creates an ItemApiLib based on an ItemSenseConfiguration
    public static ItemApiLib getItemApiLib(ItemSenseConfiguration itemSenseConfiguration){

        Client client= Client.create();
        client.addFilter( new HTTPBasicAuthFilter(itemSenseConfiguration.getUserName(), itemSenseConfiguration.getPassword()));

        ItemApiLib itemApiLib = new ItemApiLib(new Gson(), client, URI.create(itemSenseConfiguration.getBaseUrl()));

        return itemApiLib;
    }
    /**
    //
    //Creates an ItemApiLib based on an ItemSenseConfiguration
    */
    public static ControlApiLib getControlApiLib(ItemSenseConfiguration itemSenseConfiguration){

        Client client= Client.create();
        client.addFilter( new HTTPBasicAuthFilter(itemSenseConfiguration.getUserName(), itemSenseConfiguration.getPassword()));

        ControlApiLib controlApiLib = new ControlApiLib(new Gson(), client, URI.create(itemSenseConfiguration.getBaseUrl()));

        return controlApiLib;
    }
}
