package com.quylaptrinh.jbplugin.pywsl.exception;

public class NoDefaultWslDistException extends Exception {

  private static final String E_MESSAGE = "No default Distro found, you may have to setup manually";

  public NoDefaultWslDistException(String message) {
    super(message);
  }

  public NoDefaultWslDistException() {
    super(E_MESSAGE);
  }

}
