package com.nokia.ci.ejb.model;

import com.nokia.ci.ejb.CITestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link BaseEntity}.
 * @author jajuutin
 */
public class BaseEntityTest extends CITestBase {

    private final Long defaultId = Long.valueOf(1);

    @Test
    public void getId() {
        BaseEntity impl = createEntity(BaseEntityImpl.class, defaultId);
        Assert.assertEquals(impl.getId(), defaultId);
    }

    @Test
    public void prePersist() {
        BaseEntity impl = createEntity(BaseEntityImpl.class, defaultId);
        long before = getTime();
        impl.prePersist();
        long after = getTime();
        Assert.assertNotNull(impl.getCreated());
        Assert.assertNull(impl.getModified());
        assertBetween(impl.getCreated().getTime(), before, after);
    }

    @Test
    public void preUpdate() {
        BaseEntity impl = new BaseEntityImpl();
        impl.setId(defaultId);        
        long before = getTime();
        impl.preUpdate();
        long after = getTime();
        Assert.assertNull(impl.getCreated());
        Assert.assertNotNull(impl.getModified());
        assertBetween(impl.getModified().getTime(), before, after);
    }

    @Test
    public void equality() {
        // Same ids = TRUE.
        BaseEntity base = createEntity(BaseEntityImpl.class, defaultId);
        BaseEntity other = createEntity(BaseEntityImpl.class, defaultId);

        Assert.assertTrue("Same ids should equal.", base.equals(other));

        // Different ids = FALSE
        other = createEntity(BaseEntityImpl.class, System.currentTimeMillis());
        Assert.assertFalse("Different ids should not equal.", base.equals(other));

        // Null id in base = FALSE
        base = createEntity(BaseEntityImpl.class, null);
        Assert.assertFalse("Null id in base should not equal.", base.equals(other));

        // Null id in both = FALSE
        other = createEntity(BaseEntityImpl.class, null);
        Assert.assertFalse("Null id in both should not equal.", base.equals(other));

        // Null ids but same reference = TRUE
        other = base;
        Assert.assertTrue("Null id in both and same reference should equal.", base.equals(other));

        // Null id in other = FALSE
        base = createEntity(BaseEntityImpl.class, defaultId);
        Assert.assertFalse("Null id in other should not equal.", base.equals(other));

        // Not instance of BaseEntity = FALSE
        Assert.assertFalse("Not instance of super class should not equal.", base.equals(Integer.valueOf(3)));
    }

    @Test
    public void hashing() {
        Long entityId = 1024L;

        BaseEntityImpl entityImpl1 =
                createEntity(BaseEntityImpl.class, entityId);
        BaseEntityImpl entityImpl2 =
                createEntity(BaseEntityImpl.class, entityId);

        Assert.assertEquals(entityImpl1.hashCode(), entityImpl2.hashCode());
    }

    @Test
    public void hashingNull() {
        BaseEntityImpl entityImpl = createEntity(BaseEntityImpl.class, null);
        Assert.assertTrue(entityImpl.hashCode() == 0);
    }

    /**
     * Asserts true if given value is between 'min' and 'max' values.
     * Values of 'min' and 'max' are accepted values.
     * @param value Value to assert
     * @param min Minimum accepted value
     * @param max Maximum accepted value
     */
    private void assertBetween(long value, long min, long max) {
        Assert.assertTrue(value >= min && value <= max);
    }

    private long getTime() {
        return System.currentTimeMillis();
    }
}
