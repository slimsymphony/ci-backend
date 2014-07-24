package com.nokia.ci.client.model;

import com.nokia.ci.client.exception.ViewTransformException;
import java.util.Arrays;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Unit tests for client model transformations.
 * @author vrouvine
 */
public class ModelTransformTest {

    @Test
    public void fullDummyViewTransformToDummy() throws Exception {
        FullDummyView dummyView = createDummyView();
        Dummy dummy = dummyView.transformTo(Dummy.class);
        Assert.assertNotNull(dummy);
        Assert.assertEquals(dummyView.getId(), dummy.getId());
        Assert.assertEquals(dummyView.getName(), dummy.getName());
        Assert.assertEquals(dummyView.getAddress(), dummy.getAddress());
        Assert.assertEquals(dummyView.getDummyEnum(), dummy.getDummyEnum());
    }

    @Test
    public void fullDummyViewCopyValuesFromDummy() throws Exception {
        Dummy dummy = createDummy();
        FullDummyView dummyView = new FullDummyView();
        dummyView.copyValuesFrom(dummy);

        Assert.assertEquals(dummy.getId(), dummyView.getId());
        Assert.assertEquals(dummy.getName(), dummyView.getName());
        Assert.assertEquals(dummy.getAddress(), dummyView.getAddress());
        Assert.assertEquals(dummy.getDummyEnum(), dummyView.getDummyEnum());
    }

    @Test(expected = ViewTransformException.class)
    public void fullDummyViewCopyValuesFromNull() throws ViewTransformException {
        FullDummyView dummyView = new FullDummyView();
        dummyView.copyValuesFrom(null);
    }

    @Test
    public void partialDummyViewTransformToDummy() throws Exception {
        PartialDummyView dummyView = createPartialDummyView();
        Dummy dummy = dummyView.transformTo(Dummy.class);
        Assert.assertNotNull(dummy);
        // Id field value should not be transformed because it is not annotated.
        Assert.assertNull("Id should be null!", dummy.getId());
        Assert.assertEquals(dummyView.getName(), dummy.getName());
        Assert.assertEquals(dummyView.getAddress(), dummy.getAddress());
    }

    @Test
    public void partialDummyViewCopyValuesFromDummy() throws Exception {
        Dummy dummy = createDummy();
        PartialDummyView dummyView = new PartialDummyView();
        dummyView.copyValuesFrom(dummy);
        // Id field value should not be copied because it is not annotated.
        Assert.assertNull("Id should be null!", dummyView.getId());
        Assert.assertEquals(dummy.getName(), dummyView.getName());
        Assert.assertEquals(dummy.getAddress(), dummyView.getAddress());
    }
    
    @Test(expected = ViewTransformException.class)
    public void invalidFieldTypeDummyViewCopyValuesFromDummy() throws ViewTransformException {
        Dummy dummy = createDummy();
        InvalidFieldTypeDummyView invalidDummy = new InvalidFieldTypeDummyView();
        invalidDummy.copyValuesFrom(dummy);
    }
    
    @Test(expected = ViewTransformException.class)
    public void abstractDummy() {
        FullDummyView dummy = new FullDummyView();
        dummy.transformTo(AbstractDummy.class);
    }
    
    @Test(expected = ViewTransformException.class)
    public void dummyWithoutDefaultConstructor() {
        FullDummyView dummy = new FullDummyView();
        dummy.transformTo(DummyPrivateConstructor.class);
    }
    
    @Test(expected = ViewTransformException.class)
    public void illegalAccessFieldDummyView() {
        Dummy dummy = createDummy();
        IllegalFieldAccessDummyView dummyView = new IllegalFieldAccessDummyView();
        dummyView.copyValuesFrom(dummy);
    }

    private FullDummyView createDummyView() {
        FullDummyView dummyView = new FullDummyView();
        dummyView.setId(1L);
        dummyView.setName("Name");
        dummyView.setAddress("Address");
        dummyView.setDummyEnum(DummyEnum.DUMMY);
        return dummyView;
    }

    private PartialDummyView createPartialDummyView() {
        PartialDummyView dummyView = new PartialDummyView();
        dummyView.setId(1L);
        dummyView.setName("Name");
        dummyView.setAddress("Address");
        return dummyView;
    }

    private Dummy createDummy() {
        Dummy dummy = new Dummy();
        dummy.setId(2L);
        dummy.setName("Name 2");
        dummy.setAddress("Address 2");
        dummy.setList(Arrays.asList(2, 3, 4, 5));
        dummy.setDummyEnum(DummyEnum.DUMMY);
        return dummy;
    }
}
