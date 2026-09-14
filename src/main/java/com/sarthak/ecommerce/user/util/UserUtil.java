package com.sarthak.ecommerce.user.util;

import com.sarthak.ecommerce.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserUtil {
    public String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(UserConstants.ADMIN_ROLE));
    }

    public void verifyUserOwnership(String userId) {
        if (isAdmin()) {return;}
        String authenticatedUserId = getAuthenticatedUserId();
        if(!authenticatedUserId.equals(userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    UserConstants.FORBIDDEN_ERROR_MSG
            );
        }

    }
}
