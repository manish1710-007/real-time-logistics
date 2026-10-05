package com.logistics.identity.application.service;

import com.logistics.identity.domain.entity.Permission;
import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.UserId;
import java.util.List;

public interface AccessTokenIssuer {

    IssuedAccessToken issue(UserId userId, TenantId tenantId, List<Role> roles, List<Permission> permissions);
}
