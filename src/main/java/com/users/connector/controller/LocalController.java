package com.users.connector.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.users.connector.service.LocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class LocalController {

    @Autowired
    private LocalService localService;


    @GetMapping("/get-server-machine-users-groups")
    public ResponseEntity<ObjectNode> getServerMachineUsersGroups() throws Exception {
        Map<String, List<String>> usersGroups = localService.getServerMachineUsersGroups();
        return new ResponseEntity(usersGroups, HttpStatus.OK);
    }


    @GetMapping("/get-server-machine-groups")
    public ResponseEntity<ObjectNode> getServerMachineGroups() throws Exception {
       List<String> groups = localService.getLocalGroups();
        return new ResponseEntity(groups, HttpStatus.OK);
    }
}
