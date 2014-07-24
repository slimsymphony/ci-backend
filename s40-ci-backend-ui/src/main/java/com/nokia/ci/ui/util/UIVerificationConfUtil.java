package com.nokia.ci.ui.util;

import com.nokia.ci.ejb.model.AbstractCustomVerification;
import com.nokia.ci.ejb.model.AbstractCustomVerificationConf;
import com.nokia.ci.ejb.model.AbstractTemplateVerificationConf;
import com.nokia.ci.ejb.model.BranchVerificationConf;
import com.nokia.ci.ejb.model.JobVerificationConf;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.TemplateVerificationConf;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.VerificationCardinality;
import com.nokia.ci.ejb.model.VerificationTargetPlatform;
import com.nokia.ci.ejb.model.VerificationType;
import com.nokia.ci.ejb.model.AbstractVerificationConf;
import com.nokia.ci.ejb.util.VerificationConfUtil;
import com.nokia.ci.ui.model.VerificationConfCell;
import com.nokia.ci.ui.model.VerificationConfRow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class for verification configuration handling in UI.
 *
 * @author vrouvine
 */
public class UIVerificationConfUtil {

    public static List<VerificationConfRow> populateJobVerificationConfRows(List<Product> products, List<Verification> verifications,
            List<ProjectVerificationConf> projectConfs, List<BranchVerificationConf> branchConfs, List<JobVerificationConf> jobConfs, List<TemplateVerificationConf> templateConfs) {

        List<? extends AbstractVerificationConf> enabledConfs = branchConfs == null ? projectConfs : VerificationConfUtil.getEnabledConfs(projectConfs, branchConfs);
        List<AbstractVerificationConf> selectedConfs = new ArrayList<AbstractVerificationConf>();
        selectedConfs.addAll(jobConfs);
        if (templateConfs != null && !templateConfs.isEmpty()) {
            for (AbstractVerificationConf conf : templateConfs) {
                if (!VerificationConfUtil.isCombinationSelected(enabledConfs,
                        conf.getProduct(), conf.getVerification())) {
                    // Template conf not enabled in project/branch level
                    continue;
                }
                if (VerificationConfUtil.isCombinationSelected(selectedConfs,
                        conf.getProduct(), conf.getVerification())) {
                    // Template conf is already selected in job level
                    continue;
                }
                selectedConfs.add(conf);
            }
            // Update enabled confs list so that user won't be able to remove template conf selections 
            enabledConfs.removeAll(VerificationConfUtil.getEnabledConfs(enabledConfs, templateConfs));
        }
        
        return populateVerificationConfRows(products, verifications, enabledConfs, selectedConfs, templateConfs, true);
    }

    public static List<VerificationConfRow> populateBranchVerificationConfRows(List<Product> products, List<Verification> verifications,
            List<ProjectVerificationConf> projectConfs, List<BranchVerificationConf> branchConfs) {

        return populateVerificationConfRows(products, verifications, projectConfs, branchConfs, null, false);
    }

    public static List<VerificationConfRow> populateProjectVerificationConfRows(List<Product> products, List<Verification> verifications,
            List<ProjectVerificationConf> projectConfs) {

        return populateVerificationConfRows(products, verifications, null, projectConfs, null, false);
    }

    public static List<VerificationConfRow> populateTemplateVerificationConfRows(List<Product> products, List<Verification> verifications,
            List<TemplateVerificationConf> verificationConfs) {

        return populateVerificationConfRows(products, verifications, null, verificationConfs, verificationConfs, true);
    }    
    
    private static List<VerificationConfRow> populateVerificationConfRows(List<Product> products, List<Verification> verifications,
            List<? extends AbstractVerificationConf> enabledConfs, List<? extends AbstractVerificationConf> selectedConfs, 
            List<? extends AbstractVerificationConf> templateConfs, boolean isJobVerificationConf) {

        List<VerificationConfRow> rows = new ArrayList<VerificationConfRow>();
        for (Verification v : verifications) {
            if (v.getType() != VerificationType.NORMAL) {
                continue;
            }
            VerificationConfRow row = new VerificationConfRow();
            row.setRowLabel(v.getDisplayName());
            row.setIndevice(VerificationTargetPlatform.INDEVICE.equals(v.getTargetPlatform()));
            List<VerificationConfCell> cells = new ArrayList<VerificationConfCell>();
            for (Product p : products) {
                boolean cellEnabled = enabledConfs == null ? true : VerificationConfUtil.isCombinationSelected(enabledConfs, p, v);
                boolean cellSelected = selectedConfs == null ? true : VerificationConfUtil.isCombinationSelected(selectedConfs, p, v);
                boolean cellTemplate = templateConfs == null ? true : VerificationConfUtil.isCombinationSelected(templateConfs, p, v);
                // Remove selection if the cell is not enabled and not template
                if (cellSelected && !cellEnabled && !cellTemplate) {
                    cellSelected = false;
                }
                VerificationCardinality cardinality = null;
                if (isJobVerificationConf) {
                    cardinality = VerificationConfUtil.getVerificationCardinality(selectedConfs, p, v);
                }
                cells.add(createVerificationConfCell(p, v, cellEnabled, cellSelected, cellTemplate, cardinality));
            }
            row.setCells(cells);
            rows.add(row);
        }
        return rows;
    }

