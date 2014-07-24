package com.nokia.ci.ejb.util;

import com.nokia.ci.ejb.model.AbstractCustomVerificationConf;
import com.nokia.ci.ejb.model.CustomVerificationConf;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.VerificationCardinality;
import com.nokia.ci.ejb.model.AbstractVerificationConf;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for handling verification configurations.
 *
 * @author vrouvine
 */
public class VerificationConfUtil {

    public static boolean isCombinationSelected(List<? extends AbstractVerificationConf> confs, Product product, Verification verification) {
        if (confs == null || product == null || verification == null) {
            return false;
        }
        for (AbstractVerificationConf conf : confs) {
            if (conf.getProduct().equals(product) && conf.getVerification().equals(verification)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCustomCombinationSelected(List<? extends AbstractCustomVerificationConf> confs, Product product) {
        if (confs == null || product == null) {
            return false;
        }
        for (AbstractCustomVerificationConf conf : confs) {
            if (conf.getProduct().equals(product)) {
                return true;
            }
        }
        return false;
    }

    public static VerificationCardinality getVerificationCardinality(List<? extends AbstractVerificationConf> confs, Product product, Verification verification) {
        if (confs == null || product == null || verification == null) {
            return VerificationCardinality.MANDATORY;
        }
        for (AbstractVerificationConf conf : confs) {
            if (conf.getProduct().equals(product) && conf.getVerification().equals(verification)) {
                return conf.getCardinality();
            }
        }
        return VerificationCardinality.MANDATORY;
    }

    public static VerificationCardinality getCustomVerificationCardinality(List<? extends AbstractCustomVerificationConf> confs, Product product) {
        if (confs == null || product == null) {
            return VerificationCardinality.OPTIONAL;
        }
        for (AbstractCustomVerificationConf conf : confs) {
            if (conf.getProduct().equals(product)) {
                return conf.getCardinality();
            }
        }
        return VerificationCardinality.OPTIONAL;
    }

    public static List<? extends AbstractVerificationConf> getEnabledConfs(List<? extends AbstractVerificationConf> list1, List<? extends AbstractVerificationConf> list2) {
        List<AbstractVerificationConf> validConfs = new ArrayList<AbstractVerificationConf>();
        for (AbstractVerificationConf conf : list1) {
            if (VerificationConfUtil.isCombinationSelected(list2,
                    conf.getProduct(), conf.getVerification())) {
                validConfs.add(conf);
            }
        }
        return validConfs;
    }
}
