/*
 * Copyright (c) 2013-2016 TIBCO Software Inc.
 * All Rights Reserved. Confidential & Proprietary.
 * For more information, please contact:
 * TIBCO Software Inc., Palo Alto, California, USA
 *
 * $Id: LDAPGroupUserAuthentication.java 90180 2016-12-13 23:00:37Z $
 *
 */
package com.tibco.tibems.tibemsd.security.jaas;

import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * This class extends the full featured LDAP based JAAS module for EMS
 * and provides additional group information to the EMS server. It will
 * validate all connections (be they users, routes, etc.) by
 * authenticating to the LDAP server using the supplied credentials, and 
 * then will update the EMS server with any related group information found.
 * <p>
 * Most properties added in the JAAS configuration file will be passed
 * directly to JNDI when creating the LDAP context. This allows a lot
 * of flexibility in configuring the server connection and finding LDAP
 * groups.  Only properties that begin with "tibems" are reserved for
 * this module.
 * <p>
 * See {@link com.tibco.tibems.tibemsd.security.jaas.LDAPAuthentication} for more
 * information.
 * <p>
 * To enable this module, specify the location of a JAAS Configuration file
 * through the EMS server configuration option "jaas_config_file".  Please
 * refer to the EMS user guide for further options concerning JAAS Login
 * Modules.
 * <p>
 * This module does not implement the login() method and instead relies on
 * the superclass login().  Additional group information is gathered
 * by overriding the {@link #onLdapAuthenticationSuccess(String, String)} and
 * {@link #onCacheAuthenticationSuccess(String)} methods in the superclass.
 * If caching is enabled, changes to group membership will not occur until
 * the user's entry in the cache has expired.
 * <p>
 * The Subject is used to pass group related information into the EMS server.
 * The EMS server will inspect the Subject's principals for any object that
 * has implemented the {@link java.security.acl.Group} interface and add
 * that as a group in EMS.  This provides a flexible way for LoginModules
 * to implement group membership in EMS.  A simple implementation
 * of the {@link java.security.acl.Group} interface is provided here,
 * see {@link com.tibco.tibems.tibemsd.security.jaas.LDAPGroupUserAuthentication.EMSGroup}.  
 * <p>
 * The JAAS configuration file entry for this login module should have
 * a section similar to the following:
 * <pre>
 * EMSUserAuthentication {
 *      com.tibco.tibems.tibemsd.security.jaas.LDAPGroupUserAuthentication required
 *      debug=false
 *      tibems.ldap.url="ldap://ldapserver:389"
 *      tibems.ldap.user_base_dn="ou=Marketing,dc=company,dc=com"
 *      tibems.ldap.user_attribute="uid"
 *      tibems.ldap.scope="subtree"
 *      tibems.ldap.group_base_dn="ou=Groups,dc=company"
 *      tibems.ldap.group_member_attribute="uniqueMember"
 *      tibems.ldap.dynamic_group_base_dn="ou=Groups,dc=company"
 *      tibems.ldap.dynamic_group_class="groupOfURLs"
 *      tibems.ldap.dynamic_group_member_attribute="uid"
 *      tibems.ldap.dynamic_group_filter="(objectClass=GroupOfURLs)"      
 *      tibems.cache.enabled=true
 *      tibems.cache.user_ttl=600
 *      tibems.ldap.manager="CN=Manager"
 *      tibems.ldap.manager_password="password" ;
 * }; 
 * </pre>
 * </p>
 * @see com.tibco.tibems.tibemsd.security.jaas.LDAPAuthentication
 * @see javax.security.auth.spi.LoginModule
 * @see javax.security.auth.Subject
 * @see java.security.Principal
 * @see java.security.acl.Group
 */
public class LDAPGroupUserAuthentication extends LDAPAuthentication implements LoginModule
{
    private String  ldapGroupBaseDn             = null;
    private String  ldapGroupFilter             = null;
    private String  ldapGroupAttribute          = null;
    private String  ldapGroupMemberAttribute    = null;
    private int     ldapGroupSearchScope        = 0; 

    private String  ldapDynGroupBaseDn          = null;
    private String  ldapDynGroupFilter          = null;
    private String  ldapDynGroupAttribute       = null;
    private String  ldapDynGroupClass           = null;
    private String  ldapDynGroupMemberAttribute = null;
    private String  ldapDynGroupMemberURL       = null;
    private int     ldapDynGroupSearchScope     = 0;
    private boolean ldapDynGroupSearchDirect    = false;
    
    private boolean ldapNestedGroupsEnabled     = false;
    private String  ldapNestedGroupAttribute    = null;
    private String  ldapNestedGroupFilter       = null;
    private String  ldapNestedDynGroupFilter    = null;
    
    private String  ldapBacklinkAttribute        = null;
    private String  ldapBacklinkFilter           = null;
    private String  ldapBacklinkGroupNameRdn     = null;
    private String  ldapBacklinkGroupBaseDn      = null;
    private int     ldapBackLinkSearchScope      = 0;

    /**
     * The Subject is used to return group information to the EMS server.
     */    
    private Subject        subject               = null;

    /**
     * Base path for the LDAP static group search.  If null or not set,
     * static groups are not searched.
     */
    public static final String LDAP_GROUP_BASE_DN = "tibems.ldap.group_base_dn";

    /**
     * The attribute of a static LDAP group that contains the group name.
     * The default is "cn".
     */
    public static final String LDAP_GROUP_ATTRIBUTE = "tibems.ldap.group_attribute";

    /**
     * The attribute ID of a dynamic LDAP group object that specifies the name  
     * of members of the group.  The default is "uniqueMember".
     */
    public static final String LDAP_GROUP_MEMBER_ATTRIBUTE = "tibems.ldap.group_member_attribute";

    /**
     * The filter used in the static group search. By default, a filter is
     * created using the ems_ldap.group_member_attribute property. If a
     * more complex filter is needed, use this property to override the
     * default. Any occurrence of '{0}' in the search string will be replaced
     * with the group member attribute, and '{1}' will be replaced with the
     * user DN.  '{2}' contains solely the user name for cases where the DN
     * will not match group membership.
     * <p>
     * The default is "{0}={1}".
     */
    public static final String LDAP_GROUP_FILTER = "tibems.ldap.group_filter";

    /**
     * The scope of the static group search.  Valid values include "onelevel",
     * "subtree", and "object".  Default is to use a subtree search.
     */
    public static final String LDAP_GROUP_SCOPE = "tibems.ldap.group_scope";

    /**
     * Base path for the LDAP dynamic group search.  If null or not set,
     * dynamic groups are not searched.
     */
    public static final String LDAP_DYNAMIC_GROUP_BASE_DN = "tibems.ldap.dynamic_group_base_dn";

    /**
     * The attribute of an LDAP dynamic group that contains the group name.
     * The default is "cn".
     */
    public static final String LDAP_DYNAMIC_GROUP_ATTRIBUTE = "tibems.ldap.dynamic_group_attribute";

    /**
     * The attribute ID of a dynamic LDAP group object that specifies the name  
     * of members of the group.
     */
    public static final String LDAP_DYNAMIC_GROUP_MEMBER_ATTRIBUTE = "tibems.ldap.dynamic_group_member_attribute";

    /**
     * The filter used in the dynamic group search. By default, a filter is
     * created using the ems_ldap.dynamic_group_member_attribute property.
     * If a more complex filter is needed, use this property to override
     * the default. Any occurrence of '{0}'  will be replaced with the
     * group member property and '{1}' will be replaced with the DN of the
     * user for cases where that may be required. '{2}' in the search string
     * will be replaced with the user name.
     * <p>
     * When using {@link #LDAP_DYNAMIC_GROUP_SEARCH_DIRECT}, a simple filter 
     * should be used which matches all dynamic groups that may contain the
     * user, e.g. "(objectClass=GroupOfURLs)"
     * <p>
     * The default is "{0}={1}".
     */
    public static final String LDAP_DYNAMIC_GROUP_FILTER = "tibems.ldap.dynamic_group_filter";

    /**
     * The scope of the dynamic group search.  Valid values include "onelevel",
     * "subtree", and "object".  Default is to use a subtree search.
     */
    public static final String LDAP_DYNAMIC_GROUP_SCOPE = "tibems.ldap.dynamic_group_scope";

    /**
     * The class name of a dynamic group. Default is "groupOfURLs".
     */
    public static final String LDAP_DYNAMIC_GROUP_CLASS = "tibems.ldap.dynamic_group_class";
    
    /**
     * The attribute of a dynamic LDAP group object that specifies the  
     * URL generating the membership list.  Default is "memberURL".
     */
    public static final String LDAP_DYNAMIC_GROUP_MEMBER_URL = "tibems.ldap.dynamic_group_member_url";
    
    /**
     * Changes the search algorithm used for determining membership 
     * of dynamic groups.  
     * <p>
     * Normally, LDAP servers will automatically populate dynamic groups
     * based on a configured search URL.  However, some LDAP servers have issues
     * where the generated attributes representing members of the groups are
     * not properly returned by a search.  
     * <p>
     * When enabled, this changes the group search algorithm to parse out
     * a DN, scope, and filter from the search URL specified by the dynamic
     * group and use those to search for a user.
     * <p>
     * Usage of this is only recommended when it has been determined that
     * dynamic group searches are not working.
     * <p>
     * The default is "false".
     */
    public static final String LDAP_DYNAMIC_GROUP_SEARCH_DIRECT = "tibems.ldap.dynamic_group_search_direct";

    /**
     * Base path for the back linked LDAP group search.  By default, back
     * linked group searches are not enabled.  If enabled, back linked groups,
     * including nested groups, are searched using back link parameters.
     * <p>
     * To disable nested searches for back links, 
     * set tibems.ldap.nested_groups_enabled to false.
     * <p>
     * The defaults are set for use with Active Directory, the most commonly
     * used LDAP server supporting back links.
     */
    public static final String LDAP_BACKLINK_GROUP_BASE_DN = "tibems.ldap.backlink_group_base_dn";
    
    /**
     * The attribute that contains the groups an LDAP object (member or group)
     * belongs to.  The default is "memberOf".
     */
    public static final String LDAP_BACKLINK_GROUP_ATTR = "tibems.ldap.backlink_group_attribute";

    /**
     * A back-link RDN that specifies the name portion of the DN representing
     * the group.  If the entire value of the back link value is to be used
     * as the group name, do not set this value.  Default is "CN".
     */
    public static final String LDAP_BACKLINK_GROUP_RDN = "tibems.ldap.backlink_group_rdn";

    /**
     * A back-link filter used by a group search to find groups the
     * member belongs to.  If nested groups are not found, then it is highly
     * advisable to disable nested groups.  The default setting is 
     * "(distinguishedName={1})".
     */
    public static final String LDAP_BACKLINK_GROUP_FILTER = "tibems.ldap.backlink_group_filter";
    
    /**
     * The scope of the back link group search.  Valid values include "onelevel",
     * "subtree", and "object".  Default is to use a subtree search.
     */
    public static final String LDAP_BACKLINK_GROUP_SCOPE = "tibems.ldap.backlink_group_scope";
    
    /**
     * Expand searches to include nested groups. Default is enabled, "true".  
     * <p>
     * Many LDAP implementations support nested groups, or a group that
     * has a reference to a subgroup.  When a group is found for a particular
     * user, other groups are checked to see if they reference the users 
     * group.  The user is then assigned to all groups found upward in the
     * hierarchy.
     * <p>
     * Note, other nested group parameters are not used with back link group
     * searches.
     * <p>
     * If a LDAP server is known not to use nested groups, disabling this
     * will provide better performance.
     */
    public static final String LDAP_NESTED_GROUPS_ENABLED = "tibems.ldap.nested_groups_enabled";
    
    /**
     * The filter to use when searching for parents of a user's group.
     * The default is "(&(objectClass=groupOfUniqueNames)({0}={1}))".
     * {0} represents the nested group attribute, {1} is the DN of the
     * group being searched for, and {2} is available as the group name.
     */
    public static final String LDAP_NESTED_GROUPS_FILTER = "tibems.ldap.nested_groups_filter";

    /**
     * The filter to use when searching dynamic groups of a user's group.
     * The default is "(&(objectClass=groupOfURLs)({0}={1}))".
     * {0} represents the nested group attribute, {1} is the DN of the
     * group being searched for, and {2} is available as the group name.
     */
    public static final String LDAP_NESTED_DYNAMIC_GROUPS_FILTER = "tibems.ldap.nested_dynamic_groups_filter";
    
    /**
     * The attribute of a group that contains a subgroup. The default is
     * "uniquemember".
     */
    public static final String LDAP_NESTED_GROUPS_ATTRIBUTE = "tibems.ldap.nested_groups_attribute";
    
    /**
     * The EMSGroup class represents a group that can be passed to the
     * EMS server via this LoginModule's Subject.
     * <p>
     * For security reasons, the EMS server ignores membership of the
     * Group itself, only using its presence as a Principal in the Subject
     * to indicate the member being authenticated belongs to a particular
     * group.
     * 
     * @see java.security.acl.Group 
     */
    private class EMSGroup implements java.security.acl.Group
    {
        /**
         * Name of the group.
         */
        private String name;
        
        /**
         * Members of the group, hashed by name for easy lookup.
         */
        private Hashtable<String, Principal> members = null;

        /**
         * A private default constructor ensures we always create
         * a group with a name.
         */
        @SuppressWarnings("unused")
        private EMSGroup() {}
        
        /**
         * Group constructor
         * 
         * @param name name of the group.
         */
        EMSGroup(String name)
        {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (super.equals(obj))
                return true;
            
            if (obj instanceof java.security.acl.Group)
            {
                java.security.acl.Group g = (java.security.acl.Group) obj;
                if (g.getName().equalsIgnoreCase(name))
                    return true;
            }
            
            return false;
        }
        
        @Override
        public String getName()
        {
            return name;
        }

        /**
         * In EMS, membership of a Group is ignored for security purposes,
         * but implementation of the methods below are provided for completeness.
         */
        @Override
        public boolean addMember(Principal user)
        {
            if (user == null)
                return false;
            
            if (members == null)
                members = new Hashtable<String, Principal>();
            
            if (isMember(user))
                return false;
            
            members.put(user.getName(), user);
            
            return true;
        }
        
        @Override
        public boolean removeMember(Principal user)
        {
            if (user == null)
                return false;
            
            return (members.remove(user.getName()) != null);
        }

        @Override
        public boolean isMember(Principal member)
        {
            return members.contains(member);
        }

        @Override
        public Enumeration<? extends Principal> members()
        {
            return members.elements();
        }
    }

    /**
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) 
    {
        initializeModule(subject, callbackHandler, sharedState, options);
        printProperties();
    }

    /**
     * @see com.tibco.tibems.tibemsd.security.jaas.LDAPAuthentication#initializeModule(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initializeModule(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) 
    {
        super.initializeModule(subject, callbackHandler, sharedState, options);
        
        /*
         *  Add the default values, then override them with the contents
         *  of the options parameter.
         */
        HashMap<String, Object> defaults = new HashMap<String, Object>();

        /*
         * The LDAP_GROUP_BASE_DN and LDAP_DYNAMIC_GROUP_BASE_DN properties
         * default to null (unset).  If these are not set, the searches
         * are not performed. 
         */
        defaults.put(LDAP_GROUP_ATTRIBUTE, "cn");
        defaults.put(LDAP_GROUP_MEMBER_ATTRIBUTE, "uniqueMember");
        defaults.put(LDAP_GROUP_FILTER, "{0}={1}");
        defaults.put(LDAP_GROUP_SCOPE, "subtree");
        
        defaults.put(LDAP_DYNAMIC_GROUP_ATTRIBUTE, "cn");
        defaults.put(LDAP_DYNAMIC_GROUP_MEMBER_ATTRIBUTE, "uid");
        defaults.put(LDAP_DYNAMIC_GROUP_FILTER, "(&(objectClass=groupOfURLs)({0}={1}))");
        defaults.put(LDAP_DYNAMIC_GROUP_CLASS, "groupOfURLs");
        defaults.put(LDAP_DYNAMIC_GROUP_SCOPE, "subtree");
        defaults.put(LDAP_DYNAMIC_GROUP_MEMBER_URL, "memberURL");  
        defaults.put(LDAP_DYNAMIC_GROUP_SEARCH_DIRECT, "false");

        defaults.put(LDAP_NESTED_GROUPS_ENABLED, "true");
        defaults.put(LDAP_NESTED_GROUPS_ATTRIBUTE, "uniquemember");
        defaults.put(LDAP_NESTED_GROUPS_FILTER, "(&(objectClass=groupOfUniqueNames)({0}={1}))");
        defaults.put(LDAP_NESTED_DYNAMIC_GROUPS_FILTER, "(&(objectClass=groupOfURLs)({0}={1}))");

        /*
         * The default values for back links are set to work with Active
         * Directory, as that is the most common LDAP implementation backlinks
         * are used with.
         */
        defaults.put(LDAP_BACKLINK_GROUP_FILTER, "(distinguishedName={1})");
        defaults.put(LDAP_BACKLINK_GROUP_ATTR, "memberOf");
        defaults.put(LDAP_BACKLINK_GROUP_RDN, "CN");
        defaults.put(LDAP_BACKLINK_GROUP_SCOPE, "subtree");
        
        jaasConfig.addOptions(defaults, options);
        
        ldapGroupBaseDn          = jaasConfig.getString(LDAP_GROUP_BASE_DN);
        ldapGroupAttribute       = jaasConfig.getString(LDAP_GROUP_ATTRIBUTE);
        ldapGroupMemberAttribute = jaasConfig.getString(LDAP_GROUP_MEMBER_ATTRIBUTE);
        ldapGroupFilter          = jaasConfig.getString(LDAP_GROUP_FILTER);
        ldapGroupSearchScope     = jaasConfig.getScope(LDAP_GROUP_SCOPE);
        
        ldapDynGroupBaseDn          = jaasConfig.getString(LDAP_DYNAMIC_GROUP_BASE_DN);
        ldapDynGroupAttribute       = jaasConfig.getString(LDAP_DYNAMIC_GROUP_ATTRIBUTE);
        ldapDynGroupMemberAttribute = jaasConfig.getString(LDAP_DYNAMIC_GROUP_MEMBER_ATTRIBUTE);
        ldapDynGroupMemberURL       = jaasConfig.getString(LDAP_DYNAMIC_GROUP_MEMBER_URL);
        ldapDynGroupClass           = jaasConfig.getString(LDAP_DYNAMIC_GROUP_CLASS);
        ldapDynGroupSearchScope     = jaasConfig.getScope(LDAP_DYNAMIC_GROUP_SCOPE);
        ldapDynGroupSearchDirect    = jaasConfig.getBoolean(LDAP_DYNAMIC_GROUP_SEARCH_DIRECT);
        ldapDynGroupFilter          = jaasConfig.getString(LDAP_DYNAMIC_GROUP_FILTER);
        
        ldapNestedGroupsEnabled     = jaasConfig.getBoolean(LDAP_NESTED_GROUPS_ENABLED);
        ldapNestedGroupFilter       = jaasConfig.getString(LDAP_NESTED_GROUPS_FILTER);
        ldapNestedDynGroupFilter    = jaasConfig.getString(LDAP_NESTED_DYNAMIC_GROUPS_FILTER);
        ldapNestedGroupAttribute    = jaasConfig.getString(LDAP_NESTED_GROUPS_ATTRIBUTE);
        
        ldapBacklinkAttribute       = jaasConfig.getString(LDAP_BACKLINK_GROUP_ATTR);
        ldapBacklinkGroupNameRdn    = jaasConfig.getString(LDAP_BACKLINK_GROUP_RDN);
        ldapBacklinkFilter          = jaasConfig.getString(LDAP_BACKLINK_GROUP_FILTER);
        ldapBacklinkGroupBaseDn     = jaasConfig.getString(LDAP_BACKLINK_GROUP_BASE_DN);
        ldapBackLinkSearchScope     = jaasConfig.getScope(LDAP_BACKLINK_GROUP_SCOPE);

        this.subject = subject;
    }
    
    /**
     * Retrieves a string attribute value from a search result.
     * 
     * @param sr the search result.
     * @return the string value of the attribute.
     * @throws NamingException
     */
    private String getStringAttributeValue(SearchResult sr,
            String groupNameAttribute) throws NamingException
    {
        javax.naming.directory.Attributes attrs = null;
        javax.naming.directory.Attribute  attr  = null;
        
        if (sr == null)
            return null;
        
        /*
         * If there are no attributes in the result, then an empty
         * LDAP leaf has been found, and should be ignored, but reported.
         */
        attrs = sr.getAttributes();
        if (attrs == null)
        {
            debugLog("LDAP LoginModule: No attributes in %s.\n",
                    sr.getNameInNamespace());
            return null;
        }
    
        /*
         * If an entry was found, but no attribute was present,
         * this could be a configuration error, so report it.
         */
        attr = attrs.get(groupNameAttribute);
        if (attr == null)
        {
            debugLog("LDAP LoginModule: Unable to retrieve attribute %s in %s.\n",
                    groupNameAttribute, sr.getNameInNamespace());
            return null;
        }

        /* 
         * If the attribute was empty, consider this an empty
         * name.  For our purposes this is invalid - skip and
         * and report this for the user.
         */
        if (attr.size() < 0)
        {
            debugLog("LDAP LoginModule: Empty attribute %s in %s.\n",
                    groupNameAttribute, sr.getNameInNamespace());
            return null;
        }
        
        return attr.get().toString();        
    }
    
    /**
     * Retrieves search results containing a back linked attribute from 
     * a group DN or name.
     *  
     * @param context the context to perform the search for groups with.
     * 
     * @param groupDN the DN of the parent of the back link attribute
     * 
     * @param groupName the name of the group
     * 
     * @param groups a list to append the names of parent groups.
     * 
     * @return a result set populated with groups.  This may be empty if the
     *         group is a top level group.
     * 
     * @throws NamingException
     */
    private NamingEnumeration<SearchResult> searchBacklinks(
        LdapContext context, String groupDN, String groupName,
        List<String> groups) throws NamingException
    {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(ldapBackLinkSearchScope);
        
        searchCtls.setReturningAttributes(
            new String[]{ldapBacklinkAttribute});

        debugLog("LDAP LoginModule: Searching for backlinked groups for %s:\n", groupDN);
        debugLog("LDAP LoginModule:     filter = %s\n", ldapBacklinkFilter);
        debugLog("LDAP LoginModule:     {0} = %s\n", ldapBacklinkAttribute);
        debugLog("LDAP LoginModule:     {1} = %s\n", groupDN);
        debugLog("LDAP LoginModule:     {2} = %s\n", groupName);
       
        NamingEnumeration<SearchResult> result = context.search(
            ldapBacklinkGroupBaseDn, ldapBacklinkFilter,
            new Object[] { ldapBacklinkAttribute, groupDN, groupName },
            searchCtls);
        
        return result;
    }
    
    /**
     * Adds the back-linked groups an object references into a string list.
     * When nesting is enabled, this method will recurse using the back-link
     * attribute.
     *  
     * @param context the context to perform the search for groups with.
     * 
     * @param result a single result of a search for an item that may have
     *        a back-linked attribute.
     *        
     * @param groups a list to append the names of parent groups.
     * 
     * @throws NamingException
     */
    private void getBacklinkedGroups(LdapContext context,
        SearchResult result, List<String> groups) throws NamingException
    {
        String   name     = null;
        LdapName ldapName = null;
        
        if (ldapBacklinkGroupBaseDn == null)
        {
            debugLog("LDAP LoginModule: Backlinked group searches are not configured.\n");
            return;
        }
        
        debugLog("LDAP LoginModule: Searching for backlinked groups through %s, DN=%s.\n",
            ldapBacklinkAttribute, result.getNameInNamespace());
        
        javax.naming.directory.Attributes attrs = result.getAttributes();
        if (attrs.size() <= 0)
            return;
        
        javax.naming.directory.Attribute attr = attrs.get(ldapBacklinkAttribute);
        if (attr == null || attr.size() <= 0)
        {
            debugLog("LDAP LoginModule: Attribute not found.\n");
            return;
        }
        
        for (int i = 0; i < attr.size(); i++)
        {
            name = null;
            
            /**
             * Find the name of the group from the attribute.  As the attribute
             * contains a list of strings, build an LDAP name to parse out
             * the group name based on the configured RDN.
             */
            if (ldapBacklinkGroupNameRdn != null)
            {
                ldapName = new LdapName((String)attr.get(i));
                
                for(Rdn rdn : ldapName.getRdns())
                {
                    if(rdn.getType().equalsIgnoreCase(ldapBacklinkGroupNameRdn))
                    {
                        name = (String)rdn.getValue();
                        break;
                    }
                }
                
                if (name == null)
                {
                    debugLog("LDAP LoginModule: Did not find specifier '%s' in \"%s\"\n",
                        ldapBacklinkGroupNameRdn, name);
                }
            }
            else
            {
                name = (String)attr.get(i);
            }

            if (name != null)
            {
                /**
                 * If a group name was found, add it to the group.  If nested
                 * groups are enabled, then recurse up the chain of groups 
                 * using the back links.  Check for existence to avoid circular
                 * references.
                 */
                if (groups.contains(name))
                {
                    debugLog("LDAP LoginModule: Found circular nested group reference.\n");                    
                }
                else
                {
                    groups.add(name);
                    
                    if (ldapNestedGroupsEnabled == true)
                    {
                        debugLog("LDAP LoginModule: Searching for nested groups.\n");
                        
                        NamingEnumeration<SearchResult> searchResults =
                            searchBacklinks(context, (String)attr.get(i),
                            name, groups);
                        
                        while (searchResults.hasMoreElements())
                        {
                            getBacklinkedGroups(context, searchResults.next(),
                                groups);
                        }
                    }
                }
            }
        }
    }

    /**
     * This recursive method builds the list of groups a user belongs to.
     * <p>
     * When nested group searches are enabled, this method recurses the
     * LDAP tree appending to the groups list all groups in LDAP that
     * directly or indirectly reference the passed group search results.
     * 
     * @param groupSearchResults a NamingEnumeration containing groups
     *                           a user belongs to.
     *                            
     * @param baseDN             the base of nested group searches.
     * 
     * @param groupNameAttribute the attribute of a group that contains
     *                           the group name.
     *                           
     * @param nestedGroupFilter  the search filter.
     * 
     * @param scope              the LDAP search scope for nested groups.
     * 
     * @param groups             a list to append the names of parent groups.
     * @throws NamingException
     * @throws LoginException 
     * @see #LDAP_NESTED_GROUPS_ENABLED
     * @see #getDynamicGroupMembership(LdapContext, String, String, List)
     * @see #getStaticLDAPGroups(LdapContext, String, String, List)
     */
    private void buildUserGroupList(
        NamingEnumeration<SearchResult> groupSearchResults,
        String baseDN, String groupNameAttribute, String nestedGroupFilter,
        String nestedGroupAttribute, int scope, List<String> groups)
            throws NamingException, LoginException
    {
        if (groupSearchResults == null)
            return;
        
        while (groupSearchResults.hasMoreElements())
        {
            /* 
             * buildUserGroupListFromResult may recurse back into this 
             * method.
             */
            buildUserGroupListFromResult(groupSearchResults.next(),
                baseDN, groupNameAttribute, nestedGroupFilter,
                nestedGroupAttribute, scope, groups);
        }
    }
    
    /**
     * This method is called by buildUserGroupList and
     * getDynamicGroupMembershipDirect in order to recursively build 
     * a list of groups that a user belongs to based on a single result
     * of a group search.
     *   
     * @param groupResult        SearchResult from a group search representing
     *                           a group a user belongs to.
     * @param baseDN             the base of nested group searches.
     * @param nestedGroupFilter  the search filter
     * @param groupNameAttribute the attribute of a group that contains
     *                           the group name.
     * @param scope              the LDAP search scope for nested groups.
     * @param groups             a list of group names to append names of
     *                           discovered groups.
     * 
     * @throws NamingException
     * @throws LoginException
     *  
     * @see #LDAP_NESTED_GROUPS_ENABLED
     * @see #buildUserGroupList(NamingEnumeration, String, String, String, String, int, List)
     * @see #getDynamicGroupMembershipDirect(LdapContext, String, List)
     */
    private void buildUserGroupListFromResult(SearchResult groupResult,
        String baseDN, String groupNameAttribute, String nestedGroupFilter,
        String nestedGroupAttribute, int scope, List<String> groups) 
            throws NamingException, LoginException
    {
        String groupName = getStringAttributeValue(groupResult,
                groupNameAttribute);
        
        if (groupName == null)
        {
            debugLog("LDAP LoginModule: Invalid group in nested group search:  %s.\n", groupResult);
            return;
        }
        
        if (ldapNestedGroupsEnabled == false)
        {
            groups.add(groupName);
        }
        else
        {
            if (groups.contains(groupName))
            {
                debugLog("LDAP LoginModule: Found circular nested group reference.\n");
            }
            else
            {
                groups.add(groupName);
                  
                String groupDN = groupResult.getNameInNamespace();
               
                debugLog("LDAP LoginModule: Parent search for group %s.\n", groupDN);
                
                /*
                 * Create another instance of the manager context because
                 * this method may be operating under other manager context
                 * search results.
                 */
                LdapContext context = newManagerLdapContextInstance();
                
                SearchControls searchCtls = new SearchControls();
                searchCtls.setSearchScope(scope);
                
                searchCtls.setReturningAttributes(
                    new String[]{groupNameAttribute, nestedGroupAttribute});
                
                debugLog("LDAP LoginModule: Searching for nested groups under %s:\n", baseDN);
                debugLog("LDAP LoginModule:     filter = %s\n", nestedGroupFilter);
                debugLog("LDAP LoginModule:     {0} = %s\n", nestedGroupAttribute);
                debugLog("LDAP LoginModule:     {1} = %s\n", groupDN);
                debugLog("LDAP LoginModule:     {2} = %s\n", groupName);
               
                NamingEnumeration<SearchResult> result = context.search(
                    baseDN, nestedGroupFilter,
                    new Object[] { nestedGroupAttribute, groupDN, groupName },
                    searchCtls);

                 closeLdapContext(context);

                /*
                 * buildUserGroupList may recurse back into this method.
                 */
                buildUserGroupList(result, baseDN, groupNameAttribute,
                    nestedGroupFilter, nestedGroupAttribute, scope, groups);
                
            }
        }
    }
    
    /**
     * Retrieves the static LDAP groups that a user belongs to.
     * <p>
     * If the base DN for a static group search has not been configured,
     * this method immediately returns.
     * <p>
     * If groups are found, but further information is unable to
     * be retrieved, an error is logged and no groups are returned.
     * This could be either intentional in the LDAP layout or a
     * configuration error.
     * <p>
     * @param context the context to perform the search for groups with.
     * @param userName the name of the user being authenticated.
     * @param userDN  the distinguished name of the user being authenticated.
     * @param groups a list of groups the user belongs to, or null.
     * @throws NamingException
     * @throws LoginException 
     */
    private void getStaticLDAPGroups(LdapContext context, SearchResult userResult,
        String userName, List<String> groups)
            throws NamingException, LoginException
    {
        NamingEnumeration<SearchResult>   result    = null;
        
        if (ldapGroupBaseDn == null)
        {
            debugLog("LDAP LoginModule: Static group searches are not configured.\n");
            return;
        }
            
        /*
         * Search based on configured parameters.
         */
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(ldapGroupSearchScope);
        searchCtls.setReturningAttributes(new String[]{ldapGroupAttribute, ldapGroupMemberAttribute});
        
        String userDN = userResult.getNameInNamespace();
        
        debugLog("LDAP LoginModule: Searching for static groups under %s:\n", ldapGroupBaseDn);
        debugLog("LDAP LoginModule:     filter = %s\n", ldapGroupFilter);
        debugLog("LDAP LoginModule:     {0} = %s\n", ldapGroupMemberAttribute);
        debugLog("LDAP LoginModule:     {1} = %s\n", userDN);
        debugLog("LDAP LoginModule:     {2} = %s\n", userName);

        result = context.search(ldapGroupBaseDn, ldapGroupFilter,
            new Object[] { ldapGroupMemberAttribute, userDN, userName },
            searchCtls);
    
        /* 
         * If no results are returned, the user does not belong to any
         * groups and return.
         */
        if (result.hasMoreElements() == false)
        {
            debugLog("LDAP LoginModule: No static groups found for user %s.\n",
                userName);
            return;
        }
    
        buildUserGroupList(result, ldapGroupBaseDn,
            ldapGroupAttribute, ldapNestedGroupFilter,
            ldapNestedGroupAttribute, ldapGroupSearchScope, groups);
    }
    
    /**
     * Performs a search of dynamic groups and populates a list
     * with group names.
     * <p>
     * This method is used when {@link #LDAP_DYNAMIC_GROUP_SEARCH_DIRECT}
     * has not been enabled.  A straightforward search of membership
     * in dynamic LDAP groups is performed.
     * 
     * @param context the search context
     * @param userName name of the user
     * @param userDN distinguished name of the user
     * @param groups string list of groups names to populate
     * @throws NamingException
     * @throws LoginException 
     * @see {@link #LDAP_DYNAMIC_GROUP_SEARCH_DIRECT}
     */
    private void getDynamicGroupMembership(LdapContext context, SearchResult userResult,
        String userName, List<String> groups)
            throws NamingException, LoginException
    {
        NamingEnumeration<SearchResult>   groupSearchResults = null;
        
        /*
         * Search based on configured parameters.
         */
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(ldapDynGroupSearchScope);
        searchCtls.setReturningAttributes(new String[]{ldapDynGroupAttribute});
        
        String userDN = userResult.getNameInNamespace();
        
        debugLog("LDAP LoginModule: Searching for dynamic groups under %s:\n", ldapDynGroupBaseDn);
        debugLog("LDAP LoginModule:     filter = %s\n", ldapDynGroupFilter);
        debugLog("LDAP LoginModule:     {0} = %s\n", ldapDynGroupMemberAttribute);
        debugLog("LDAP LoginModule:     {1} = %s\n", userDN);
        debugLog("LDAP LoginModule:     {2} = %s\n", userName);
        
        groupSearchResults = context.search(ldapDynGroupBaseDn,
            ldapDynGroupFilter,
            new Object[] { ldapDynGroupMemberAttribute, userDN, userName },
            searchCtls);
    
        /* 
         * If no results are returned, the user does not belong to any
         * groups and return.
         */
        if (groupSearchResults.hasMoreElements() == false)
        {
            debugLog("LDAP LoginModule: No dynamic groups found for user %s.\n",
                userName);
            return;
        }
    
        buildUserGroupList(groupSearchResults, ldapDynGroupBaseDn,
            ldapDynGroupAttribute, ldapNestedDynGroupFilter,
            ldapNestedGroupAttribute, ldapDynGroupSearchScope, groups);        
    }
    

    /**
     * Determine if a search using components of a search URL would find a 
     * user.
     * <p>
     * Used when {@link #LDAP_DYNAMIC_GROUP_SEARCH_DIRECT} has been enabled.
     * 
     * @param context the search context
     * @param userName name of the user
     * @param memberURL a search URL, as specified in a dynamic group.
     * @return true if the user is found, false otherwise
     * @throws NamingException
     */
    private boolean isUserReferencedBySearchURL(LdapContext context,
        String userName, String memberURL)
    {
        String         searchDN     = null;
        SearchControls searchCtls   = null;
        String         filter       = null;
        String         query[] = { null, null, null, null };
        
        if (memberURL == null || memberURL.isEmpty())
            return false;
        
        /*
         * We will use URI to parse out the member URL so that escaped
         * characters are processed.
         * 
         * memberURL = ldap://host:port/DN?attributes?scope?filter?extensions
         * 
         * Parsing with the URI class will handle encoding and, in terms of
         * a URI, will yield:
         * 
         * authority  = host:port
         * fragment   = null
         * host       = host
         * path       = /DN
         * port       = port
         * query      = attribs?scope?filter?extensions
         * scheme     = ldap
         * schemepart = //host:port/DN?attributes?scope?filter?extensions
         */
        try
        {
            java.net.URI uri = new java.net.URI(memberURL);

            /*
             * We are interested in the DN, scope, and filter.  These
             * resolve to the path and query components of a URI.
             */
            searchDN = uri.getPath();
            
            if (uri.getQuery() != null)
                query = uri.getQuery().split("\\?");
        }
        catch (URISyntaxException e)
        {
            debugLog("LDAP LoginModule: Invalid group member URL: %s.\n", memberURL);
            return false;
        }
        
        /*
         * the path may start with a / which needs
         * to be removed.  
         */
        if (searchDN.startsWith("/"))
            searchDN = searchDN.substring(1);
        
        /*
         * The query parts contain:
         * ["?" [attributes] ["?" [scope]
         *      ["?" [filter] ["?" extensions]]]]]]
         * 
         * queryParts[0] = attributes - (ignored in place of LDAP module settings)
         * queryParts[1] = scope, a substring by default.
         * queryParts[2] = filter, if present, used in the search
         * queryParts[3] = extensions (ignored)
         */

        if (searchDN == null)
            return false;

        /*
         * default to the subtree search scope.
         */
        searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        
        if (query[1] == null)
        {
            if (query[1].equals("base"))
                searchCtls.setSearchScope(SearchControls.OBJECT_SCOPE);
            else if (query[1].equals("one"))
                searchCtls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        }
        
        /*
         * Setup the filter.
         */
        String userFilter = "(" + ldapDynGroupMemberAttribute + "=" + userName + ")";
        if (query[2] != null)
            filter = "(&" + query[2] + userFilter + ")";
        else
            filter = userFilter;
        
        debugLog("LDAP LoginModule: Direct search of %s with filter %s\n", searchDN, filter);

        /*
         * Any result indicates this user belongs to the memberURL specified;
         * rely on the filter to ensure this.
         */
        NamingEnumeration<SearchResult> result;
        try
        {
            result = context.search(searchDN, filter, searchCtls);
            if (result.hasMore())
                return true;
        }
        catch (NamingException e)
        {
            debugLog("LDAP LoginModule: LDAP search error (%s).\n", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Determines if a user can be found by any URLS used by the
     * dynamic group to generate membership.  This is only used when
     * {@link #LDAP_DYNAMIC_GROUP_SEARCH_DIRECT} has been specified.
     * 
     * @param context the search context
     * @param sr a SearchResult referencing a dynamic group
     * @param userName the name of the user
     * @return true if the user is found, false otherwise
     * @throws NamingException
     */
    private boolean isUserReferencedByDynamicGroup(LdapContext context,
        SearchResult sr, String userName) throws NamingException
    {
        javax.naming.directory.Attribute  attr  = null;
        int                               count = 0;

        attr = sr.getAttributes().get(ldapDynGroupMemberURL);
        if (attr == null)
        {
            debugLog("LDAP LoginModule: No attribute %s found in %s.\n",
                ldapDynGroupMemberURL, sr.getNameInNamespace());
            debugLog("LDAP LoginModule: %s\n", sr.getAttributes());
            return false;
        }
        
        count = attr.size();
        if (count < 1)
        {
            debugLog("LDAP LoginModule: Empty attribute %s found in %s.\n",
                ldapDynGroupMemberURL,sr.getNameInNamespace());
            debugLog("LDAP LoginModule: %s\n", attr);
            return false;
        }

        /*
         * Search the memberURL attributes. There may be more than one.
         */
        for (int i = 0; i < count; i++)
        {
            String memberURL = (String) attr.get(i);
            if (isUserReferencedBySearchURL(context, userName, memberURL))
            {
                debugLog("LDAP LoginModule: Found user %s in URL %s\n", userName, memberURL);
                return true;
            }
        }
        
        return false;
    }

    /**
     * Searches for a user's membership in dynamic groups by finding
     * all groups matching the search filter, then determining if that
     * user meets the search criteria specified in the group membership URL(s).
     * This is called when {@link #LDAP_DYNAMIC_GROUP_SEARCH_DIRECT} has been set.
     * 
     * @param context the search context
     * @param userName the name of the user
     * @param groups string list of groups names to populate
     * @throws NamingException
     * @throws LoginException 
     */
    private void getDynamicGroupMembershipDirect(LdapContext context,
        String userName, List<String> groups)
            throws NamingException, LoginException
    {
        NamingEnumeration<SearchResult>   result    = null;
        String                            groupName = null;
        
        debugLog("LDAP LoginModule: performing direct search.");
        /*
         * Search based on configured parameters.
         */
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(ldapDynGroupSearchScope);
        searchCtls.setReturningAttributes(new String[]{ldapDynGroupClass});

        debugLog("LDAP LoginModule: Searching for dynamic groups (direct) under %s:\n", ldapDynGroupBaseDn);
        debugLog("LDAP LoginModule:     filter = %s\n", ldapDynGroupFilter);
        
        /*
         * First get all dynamic groups under our base DN.
         */
        result = context.search(ldapDynGroupBaseDn, ldapDynGroupFilter, searchCtls);
    
        if (result.hasMoreElements() == false)
        {
            debugLog("LDAP LoginModule: No dynamic groups found.\n");
            return;
        }
        
        while (result.hasMoreElements())
        {
            SearchResult sr = result.next();
            
            groupName = getStringAttributeValue(sr,
                    ldapDynGroupAttribute);
            
            if (groupName == null)
            {
                debugLog("LDAP LoginModule:  Invalid group found:  %s.", sr.getAttributes());
                continue;
            }
            
            debugLog("LDAP LoginModule: Checking dynamic group: %s.\n",
                groupName);
            
            if (isUserReferencedByDynamicGroup(context, sr, userName))
            {
                buildUserGroupListFromResult(sr, ldapDynGroupBaseDn,
                    ldapDynGroupAttribute, ldapNestedDynGroupFilter,
                    ldapNestedGroupAttribute, ldapDynGroupSearchScope,
                    groups);        
            }
        }
    }
    
    /**
     * Retrieves the dynamic LDAP groups that a particular user belongs to.
     * <p>
     * If the base DN for a dynamic group search has not been configured,
     * this method immediately returns.
     * <p>
     * If groups are found, but further information is unable to
     * be retrieved, an error is logged and null is returned.
     * This could be either intentional in the LDAP layout or a
     * configuration error.  
     * <p>
     * @param context the context to perform the search for groups with.
     * @param userName the name of the user being authenticated.
     * @param userDN  the distinguished name of the user being authenticated.
     * @param groups a list of groups the user belongs to, or null.
     * @throws NamingException
     * @throws LoginException 
     */
    private void getDynamicLDAPGroups(LdapContext context,
        SearchResult userResult, String userName, List<String> groups)
            throws NamingException, LoginException
    {
        if (ldapDynGroupBaseDn == null)
        {
            debugLog("LDAP LoginModule: Dynamic group searches are not configured.\n");
            return;
        }

        if (ldapDynGroupSearchDirect)
            getDynamicGroupMembershipDirect(context, userName, groups);
        else
            getDynamicGroupMembership(context, userResult, userName, groups);
    }
    
    /**
     * Retrieves static and dynamic LDAP groups that a particular user
     * belongs to.
     * <p>
     * If no groups are found, this method returns null.
     * <p>
     * If groups are found, but further information is unable to
     * be retrieved, an error is logged and null is returned.
     * This could be either intentional in the LDAP layout or a
     * configuration error.  
     * <p>
     * @param context the context to perform the search for groups with.
     * @param userName the name of the user being authenticated.
     * @param userDN  the distinguished name of the user being authenticated.
     * @return a list of groups the user belongs to, or null.
     * @throws NamingException
     * @throws LoginException 
     */
    private List<String> getLdapGroups(LdapContext context,
        SearchResult userResult, String userName, String userDN)
            throws NamingException, LoginException
    {
        List<String> groups = new ArrayList<String>();
        
        if (ldapGroupBaseDn == null && ldapDynGroupBaseDn == null &&
            ldapBacklinkGroupBaseDn == null)
        {
            debugLog("LDAP LoginModule: Warning - no group search base DNs are configured.\n");
            return null;
        }
        
        getBacklinkedGroups(context, userResult, groups);
        getStaticLDAPGroups(context, userResult, userName, groups);
        getDynamicLDAPGroups(context, userResult, userDN, groups);

        if (groups.size() == 0)
            return null;
        
        return groups;
    }
    
    /**
     * Updates the Subject to pass Group information back to 
     * the EMS server.
     *  
     * @param userName name of the user authenticated.
     * @param groups list of groups the user belongs to.
     */
    private void updateSubject(String userName, List<String> groups)
    {
        if (groups == null)
            return;
        
        if (groups.isEmpty())
            return;

        for (String groupName : groups)
        {
            subject.getPrincipals().add(new EMSGroup(groupName));
            
             debugLog("LDAP LoginModule: Added user %s to group %s\n",
                 userName, groupName);
        }
    }
    
    /**
     * This method, invoked from the superclass login() method, retrieves
     * group related information and updates the Subject.
     *
     * @see com.tibco.tibems.tibemsd.security.jaas.LDAPAuthentication#onLdapAuthenticationSuccess(String, String)
     */
    @Override
    protected void onLdapAuthenticationSuccess(String userName, SearchResult userResult) 
            throws LoginException, CommunicationException
    {
        LdapContext                     context = null;
        List<String>                    groups = null;;
        
        String userDN = userResult.getNameInNamespace();
        
        super.onLdapAuthenticationSuccess(userName, userResult);
        
        if ((ldapGroupAttribute == null && ldapDynGroupAttribute == null) ||
            userDN == null ||  userDN.length() <= 0)
        {
            debugLog("LDAP LoginModule: Ignoring group lookup.\n");
            return;
        }

        try 
        {
            context = newManagerLdapContextInstance();
            groups  = getLdapGroups(context, userResult, userName, userDN);
            if (groups != null)
            {
                updateSubject(userName, groups);
                
                if (userCache != null)
                    userCache.setGroups(userName, groups);
            }

            closeLdapContext(context);
        }
        catch (NamingException ne)
        {
            throw buildLoginException(ne, "Invalid LDAP configuration.");
        }
        finally 
        {
            closeLdapContext(context);
        }
    }
    
    /**
     * This method, invoked from the superclass login() method, updates the
     * Subject for the EMS server with cached group information.
     *
     * @see com.tibco.tibems.tibemsd.security.jaas.LDAPAuthentication#onCacheAuthenticationSuccess(String)
     * 
     */
    @Override
    protected void onCacheAuthenticationSuccess(String userName) throws LoginException
    {
        if (userCache == null)
            return;
         
        updateSubject(userName, userCache.getGroups(userName));
    }
}