    public static VerificationConfRow populateCustomVerificationRow(List<Product> products, AbstractCustomVerification customVerification,
            List<ProjectVerificationConf> projectConfs, List<BranchVerificationConf> branchConfs, List<? extends AbstractCustomVerificationConf> customConfs) {
        List<? extends AbstractVerificationConf> enabledConfs = branchConfs == null ? projectConfs : VerificationConfUtil.getEnabledConfs(projectConfs, branchConfs);
        VerificationConfRow row = new VerificationConfRow();
        row.setCustom(true);
        row.setTemplate(false);
        row.setCustomVerification(customVerification);
        row.setRowLabel(customVerification.getVerification().getDisplayName());
        row.setIndevice(VerificationTargetPlatform.INDEVICE.equals(customVerification.getVerification().getTargetPlatform()));
        List<VerificationConfCell> cells = new ArrayList<VerificationConfCell>();
        for (Product p : products) {
            boolean cellEnabled = true;
            if(enabledConfs != null) {
                cellEnabled = VerificationConfUtil.isCombinationSelected(enabledConfs, p, customVerification.getVerification());
            }
            
            boolean cellSelected = cellEnabled && VerificationConfUtil.isCustomCombinationSelected(customConfs, p);
            VerificationCardinality cardinality = VerificationConfUtil.getCustomVerificationCardinality(customConfs, p);
            cells.add(createVerificationConfCell(p, customVerification.getVerification(), cellEnabled, cellSelected, false, cardinality));
        }
        row.setCells(cells);

        return row;
    }

    public static VerificationConfRow populateTemplateCustomVerificationRow(List<Product> products, AbstractCustomVerification customVerification,
            List<ProjectVerificationConf> projectConfs, List<BranchVerificationConf> branchConfs, List<? extends AbstractCustomVerificationConf> templateConfs) {
        List<? extends AbstractVerificationConf> enabledConfs = branchConfs == null ? projectConfs : VerificationConfUtil.getEnabledConfs(projectConfs, branchConfs);
        VerificationConfRow row = new VerificationConfRow();
        row.setCustom(true);
        row.setTemplate(true);
        row.setCustomVerification(customVerification);
        row.setRowLabel(customVerification.getVerification().getDisplayName());
        row.setIndevice(VerificationTargetPlatform.INDEVICE.equals(customVerification.getVerification().getTargetPlatform()));
        List<VerificationConfCell> cells = new ArrayList<VerificationConfCell>();
        for (Product p : products) {
            boolean cellEnabled = false;
            if(enabledConfs != null) {
                cellEnabled = VerificationConfUtil.isCombinationSelected(enabledConfs, p, customVerification.getVerification());
            }
            
            boolean cellSelected = VerificationConfUtil.isCustomCombinationSelected(templateConfs, p);
            VerificationCardinality cardinality = VerificationConfUtil.getCustomVerificationCardinality(templateConfs, p);
            cells.add(createVerificationConfCell(p, customVerification.getVerification(), false, cellSelected && cellEnabled, true, cardinality));
        }
        row.setCells(cells);

        return row;
    }
    
    private static VerificationConfCell createVerificationConfCell(Product p,
            Verification v,
            boolean cellEnabled,
            boolean cellSelected,
            boolean cellTemplate,
            VerificationCardinality cardinality) {
        VerificationConfCell cell = new VerificationConfCell();
        cell.setProductId(p.getId());
        cell.setRmCode(p.getRmCode());
        cell.setVerificationId(v.getId());
        cell.setEnabled(cellEnabled);
        cell.setSelected(cellSelected);
        cell.setTemplate(cellTemplate);
        if (cardinality == null) {
            cardinality = VerificationCardinality.MANDATORY;
        }
        cell.setCardinality(cardinality);
        return cell;
    }
}
