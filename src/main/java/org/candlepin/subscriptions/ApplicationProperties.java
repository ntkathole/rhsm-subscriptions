/*
 * Copyright (c) 2009 - 2019 Red Hat, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Red Hat trademarks are not licensed under GPLv3. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.candlepin.subscriptions;

import org.candlepin.subscriptions.retention.TallyRetentionPolicyProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * POJO to hold property values via Spring's "Type-Safe Configuration Properties" pattern
 */
@ConfigurationProperties(prefix = "rhsm-subscriptions")
public class ApplicationProperties {

    private boolean prettyPrintJson = false;

    private boolean devMode = false;

    private final TallyRetentionPolicyProperties tallyRetentionPolicy = new TallyRetentionPolicyProperties();

    /**
     * Resource location of a file containing a mapping of product IDs to product IDs that identify them.
     */
    private String productIdToProductsMapResourceLocation;

    /**
     * Resource location of a file containing a mapping of syspurpose roles to products.
     */
    private String roleToProductsMapResourceLocation;

    /**
     * Resource location of a file containing a list of accounts to process.
     */
    private String accountListResourceLocation;

    /**
     * Resource location of a file containing a list of products (SKUs) to process. If not specified, all
     * products will be processed.
     */
    private String productWhitelistResourceLocation;

    /**
     * Resource location of a file containing the whitelisted accounts allowed to run reports.
     */
    private String reportingAccountWhitelistResourceLocation;

    /**
     * An hour based threshold used to determine whether an inventory host record's rhsm facts are outdated.
     * The host's rhsm.SYNC_TIMESTAMP fact is checked against this threshold. The default is 24 hours.
     */
    private int hostLastSyncThresholdHours = 24;

    /**
     * The batch size of account numbers that will be processed at a time while producing snapshots.
     * Default: 500
     */
    private int accountBatchSize = 500;

    /**
     * Whether the ingress endpoint is enabled on this instance of rhsm-subscriptions or not.
     */
    private boolean enableIngressEndpoint;

    /**
     * Amount of time to cache the account list, before allowing a re-read from the filesystem.
     */
    private Duration accountListCacheTtl = Duration.ofMinutes(5);

    /**
     * Amount of time to cache the product mapping, before allowing a re-read from the filesystem.
     */
    private Duration productIdToProductsMapCacheTtl = Duration.ofMinutes(5);

    /**
     * Amount of time to cache the product whitelist, before allowing a re-read from the filesystem.
     */
    private Duration productWhiteListCacheTtl = Duration.ofMinutes(5);

    /**
     * Amount of time to cache the syspurpose role to products map, before allowing a re-read from the
     * filesystem.
     */
    private Duration roleToProductsMapCacheTtl = Duration.ofMinutes(5);

    /**
     * Amount of time to cache the API access whitelist, before allowing a re-read from the filesystem.
     */
    private Duration reportingAccountWhitelistCacheTtl = Duration.ofMinutes(5);

    public boolean isPrettyPrintJson() {
        return prettyPrintJson;
    }

    public void setPrettyPrintJson(boolean prettyPrintJson) {
        this.prettyPrintJson = prettyPrintJson;
    }

    public String getProductIdToProductsMapResourceLocation() {
        return productIdToProductsMapResourceLocation;
    }

    public void setProductIdToProductsMapResourceLocation(String productIdToProductsMapResourceLocation) {
        this.productIdToProductsMapResourceLocation = productIdToProductsMapResourceLocation;
    }

    public String getRoleToProductsMapResourceLocation() {
        return roleToProductsMapResourceLocation;
    }

    public void setRoleToProductsMapResourceLocation(String roleToProductsMapResourceLocation) {
        this.roleToProductsMapResourceLocation = roleToProductsMapResourceLocation;
    }

    public TallyRetentionPolicyProperties getTallyRetentionPolicy() {
        return tallyRetentionPolicy;
    }

    public String getAccountListResourceLocation() {
        return accountListResourceLocation;
    }

    public void setAccountListResourceLocation(String accountListResourceLocation) {
        this.accountListResourceLocation = accountListResourceLocation;
    }

    public int getHostLastSyncThresholdHours() {
        return hostLastSyncThresholdHours;
    }

    public void setHostLastSyncThresholdHours(int hostLastSyncThresholdHours) {
        this.hostLastSyncThresholdHours = hostLastSyncThresholdHours;
    }

    public int getAccountBatchSize() {
        return this.accountBatchSize;
    }

    public void setAccountBatchSize(int accountBatchSize) {
        this.accountBatchSize = accountBatchSize;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public boolean isEnableIngressEndpoint() {
        return enableIngressEndpoint;
    }

    public void setEnableIngressEndpoint(boolean enableIngressEndpoint) {
        this.enableIngressEndpoint = enableIngressEndpoint;
    }

    public String getProductWhitelistResourceLocation() {
        return productWhitelistResourceLocation;
    }

    public void setProductWhitelistResourceLocation(String productWhitelistResourceLocation) {
        this.productWhitelistResourceLocation = productWhitelistResourceLocation;
    }

    public String getReportingAccountWhitelistResourceLocation() {
        return reportingAccountWhitelistResourceLocation;
    }

    public void setReportingAccountWhitelistResourceLocation(String location) {
        this.reportingAccountWhitelistResourceLocation = location;
    }

    public Duration getAccountListCacheTtl() {
        return accountListCacheTtl;
    }

    public void setAccountListCacheTtl(Duration accountListCacheTtl) {
        this.accountListCacheTtl = accountListCacheTtl;
    }

    public Duration getProductIdToProductsMapCacheTtl() {
        return productIdToProductsMapCacheTtl;
    }

    public void setProductIdToProductsMapCacheTtl(Duration productIdToProductsMapCacheTtl) {
        this.productIdToProductsMapCacheTtl = productIdToProductsMapCacheTtl;
    }

    public Duration getProductWhiteListCacheTtl() {
        return productWhiteListCacheTtl;
    }

    public void setProductWhiteListCacheTtl(Duration productWhiteListCacheTtl) {
        this.productWhiteListCacheTtl = productWhiteListCacheTtl;
    }

    public Duration getRoleToProductsMapCacheTtl() {
        return roleToProductsMapCacheTtl;
    }

    public void setRoleToProductsMapCacheTtl(Duration roleToProductsMapCacheTtl) {
        this.roleToProductsMapCacheTtl = roleToProductsMapCacheTtl;
    }

    public Duration getReportingAccountWhitelistCacheTtl() {
        return reportingAccountWhitelistCacheTtl;
    }

    public void setReportingAccountWhitelistCacheTtl(Duration reportingAccountWhitelistCacheTtl) {
        this.reportingAccountWhitelistCacheTtl = reportingAccountWhitelistCacheTtl;
    }
}
