package com.quylaptrinh.jbplugin.pywsl.exception;

public class DistroNotLoadedException extends Throwable {

  public DistroNotLoadedException() {
    super("Wsl Distro not selected or loaded successfully");
  }
}
