package com.quylaptrinh.jbplugin.pywsl.services;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.wsl.WSLDistribution;
import com.intellij.execution.wsl.WslDistributionManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Service.Level;
import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.quylaptrinh.jbplugin.pywsl.exception.DistroNotLoadedException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service(Level.PROJECT)
public final class WslService {

  /**
   * logger setup
   */
  static Logger log = DefaultLogger.getInstance(WslService.class);

  private static WslService wslDriver;
  private WSLDistribution selectedDistro;
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

    this.selectedDistro = this.getDefaultWslDistro().orElse(null);
    if (Objects.nonNull(this.selectedDistro)) {
      this.isWslLoaded = true;
    }
    return null;
  }

  /**
   * Pickup system default WSL distro and return its name as `String` if status is `running`
   *
   * @return name as `String`
   */
  public Optional<WSLDistribution> getDefaultWslDistro() {

    List<WSLDistribution> distributions = WslDistributionManager.getInstance()
        .getInstalledDistributions();

    try {
      String[] pArgs = {WSLDistribution.WSL_EXE, "-l", "-v"};
      Process process = new ProcessBuilder(pArgs).start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      String defaultDistName = UUID.randomUUID().toString();
      while ((line = reader.readLine()) != null) {

        if (line.startsWith("*")) {  // The default distro is marked with '*'
          line = line.replace('*', ' ');
          line = line.trim(); // chim 3==D
          String[] distInfo = line.split("\\s++");
          int nameCol = 0;
          //int verCol = 2;
          defaultDistName = distInfo[nameCol];
          break;
        }
      }

      for (WSLDistribution distribution : distributions) {
        if (Objects.equals(distribution.getId(), defaultDistName)) {
          return Optional.of(distribution);
        }
      }
    } catch (Exception e) {
      log.error("ERROR: Unexpected error while getting default wsl distro.", e);
    }
    return Optional.empty();
  }

  public boolean isWslLoaded() {
    return this.isWslLoaded;
  }

  public WSLDistribution getDistro() throws DistroNotLoadedException {
    if (!isWslLoaded) {
      throw new DistroNotLoadedException();
    }
    return this.selectedDistro;
  }

  public void setDistro(WSLDistribution dist) {
    this.selectedDistro = dist;
    if (this.isUsable()) {
      this.isWslLoaded = true;
    } else {
      this.selectedDistro = null;
      this.isWslLoaded = false;
    }
  }

  public boolean isUsable() {
    if (Objects.isNull(this.selectedDistro)) {
      return false;
    }
    try {
      // Check if the distribution is installed by attempting to get its version
      int version = this.selectedDistro.getVersion();
      if (version <= 0) {
        return false;
      }

      // Check if a simple command can be executed successfully
      ProcessOutput output = this.selectedDistro.executeOnWsl(5000, "echo", "test");
      return output.getExitCode() == 0;
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
