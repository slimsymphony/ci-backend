package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jajuutin
 */
public class BaseEntityImpl extends BaseEntity {

    private Long id;
    private String name;
    private List<ChildEntity> childs = new ArrayList<ChildEntity>(); 

    public BaseEntityImpl() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {        
        this.id = id;
    }    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ChildEntity> getChilds() {
        return childs;
    }

    public void setChilds(List<ChildEntity> childs) {
        this.childs = childs;
    }
}
