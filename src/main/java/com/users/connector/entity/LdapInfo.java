package com.users.connector.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ldap_info")
public class LdapInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "connection_name", length = 100)
    private String connectionName;

    @Column(name = "ldap_url", length = 50)
    private String ldapUrl;

    @Column(name = "port", length = 6)
    private String port;

    @Column(name = "domain_name", length = 250)
    private String domainName;

    @Column(name = "password", length = 150)
    private String password;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
