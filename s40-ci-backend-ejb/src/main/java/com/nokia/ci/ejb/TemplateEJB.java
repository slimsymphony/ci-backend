/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Template;
import com.nokia.ci.ejb.model.TemplateCustomVerification;
import com.nokia.ci.ejb.model.TemplateCustomVerificationConf;
import com.nokia.ci.ejb.model.TemplateVerificationConf;
import com.nokia.ci.ejb.model.Template_;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.util.RelationUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class TemplateEJB extends CrudFunctionality<Template> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(TemplateEJB.class);

    public TemplateEJB() {
        super(Template.class);
    }

    public List<TemplateVerificationConf> getVerificationConfs(Long id) {
        return getJoinList(id, Template_.verificationConfs);
    }

    public List<Verification> getVerifications(Long id) throws NotFoundException {
        Template template = read(id);
        
        List<Verification> verifications = new ArrayList<Verification>();
        for (TemplateVerificationConf tvc : template.getVerificationConfs()) {
            if (verifications.contains(tvc.getVerification())) {
                continue;
            }
            verifications.add(tvc.getVerification());
        }

        for (TemplateCustomVerification tcv : template.getCustomVerifications()) {
            if (verifications.contains(tcv.getVerification())) {
                continue;
            }
            verifications.add(tcv.getVerification());
        }

        return verifications;
    }

    public List<Product> getProducts(Long id) throws NotFoundException {
        Template template = read(id);
        
        List<Product> products = new ArrayList<Product>();
        for (TemplateVerificationConf tvc : template.getVerificationConfs()) {
            if (products.contains(tvc.getProduct())) {
                continue;
            }
            products.add(tvc.getProduct());
        }

        for (TemplateCustomVerification tcv : template.getCustomVerifications()) {
            for (TemplateCustomVerificationConf tcvc : tcv.getCustomVerificationConfs()) {
                if (products.contains(tcvc.getProduct())) {
                    continue;
                }
                products.add(tcvc.getProduct());
            }
        }
        
        return products;
    }

    public List<TemplateCustomVerification> getCustomVerifications(Long id) {
        return getJoinList(id, Template_.customVerifications);
    }

    public Template saveVerificationConfs(Long id, List<TemplateVerificationConf> confs) throws NotFoundException {
        Template t = read(id);
        for (TemplateVerificationConf conf : t.getVerificationConfs()) {
            em.remove(conf);
        }
        t.getVerificationConfs().clear();

        log.info("Saving verification configurations for template {}", t);
        for (TemplateVerificationConf tvc : confs) {
            RelationUtil.relate(t, tvc);
        }

        t.setModifiedBy(getCallerUsername());

        return em.merge(t);
    }
}
