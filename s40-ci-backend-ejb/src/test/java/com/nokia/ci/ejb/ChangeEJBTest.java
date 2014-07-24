/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import static com.nokia.ci.ejb.CITestBase.createEntity;
import static com.nokia.ci.ejb.EJBTestBase.createTypedQueryMock;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.model.Change;
import javax.persistence.TypedQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author hhellgre
 */
public class ChangeEJBTest extends EJBTestBase {

    private ChangeEJB ejb;

    @Override
    @Before
    public void before() {
        super.before();

        ejb = new ChangeEJB();
        ejb.em = em;
        ejb.cb = cb;
        ejb.context = context;
    }

    @Test
    public void getChangeByCommitId() throws BackendAppException {
        Change change = createEntity(Change.class, 1L);
        populateChange(change);

        TypedQuery mockQuery = createTypedQueryMock(change);
        mockCriteriaQuery(Change.class, mockQuery, Change.class);

        Change result = ejb.getChangeByCommitId(change.getCommitId());

        Assert.assertEquals(result, change);
        Assert.assertEquals(result.getCommitId(), change.getCommitId());
    }
}
