package com.nokia.ci.ejb.util;

import com.nokia.ci.ejb.CITestBase;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.Verification;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link VerificationConfUtil} class.
 * 
 * @author vrouvine
 */
public class VerificationConfUtilTest extends CITestBase {
    
    @Test
    public void isCompinationSelectedProjectConfs() {
        List<ProjectVerificationConf> projectConfs = createEntityList(ProjectVerificationConf.class, 5);
        populateProjectVerificationConfs(projectConfs, null);
        Product notMatchingProduct = new Product();
        notMatchingProduct.setId(Long.MIN_VALUE);
        
        Verification notMatchingVerification = new Verification();
        notMatchingVerification.setId(Long.MIN_VALUE);
        
        Product matchingProduct = createEntity(Product.class, 1L);
        Verification matchingVerification = createEntity(Verification.class, 1L);
        
        Assert.assertFalse(VerificationConfUtil.isCombinationSelected(null, null, null));
        Assert.assertFalse(VerificationConfUtil.isCombinationSelected(projectConfs, null, null));
        Assert.assertFalse(VerificationConfUtil.isCombinationSelected(projectConfs, notMatchingProduct, notMatchingVerification));
        Assert.assertFalse(VerificationConfUtil.isCombinationSelected(projectConfs, matchingProduct, notMatchingVerification));
        Assert.assertFalse(VerificationConfUtil.isCombinationSelected(projectConfs, notMatchingProduct, matchingVerification));
        Assert.assertTrue(VerificationConfUtil.isCombinationSelected(projectConfs, matchingProduct, matchingVerification));
    }

    private List<ProjectVerificationConf> createProjectVerificationConfs(int size) {
        List<ProjectVerificationConf> confs = new ArrayList<ProjectVerificationConf>();
        List<Product> products = createEntityList(Product.class, size);
        List<Verification> verifications = createEntityList(Verification.class, size);
        for (Product p : products) {
            for (Verification v : verifications) {
                ProjectVerificationConf projectVerificationConf = new ProjectVerificationConf();
                projectVerificationConf.setProduct(p);
                projectVerificationConf.setVerification(v);
                confs.add(projectVerificationConf);
            }
        }
        return confs;
    }
}
