package com.quylaptrinh.jbplugin.pywsl.exception;

public class WslNotRunningException extends Exception {

  private final String distroName;

  private static final String E_MESSAGE = "WSL distro not running";

  public WslNotRunningException(String distroName) {
    super(E_MESSAGE + ", distro name: " + distroName);
    this.distroName = distroName;
  }

  public String getDistroName() {
    return distroName;
  }
}
