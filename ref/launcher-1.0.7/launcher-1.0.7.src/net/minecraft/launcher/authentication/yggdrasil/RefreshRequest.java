package net.minecraft.launcher.authentication.yggdrasil;

import net.minecraft.launcher.authentication.GameProfile;

public class RefreshRequest
{
  private String clientToken;
  private String accessToken;
  private GameProfile selectedProfile;

  public RefreshRequest(YggdrasilAuthenticationService authenticationService)
  {
    this(authenticationService, null);
  }

  public RefreshRequest(YggdrasilAuthenticationService authenticationService, GameProfile profile) {
    this.clientToken = authenticationService.getClientToken();
    this.accessToken = authenticationService.getAccessToken();
    this.selectedProfile = profile;
  }
}