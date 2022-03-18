package org.candlepin.subscriptions.security.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.candlepin.subscriptions.security.RoleProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * A security annotation ensuring that the user must have the internal role in order to
 * execute the method.
 *
 * <p>Requires the ROLE_INTERNAL role
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('" + RoleProvider.ROLE_INTERNAL + "')")
public @interface InternalRoleRequired {}