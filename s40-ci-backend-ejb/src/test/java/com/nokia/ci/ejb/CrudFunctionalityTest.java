/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BaseEntityImpl;
import com.nokia.ci.ejb.model.ChildEntity;
import java.util.List;
import java.util.ListIterator;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.ListAttribute;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 *
 * @author jajuutin
 */
public class CrudFunctionalityTest extends EJBTestBase {

    private CrudFunctionalityImpl crudImpl;

    @Override
    @Before
    public void before() {
        super.before();

        crudImpl = new CrudFunctionalityImpl();
        crudImpl.em = em;
        crudImpl.cb = cb;
        crudImpl.context = context;
    }

    @Test
    public void init() {
        Mockito.when(em.getCriteriaBuilder()).thenReturn(cb);
        crudImpl.init();
        Assert.assertNotNull(crudImpl.cb);
    }

    @Test
    public void create() {
        // setup
        BaseEntityImpl entityImpl = createEntity(BaseEntityImpl.class, 1L);

        // run
        crudImpl.create(entityImpl);

        // verify
        ArgumentCaptor<BaseEntityImpl> persistedEntityImpl = ArgumentCaptor.forClass(BaseEntityImpl.class);
        Mockito.verify(em, Mockito.atLeastOnce()).persist(persistedEntityImpl.capture());
        BaseEntityImpl result = persistedEntityImpl.getValue();
        Assert.assertTrue(entityImpl == result);

        Mockito.verify(em, Mockito.atLeastOnce()).flush();
    }

    @Test
    public void read() throws NotFoundException {
        // setup
        BaseEntityImpl entityImpl = createEntity(BaseEntityImpl.class, 1L);
        Mockito.when(em.find(BaseEntityImpl.class, 1L)).thenReturn(entityImpl);

        // run
        BaseEntityImpl result = crudImpl.read(1L);

        // verify
        Assert.assertTrue(entityImpl == result);
    }

    @Test
    public void readAll() {
        // setup
        List<BaseEntityImpl> entities = createEntityList(BaseEntityImpl.class, 2);
        TypedQuery mockQuery = createTypedQueryMock(entities);

        mockCriteriaQuery(BaseEntityImpl.class, mockQuery, BaseEntityImpl.class);

        // run
        List<BaseEntityImpl> result = crudImpl.readAll();

        // verify
        Assert.assertTrue(entities.get(0) == result.get(0));
        Assert.assertTrue(entities.get(1) == result.get(1));
    }

    @Test
    public void update() throws NotFoundException {
        // setup
        final Long id = 1L;

        BaseEntityImpl originalEntity = createEntity(BaseEntityImpl.class, id);
        originalEntity.setName("original name");

        BaseEntityImpl updateEntity = createEntity(BaseEntityImpl.class, id);
        updateEntity.setName("updated name");

        Mockito.when(em.find(BaseEntityImpl.class, id)).thenReturn(originalEntity);
        Mockito.when(em.merge(updateEntity)).thenReturn(updateEntity);

        // run
        BaseEntityImpl result = crudImpl.update(updateEntity);

        // verify
        Assert.assertNotNull(result);
        Assert.assertTrue(originalEntity.getName().compareTo(
                updateEntity.getName()) != 0);
    }

    @Test
    public void delete() throws NotFoundException {
        // setup
        final Long id = 1L;

        BaseEntityImpl entity = createEntity(BaseEntityImpl.class, id);
        Mockito.when(em.find(BaseEntityImpl.class, id)).thenReturn(entity);

        // run
        crudImpl.delete(entity);

        // verify
        Mockito.verify(em, Mockito.atLeastOnce()).find(BaseEntityImpl.class, id);
        Mockito.verify(em, Mockito.atLeastOnce()).remove(entity);
    }

    @Test
    public void getJoinList() {
        BaseEntityImpl entityImpl = createEntity(BaseEntityImpl.class, 1L);
        Mockito.when(em.find(BaseEntityImpl.class, 1L)).thenReturn(entityImpl);

        List<ChildEntity> childs = createEntityList(ChildEntity.class, 2);
        entityImpl.setChilds(childs);

        TypedQuery typedQueryMock = createTypedQueryMock(childs);
        ListAttribute listAttributeMock = Mockito.mock(ListAttribute.class);
        Mockito.when(listAttributeMock.getBindableJavaType()).thenReturn(ChildEntity.class);

        CriteriaQuery queryMock = Mockito.mock(CriteriaQuery.class);
        ListJoin listJoinMock = Mockito.mock(ListJoin.class);
        Root rootMock = Mockito.mock(Root.class);

        Mockito.when(cb.createQuery(ChildEntity.class)).thenReturn(queryMock);
        Mockito.when(queryMock.from(BaseEntityImpl.class)).thenReturn(rootMock);
        Mockito.when(rootMock.join(listAttributeMock)).thenReturn(listJoinMock);
        Mockito.when(em.createQuery(queryMock)).thenReturn(typedQueryMock);

        List<ChildEntity> results = crudImpl.getJoinList(1L, listAttributeMock);
        Assert.assertEquals(results.size(), childs.size());

        Mockito.verify(cb, Mockito.times(1)).createQuery(ChildEntity.class);
        Mockito.verify(queryMock, Mockito.times(1)).from(BaseEntityImpl.class);
        Mockito.verify(queryMock, Mockito.times(1)).select(listJoinMock);
        Mockito.verify(cb, Mockito.times(1)).equal(rootMock, entityImpl.getId());

        ListIterator<ChildEntity> listIterator = childs.listIterator();
        while (listIterator.hasNext()) {
            int nextIndex = listIterator.nextIndex();
            Assert.assertEquals(listIterator.next(), results.get(nextIndex));
        }
    }
}
