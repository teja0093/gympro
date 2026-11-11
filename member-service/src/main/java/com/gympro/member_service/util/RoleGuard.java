package com.gympro.member_service.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RoleGuard {

    public static void requireRole(String actualRole, String... allowed) {
        for (String r : allowed) {
            if (r.equalsIgnoreCase(actualRole)) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission");
    }
}