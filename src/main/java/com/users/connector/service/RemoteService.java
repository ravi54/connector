package com.users.connector.service;

import com.users.connector.entity.MachineInfo;
import com.users.connector.repository.MachineInfoRepo;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RemoteService {
    @Autowired
    private MachineInfoRepo machineInfoRepo;
    private long defaultTimeoutSeconds = 3000;

    public Map<String, List<String>> getRemoteUsersGroups(Integer machineId) throws Exception {
        Map<String, List<String>> userGroups = new HashMap<>();
        Optional<MachineInfo> machineInfoOpt = machineInfoRepo.findById(machineId);
        if(machineInfoOpt.isPresent()) {
            MachineInfo machineInfo = machineInfoOpt.get();
            SshClient client = SshClient.setUpDefaultClient();
            client.start();
            try (ClientSession session = client.connect(machineInfo.getUsername(), machineInfo.getHost(), machineInfo.getPort())
                    .verify(defaultTimeoutSeconds, TimeUnit.SECONDS).getSession()) {
                session.addPasswordIdentity(machineInfo.getPassword());
                boolean isConnected = session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS).isSuccess();
                if (isConnected) {
                    if(machineInfo.getMachineType().equalsIgnoreCase("linux"))
                        processUserGroupsForLinux(userGroups, session);
                    else if(machineInfo.getMachineType().equalsIgnoreCase("windows"))
                        processUserGroupsForWindows(userGroups, session);
                }
            } finally {
                client.stop();
            }
        }
        return userGroups;
    }

    private void processUserGroupsForWindows(Map<String, List<String>> userGroups, ClientSession session) throws Exception {
        List<String> groups = getGroupsFromWindows(session);
        for(String group : groups) {
             getGroupUsersFromWindows(session, group, userGroups);
        }
    }

    private static List<String> getGroupUsersFromWindows(ClientSession session, String group, Map<String, List<String>> userGroups) throws Exception {
        List<String> users = new ArrayList<>();
        ClientChannel execChannel = session.createChannel(ClientChannel.CHANNEL_EXEC, "net localgroup \""+group+"\"");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        execChannel.setOut(out);
        execChannel.setErr(err);
        execChannel.open().await(1, TimeUnit.SECONDS);
        execChannel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 2000);
        boolean startCollecting = false;
        String[] result = new String(out.toByteArray(), StandardCharsets.UTF_8).split("\n");
        for(String name : result){
            String r = name.trim();
            if(startCollecting && !r.contains("completed successfully") && !r.trim().isEmpty())
                if(userGroups.containsKey(r)){
                    userGroups.get(r).add(group);
                } else {
                    List<String> gs = new ArrayList<>();
                    gs.add(group);
                    userGroups.put(r, gs);
                }
            if(r.contains("--------"))
                startCollecting = true;
        }
        String error = new String(err.toByteArray(), StandardCharsets.UTF_8);
        if(error != null && !error.isEmpty())
            throw new Exception("Error while exccuting command");
        return users;
    }

    private static List<String> getGroupsFromWindows(ClientSession session) throws Exception {
        List<String> groups = new ArrayList<>();
        ClientChannel execChannel = session.createChannel(ClientChannel.CHANNEL_EXEC, "net localgroup");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        execChannel.setOut(out);
        execChannel.setErr(err);
        execChannel.open().await(1, TimeUnit.SECONDS);
        execChannel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 2000);
        String[] result = new String(out.toByteArray(), StandardCharsets.UTF_8).split("\n");
        for(String r : result){
            if(r.startsWith("*"))
                groups.add(r.substring(1).trim());
        }
        String error = new String(err.toByteArray(), StandardCharsets.UTF_8);
        if(error != null && !error.isEmpty())
            throw new Exception("Error while exccuting command");
        return groups;
    }

    private void processUserGroupsForLinux(Map<String, List<String>> userGroups, ClientSession session) throws Exception {
        List<String> users = getUserNamesFromLinux(session);
        for(String user : users) {
            userGroups.put(user, getUserGroupFromLinux(session, user));
        }
    }


    private static List<String> getUserGroupFromLinux(ClientSession session, String userName) throws Exception {
        List<String> users = new ArrayList<>();
        ClientChannel execChannel = session.createChannel(ClientChannel.CHANNEL_EXEC, "groups "+userName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        execChannel.setOut(out);
        execChannel.setErr(err);
        execChannel.open().await(1, TimeUnit.SECONDS);
        execChannel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 2000);
        String[] result = new String(out.toByteArray(), StandardCharsets.UTF_8).split("\n");
        for(String r : result){
            if(StringUtils.hasText(r)) {
                r = r.replaceAll(userName, "");
                if(r.contains("adm"))
                    users.add("Admin");
                else
                    users.add("Standard User");
            }
        }
        String error = new String(err.toByteArray(), StandardCharsets.UTF_8);
        if(error != null && !error.isEmpty())
            throw new Exception("Error while exccuting command");
        return users;
    }

    private static List<String> getUserNamesFromLinux(ClientSession session) throws Exception {
        List<String> users = new ArrayList<>();
        ClientChannel execChannel = session.createChannel(ClientChannel.CHANNEL_EXEC, "getent passwd | grep /home | grep /bin/bash");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        execChannel.setOut(out);
        execChannel.setErr(err);
        execChannel.open().await(1, TimeUnit.SECONDS);
        execChannel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 2000);
        String[] result = new String(out.toByteArray(), StandardCharsets.UTF_8).split("\n");
        for(String r : result){
            if(r.contains(":"))
                users.add(r.split(":")[0]);
        }
        String error = new String(err.toByteArray(), StandardCharsets.UTF_8);
        if(error != null && !error.isEmpty())
            throw new Exception("Error while exccuting command");
        return users;
    }

    public List<MachineInfo> getRemoteMachinesList() {
        List<MachineInfo> machineInfos = new ArrayList<>();
        machineInfoRepo.findAll().forEach(machineInfos::add);
        return machineInfos;
    }
}
