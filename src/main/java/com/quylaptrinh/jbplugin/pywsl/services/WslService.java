package com.quylaptrinh.jbplugin.pywsl.services;

import com.intellij.execution.wsl.WSLDistribution;
import com.intellij.execution.wsl.WslDistributionManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.quylaptrinh.jbplugin.pywsl.exception.NoDefaultWslDistException;
import com.quylaptrinh.jbplugin.pywsl.exception.WslNotRunningException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

@Service(Service.Level.PROJECT)
public final class WslService {

  /**
   * logger setup
   */
  static Logger log = DefaultLogger.getInstance(WslService.class);

  private static WslService wslDriver;
  private WSLDistribution systemWslDistribution;
  private boolean isWslLoaded = false;

  private WslService() {

  }

  public static WslService getInstance() {
    if (Objects.equals(wslDriver, null)) {
      wslDriver = new WslService();
    }
    return wslDriver;
  }


  public List<WSLDistribution> listDistributions() {
    return WslDistributionManager.getInstance().getInstalledDistributions();
  }

  /**
   * Pickup system installed wsl distribution
   *
   * @return WSLDistribution
   */
  private WSLDistribution wslDistPickup() {

    List<WSLDistribution> distributions = WslDistributionManager.getInstance()
        .getInstalledDistributions();

    if (distributions.isEmpty()) {
      throw new RuntimeException("No WSL distributions found!");
    }

    try {
      String dfDistName = this.getDefaultWslDistro();
    } catch (WslNotRunningException | RuntimeException e) {
      return null;
    }

//    return distributions.stream().anyMatch(wslDistribution -> {
//      wslDistribution.getId()
//    })
    return null;
  }

  /**
   * Pickup system default WSL distro and return its name as `String` if status is `running`
   *
   * @return name as `String`
   */
  private String getDefaultWslDistro() throws RuntimeException, WslNotRunningException {

    String defaultDistName = "";
    boolean hasDefaultDistro = false;
    boolean isDefaultDistRunning = false;

    try {
      String[] pArgs = {WSLDistribution.WSL_EXE, "-l", "-v"};
      Process process = new ProcessBuilder(pArgs).start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("*")) {  // The default distro is marked with '*'
          hasDefaultDistro = true;
          line = line.replace('*', ' ');
          line = line.trim(); // chim 3==D
          String[] distInfo = line.split("\\s++");
          int nameCol = 0;
          int stateCol = 1;
          //int verCol = 2;
          defaultDistName = distInfo[nameCol];
          if (distInfo[stateCol].equalsIgnoreCase("running")) {
            isDefaultDistRunning = true;
          }
          break;
        }
      }
      if (!hasDefaultDistro) {
        throw new NoDefaultWslDistException();
      }
      if (isDefaultDistRunning) {
        if (defaultDistName.isEmpty()) {
          String msg = "Could not get default distro name, result returned empty string";
          throw new RuntimeException(msg);
        }
      } else {
        log.warn("WARNING: Default WSL dist found but not running, "
            + "may need to config wsl distro manually");
        throw new WslNotRunningException(defaultDistName);
      }
    } catch (Exception e) {
      if (e instanceof WslNotRunningException) {
        throw (WslNotRunningException) e;
      }
      log.error("ERROR: Unexpected error while getting default wsl distro.", e);
    }
    return defaultDistName;
  }
}
