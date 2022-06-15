package com.users.connector.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.users.connector.common.CommonUtils;
import com.users.connector.entity.LdapInfo;
import com.users.connector.repository.LdapInfoRepo;
import com.users.connector.vo.LdapInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Service
public class LdapService {

    @Autowired
    private LdapInfoRepo ldapInfoRepo;


    public DirContext getDirContext(LdapInfo ldapInfo) throws Exception {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapInfo.getLdapUrl());
        env.put(Context.SECURITY_PRINCIPAL, ldapInfo.getDomainName());
        env.put(Context.SECURITY_CREDENTIALS, ldapInfo.getPassword());
        if (ldapInfo.getLdapUrl().contains("ldaps"))
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        return new InitialDirContext(env);
    }


    public List<ObjectNode> getLdapUsers(LdapInfoVO ldapInfoVO) throws Exception {
        List<ObjectNode> list = new ArrayList<>();
        Optional<LdapInfo> ldapInfoOpt = ldapInfoRepo.findById(ldapInfoVO.getId());
        if (ldapInfoOpt.isPresent()) {
            LdapInfo ldapInfo = ldapInfoOpt.get();
            DirContext context = getDirContext(ldapInfo);
            String searchFilter = "(objectClass=inetOrgPerson)";
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration users = context.search(ldapInfoVO.getDomainGroup(), searchFilter, controls);
            while (users.hasMore()) {
                ObjectNode node = CommonUtils.mapper.createObjectNode();
                SearchResult match = (SearchResult) users.next();
                Attributes attrs = match.getAttributes();
                NamingEnumeration attrsIds = attrs.getIDs();
                while (attrsIds.hasMoreElements()) {
                    String key = attrsIds.nextElement().toString();
                    if (!key.equalsIgnoreCase("objectClass")) {
                        node.put(key, attrs.get(key).get().toString());
                    }
                }
                list.add(node);
            }
        }
        return list;
    }

    public List<LdapInfo> getLdapServersInfo() {
        List<LdapInfo> ldapInfos = new ArrayList<>();
        ldapInfoRepo.findAll().forEach(ldapInfos::add);
        return ldapInfos;
    }
}
