/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jajuutin
 */
public enum BranchType {

    SINGLE_COMMIT("Single Commit", RepositoryType.GERRIT),
    DEVELOPMENT("Development", RepositoryType.GIT),
    TOOLBOX("Toolbox", RepositoryType.GERRIT),
    MASTER("Master", RepositoryType.GIT),
    DRAFT("Draft", RepositoryType.GERRIT);
    String desc;
    RepositoryType repositoryType;

    private BranchType(String desc, RepositoryType repositoryType) {
        this.desc = desc;
        this.repositoryType = repositoryType;
    }

    public String getDesc() {
        return desc;
    }

    public RepositoryType getRepositoryType() {
        return this.repositoryType;
    }

    public static List<BranchType> getBranchTypes(RepositoryType typeFilter) {
        List<BranchType> results = new ArrayList<BranchType>();

        for (BranchType bt : values()) {
            if (bt.getRepositoryType() == typeFilter) {
                results.add(bt);
            }
        }

        return results;
    }
}
