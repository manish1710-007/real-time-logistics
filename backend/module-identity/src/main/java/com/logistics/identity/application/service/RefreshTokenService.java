package com.logistics.identity.application.service;

import com.logistics.identity.application.command.RefreshTokenCommand;
import com.logistics.identity.application.dto.RefreshTokenResult;

public interface RefreshTokenService {

    RefreshTokenResult refreshToken(RefreshTokenCommand command);
}
