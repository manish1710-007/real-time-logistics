package com.logistics.identity.application.service;

import com.logistics.identity.application.command.AuthenticateUserCommand;

public interface AuthenticationService {
    AuthenticationResult authenticate(AuthenticateUserCommand command);
}
