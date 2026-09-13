package com.sarthak.ecommerce.product.util;

import com.sarthak.ecommerce.common.exception.ApiException;
import com.sarthak.ecommerce.product.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ProductUtil {

    public void verifyProductOwnership(Product product) {

        if (isAdmin()) {
            return;
        }

        String authenticatedUserId = getAuthenticatedUserId();

        if (!authenticatedUserId.equals(product.getSellerId())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to modify this product"
            );
        }
    }


    public String getAuthenticatedUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getName();
    }

    public boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals(ProductConstants.ROLE_ADMIN));
    }
}
