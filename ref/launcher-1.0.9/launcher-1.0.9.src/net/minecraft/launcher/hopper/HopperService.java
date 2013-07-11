package net.minecraft.launcher.hopper;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URL;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;
import org.apache.commons.lang3.StringUtils;

public class HopperService
{
  private static final String BASE_URL = "http://hopper.minecraft.net/crashes/";
  private static final URL ROUTE_SUBMIT = LauncherConstants.constantURL("http://hopper.minecraft.net/crashes/submit_report/");
  private static final URL ROUTE_PUBLISH = LauncherConstants.constantURL("http://hopper.minecraft.net/crashes/publish_report/");

  private final Gson gson = new Gson();

  public SubmitResponse submitReport(String report, String version) throws IOException {
    SubmitRequest request = new SubmitRequest(report, version);

    return (SubmitResponse)makeRequest(ROUTE_SUBMIT, request, SubmitResponse.class);
  }

  public PublishResponse publishReport(Report report) throws IOException {
    PublishRequest request = new PublishRequest(report);

    return (PublishResponse)makeRequest(ROUTE_PUBLISH, request, PublishResponse.class);
  }

  protected <T extends Response> T makeRequest(URL url, Object input, Class<T> classOfT) throws IOException {
    String jsonResult = Http.performPost(url, this.gson.toJson(input), Launcher.getInstance().getProxy(), "application/json", true);
    Response result = (Response)this.gson.fromJson(jsonResult, classOfT);

    if (result == null) return null;

    if (StringUtils.isNotBlank(result.getError())) {
      throw new IOException(result.getError());
    }

    return result;
  }
}