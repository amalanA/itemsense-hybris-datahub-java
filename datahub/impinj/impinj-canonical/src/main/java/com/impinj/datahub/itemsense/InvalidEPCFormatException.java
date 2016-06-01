package com.impinj.datahub.itemsense;

/**
 * Created by jcombopi on 1/12/16.
 */
public class InvalidEPCFormatException extends RuntimeException{

    public InvalidEPCFormatException () {

    }

    public InvalidEPCFormatException (String message) {
        super (message);
    }

    public InvalidEPCFormatException (Throwable cause) {
        super (cause);
    }

    public InvalidEPCFormatException (String message, Throwable cause) {
        super (message, cause);
    }
}
