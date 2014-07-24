/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.session;

import java.io.Serializable;
import java.util.UUID;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.infinispan.Cache;

/**
 *
 * @author jajuutin
 */
@Named
@RequestScoped
public class SessionManager {

    @Resource(lookup = "java:jboss/infinispan/cache/ci20/session-cache")
    private Cache<String, Session> cache;

    public Session getSession(String token) {
        return cache.get(token);
    }

    public Session newSession(Long sysUserId) {
        Session session = new Session();
        session.setSysUserId(sysUserId);
        session.setToken(UUID.randomUUID().toString());

        cache.put(session.getToken(), session);

        return session;
    }

    public void deleteSession(Session session) {
        if (session != null) {
            cache.remove(session.getToken());
        }
    }

    /**
     * @return the cache
     */
    public Cache<String, Session> getCache() {
        return cache;
    }

    /**
     * @param cache the cache to set
     */
    public void setCache(Cache<String, Session> cache) {
        this.cache = cache;
    }

    /**
     * Session
     */
    public static class Session implements Serializable{

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private Long sysUserId;
        private String token;

        /**
         * @return the sysUserId
         */
        public Long getSysUserId() {
            return sysUserId;
        }

        /**
         * @param sysUserId the sysUserId to set
         */
        public void setSysUserId(Long sysUserId) {
            this.sysUserId = sysUserId;
        }

        /**
         * @return the token
         */
        public String getToken() {
            return token;
        }

        /**
         * @param token the token to set
         */
        public void setToken(String token) {
            this.token = token;
        }
    }
}
