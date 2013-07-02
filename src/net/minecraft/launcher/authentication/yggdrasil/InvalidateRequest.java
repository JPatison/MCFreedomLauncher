package net.minecraft.launcher.authentication.yggdrasil;

public class InvalidateRequest {
    private String accessToken;
    private String clientToken;

    public InvalidateRequest(YggdrasilAuthenticationService authenticationService) {
        this.accessToken = authenticationService.getAccessToken();
        this.clientToken = authenticationService.getClientToken();
    }
}


