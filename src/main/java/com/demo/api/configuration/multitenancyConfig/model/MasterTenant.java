
package com.demo.api.configuration.multitenancyConfig.model;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "master_tenant")
public class MasterTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Size(max = 30)
    @Column(name = "tenant_id")
    private String tenantId;

    @Size(max = 256)
    @Column(name = "url")
    private String url;

    @Size(max = 30)
    @Column(name = "username")
    private String username;

    /**
     * For simplicity, we are not storing an encrypted password. In production
     * this should be a encrypted password.
     */
    @Size(max = 30)
    @Column(name = "password")
    private String password;

    /**
     * Specifies the version field or property of an entity class that serves as
     * its optimistic lock value. The version is used to ensure integrity when
     * performing the merge operation and for optimistic concurrency control.
     */
    @Version
    private int version = 0;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the tenantId
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId
     *            the tenantId to set
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }
}