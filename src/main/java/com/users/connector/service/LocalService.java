package com.users.connector.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocalService {
    public Map<String, List<String>> getServerMachineUsersGroups() throws Exception {
        List<String> groups = getLocalGroups();
        Runtime rt = Runtime.getRuntime();
        BufferedReader br;
        String line;
        Map<String, List<String>> userGroups = new HashMap<>();
        for(String group : groups){
            Process process = rt.exec("net localgroup "+group);
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            boolean startCollecting = false;
            while ((line = br.readLine()) != null) {
                if(startCollecting && !line.contains("completed successfully") && !line.trim().isEmpty())
                    if(userGroups.containsKey(line)){
                        userGroups.get(line).add(group);
                    } else {
                        List<String> gs = new ArrayList<>();
                        gs.add(group);
                        userGroups.put(line, gs);
                    }
                if(line.contains("--------"))
                    startCollecting = true;
            }
        }
        return userGroups;
    }

    public List<String> getLocalGroups() throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process process = rt.exec("net localgroup");
        List<String> groups = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            if(line.startsWith("*"))
                groups.add(line.substring(1));
        }
        return groups;
    }
}
