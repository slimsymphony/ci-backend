package com.nokia.ci.ejb.model;

import java.util.List;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractCustomVerification extends BaseEntity {
    public abstract Verification getVerification();
    public abstract void setVerification(Verification verification);
    public abstract List<? extends AbstractCustomParam> getAbstractCustomParams();
}