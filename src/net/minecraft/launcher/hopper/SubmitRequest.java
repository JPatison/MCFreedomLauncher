package net.minecraft.launcher.hopper;

public class SubmitRequest
{
  private String report;
  private String version;

  public SubmitRequest(String report, String version)
  {
    this.report = report;
    this.version = version;
  }
}