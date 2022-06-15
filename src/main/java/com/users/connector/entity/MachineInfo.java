package com.users.connector.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "machine_info")
public class MachineInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "machine_name", length = 100)
    private String machineName;

    @Column(name = "host", length = 50)
    private String host;

    @Column(name = "port", length = 6)
    private Integer port;

    @Column(name = "user_name", length = 250)
    private String username;

    @Column(name = "password", length = 150)
    private String password;

    @Column(name = "machine_type", length = 10)
    private String machineType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }
}
