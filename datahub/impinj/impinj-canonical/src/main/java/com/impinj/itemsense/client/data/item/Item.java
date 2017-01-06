package com.impinj.itemsense.client.data.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.impinj.itemsense.client.data.PresenceConfidence;

import java.time.ZonedDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {

    private String epc;
    private String tagId;

    //Since these are single lettered, variables Jackson is getting confused by the getter/setters created by lombok such as 'getXLocation'
    // Jackson is then trying to render these as xlocaton, ylocation, etc. Therefore the json property annotation is required for these fields.

    @JsonProperty("xLocation")
    private double xLocation;
    @JsonProperty("yLocation")
    private double yLocation;
    @JsonProperty("zLocation")
    private double zLocation;
    private String zone;
    private String floor;
    private String facility;
    @Deprecated
    private PresenceConfidence presenceConfidence;

    private ZonedDateTime lastModifiedTime;

    public static Item fromMap(final Map map) {
System.out.println ("Item.fromMap( "  + map + " )");
        final Item item = new Item();
        item.setLastModifiedTime(ZonedDateTime.parse((String) map.get("lastModifiedTime")));
        item.setEpc((String) map.get("epc"));
        item.setPresenceConfidence(PresenceConfidence.valueOf((String) map.get("presenceConfidence")));
        if (map.get("xLocation") != null ) item.setXLocation((Double) map.get("xLocation"));
        if (map.get("yLocation") != null ) item.setYLocation((Double) map.get("yLocation"));
        if (map.get("zLocation") != null ) item.setZLocation((Double) map.get("zLocation"));
        item.setZone((String) map.get("zone"));
        item.setTagId((String) map.get("tagId"));
        item.setFloor((String) map.get("floor"));
        item.setFacility((String) map.get("facility"));

        return item;
  }

  public String getEpc() {
    if (null == epc) {
      return null;
    }
    final String[] tokens = epc.split(".");
    if (tokens.length < 2) {
      // it looks like epc can be either raw URI or just the hex string depending on where it came
      // from.
      return epc;
    }
    return tokens[1];
  }

}
