package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.CustomParamEJB;
import com.nokia.ci.ejb.ProductEJB;
import com.nokia.ci.ejb.TemplateCustomVerificationEJB;
import com.nokia.ci.ejb.TemplateCustomVerificationParamEJB;
import com.nokia.ci.ejb.TemplateEJB;
import com.nokia.ci.ejb.VerificationEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CustomParam;
import com.nokia.ci.ejb.model.CustomParamValue;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Template;
import com.nokia.ci.ejb.model.TemplateCustomVerification;
import com.nokia.ci.ejb.model.TemplateCustomVerificationConf;
import com.nokia.ci.ejb.model.TemplateCustomVerificationParam;
import com.nokia.ci.ejb.model.TemplateVerificationConf;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.util.RelationUtil;
import com.nokia.ci.ui.model.VerificationConfCell;
import com.nokia.ci.ui.model.VerificationConfRow;
import com.nokia.ci.ui.util.UIVerificationConfUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for template.
 *
 * @author jajuutin
 */
@Named
@ViewScoped
public class TemplateBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(TemplateBean.class);
    private Template template;
    private DualListModel<Product> products;
    private DualListModel<Verification> verifications;
    private List<VerificationConfRow> verificationConfRows;
    private TemplateCustomVerification selectedCustomVerification;
    private TemplateCustomVerificationParam customParameter;
    private Map<TemplateCustomVerificationParam, List<SelectItem>> customParamValueItemsMap;
    @Inject
    private TemplateEJB templateEJB;
    @Inject
    private VerificationEJB verificationEJB;
    @Inject
    private ProductEJB productEJB;
    @Inject
    private TemplateCustomVerificationEJB templateCustomVerificationEJB;
    @Inject
    private CustomParamEJB customParamEJB;
    @Inject
    private TemplateCustomVerificationParamEJB templateCustomVerificationParamEJB;

    @Override
    protected void init() throws NotFoundException {
        String templateId = getQueryParam("templateId");

        if (templateId == null) {
            template = new Template();
        } else {
            log.debug("Finding template {} for editing!", template);
            template = templateEJB.read(Long.parseLong(templateId));
        }
        initCustomVerifications();
        initProducts();
        initVerifications();
        initVerificationConfRows();
    }

    private void initCustomVerifications() {
        if (template.getId() == null) {
            return;
        }
        template.setCustomVerifications(templateEJB.getCustomVerifications(template.getId()));
    }

    private void initVerifications() throws NotFoundException {
        verifications = new DualListModel<Verification>();
        List<Verification> availableVerifications = verificationEJB.readAll();
        if (template.getId() == null) {
            verifications.setSource(availableVerifications);
            return;
        }
        List<Verification> templateVerifications = templateEJB.getVerifications(template.getId());
        availableVerifications.removeAll(templateVerifications);
        verifications.setSource(availableVerifications);
        verifications.setTarget(templateVerifications);
    }

    private void initProducts() throws NotFoundException {
        products = new DualListModel<Product>();
        List<Product> availableProducts = productEJB.readAll();
        if (template.getId() == null) {
            products.setSource(availableProducts);
            return;
        }
        List<Product> templateProducts = templateEJB.getProducts(template.getId());
        availableProducts.removeAll(templateProducts);
        products.setSource(availableProducts);
        products.setTarget(templateProducts);
    }

    private void initVerificationConfRows() {
        List<TemplateVerificationConf> confs = new ArrayList<TemplateVerificationConf>();

        if (template != null) {
            confs = templateEJB.getVerificationConfs(template.getId());
        }

        verificationConfRows = UIVerificationConfUtil.populateTemplateVerificationConfRows(
                products.getTarget(), verifications.getTarget(), confs);

        List<VerificationConfRow> customVerificationConfRows = getCustomVerificationConfRows();
        verificationConfRows.addAll(customVerificationConfRows);
    }

    private List<VerificationConfRow> getCustomVerificationConfRows() {
        List<VerificationConfRow> customConfRows = new ArrayList<VerificationConfRow>();
        if (template.getId() == null) {
            return customConfRows;
        }
        List<TemplateCustomVerification> customVerifications = templateEJB.getCustomVerifications(template.getId());
        for (TemplateCustomVerification customVerification : customVerifications) {
            List<TemplateCustomVerificationConf> customVerificationConfs = templateCustomVerificationEJB.getCustomVerificationConfs(customVerification.getId());
            List<TemplateCustomVerificationParam> customVerificationParams = templateCustomVerificationEJB.getCustomVerificationParams(customVerification.getId());
            customVerification.setCustomVerificationParams(customVerificationParams);
            VerificationConfRow row = UIVerificationConfUtil.populateCustomVerificationRow(products.getTarget(), customVerification, null, null, customVerificationConfs);
            customConfRows.add(row);
        }
        return customConfRows;
    }

    public String save() throws NotFoundException {
        log.debug("Save triggered!");

        if (template.getId() != null) {
            log.debug("Updating existing template {}", template);
            try {
                template = templateEJB.update(template);
            } catch (NotFoundException e) {
                log.warn("Can not save template {}! Cause: {}", template, e.getMessage());
                addMessage(FacesMessage.SEVERITY_ERROR, "Template could not be saved!", "");
                return null;
            }
        } else {
            log.debug("Saving new template!");

            // Remove default paramters (param value matches VerificationConfRow.DEFAULT_PARAM_VALUE)
            for (TemplateCustomVerification tcv : template.getCustomVerifications()) {
                removeDefaultParameters(tcv.getCustomVerificationParams());
            }

            templateEJB.create(template);
        }

        saveVerificationConfs();

        return "templates?faces-redirect=true";
    }

    private boolean saveVerificationConfs() throws NotFoundException {
        log.debug("Saving verification confs for template {}", template);
        if (template == null || template.getId() == null) {
            return false;
        }

        List<TemplateVerificationConf> confs = new ArrayList<TemplateVerificationConf>();

        for (VerificationConfRow row : verificationConfRows) {
            List<TemplateCustomVerificationConf> customConfs = new ArrayList<TemplateCustomVerificationConf>();
            for (VerificationConfCell cell : row.getCells()) {
                if (!cell.isSelected()) {
                    continue;
                }
                if (row.isCustom()) {
                    addCustomVerificationConf(cell, customConfs);
                    continue;
                }
                addVerificationConf(cell, confs);
            }

            if (row.isCustom()) {
                templateCustomVerificationEJB.saveCustomVerificationConfs(row.getCustomVerification().getId(), customConfs);
            }
        }

        templateEJB.saveVerificationConfs(template.getId(), confs);

        return true;
    }

    private void addVerificationConf(VerificationConfCell cell, List<TemplateVerificationConf> confs) {
        TemplateVerificationConf conf = new TemplateVerificationConf();
        Product p = new Product();
        p.setId(cell.getProductId());
        Verification v = new Verification();
        v.setId(cell.getVerificationId());
        conf.setProduct(p);
        conf.setVerification(v);
        conf.setCardinality(cell.getCardinality());
        confs.add(conf);
    }

    private void addCustomVerificationConf(VerificationConfCell cell, List<TemplateCustomVerificationConf> customConfs) {
        TemplateCustomVerificationConf customConf = new TemplateCustomVerificationConf();
        Product p = new Product();
        p.setId(cell.getProductId());
        customConf.setProduct(p);
        customConf.setCardinality(cell.getCardinality());
        customConfs.add(customConf);
    }

    public void saveCustomVerification() throws NotFoundException {
        if (!template.getCustomVerifications().contains(selectedCustomVerification)) {
            RelationUtil.relate(template, selectedCustomVerification);
            List<TemplateCustomVerificationConf> customConfs = new ArrayList<TemplateCustomVerificationConf>();
            if (selectedCustomVerification.getId() != null) {
                customConfs = templateCustomVerificationEJB.getCustomVerificationConfs(selectedCustomVerification.getId());
            }
            VerificationConfRow row = UIVerificationConfUtil.populateCustomVerificationRow(products.getTarget(), selectedCustomVerification, null, null, customConfs);
            verificationConfRows.add(row);
        }

        if (template.getId() == null) {
            return;
        }

        removeDefaultParameters(selectedCustomVerification.getCustomVerificationParams());
        if (selectedCustomVerification.getId() == null) {
            templateCustomVerificationEJB.create(selectedCustomVerification);
            return;
        }
        templateCustomVerificationEJB.update(selectedCustomVerification);
    }

    private void removeDefaultParameters(List<TemplateCustomVerificationParam> customVerificationParams) throws NotFoundException {
        List<TemplateCustomVerificationParam> removed = new ArrayList<TemplateCustomVerificationParam>();
        for (TemplateCustomVerificationParam param : customVerificationParams) {
            if (VerificationConfRow.DEFAULT_PARAM_VALUE.equals(param.getParamValue())) {
                removed.add(param);
                if (param.getId() != null) {
                    templateCustomVerificationParamEJB.delete(param);
                }
            }

            if (VerificationConfRow.EMPTY_PARAM_VALUE.equals(param.getParamValue())) {
                param.setParamValue("");
            }
        }
        customVerificationParams.removeAll(removed);
    }

    public String cancelEdit() {
        return "templates?faces-redirect=true";
    }

    public void updateVerificationConfTable(ActionEvent event) throws NotFoundException {
        initVerificationConfRows();
    }

    public void selectAllVerifications() {
        for (VerificationConfRow row : verificationConfRows) {
            for (VerificationConfCell cell : row.getCells()) {
                cell.setSelected(true);
            }
        }
    }

    public void clearAllVerifications() {
        for (VerificationConfRow row : verificationConfRows) {
            for (VerificationConfCell cell : row.getCells()) {
                cell.setSelected(false);
            }
        }
    }

    public void deleteCustomVerification(VerificationConfRow row) throws NotFoundException {
        verificationConfRows.remove(row);
        TemplateCustomVerification customVerification = (TemplateCustomVerification) row.getCustomVerification();
        template.getCustomVerifications().remove(customVerification);
        if (customVerification.getId() != null) {
            templateCustomVerificationEJB.delete(customVerification);
        }
    }

    public void selectCustomVerification(VerificationConfRow row) throws NotFoundException {
        selectedCustomVerification = (TemplateCustomVerification) row.getCustomVerification();
        if (selectedCustomVerification.getId() != null) {
            initCustomVerificationParams();
            return;
        }
        initCustomParameterValueItemsMap();
    }

    public void addCustomVerification() throws NotFoundException {
        selectedCustomVerification = new TemplateCustomVerification();
        selectedCustomVerification.setVerification(verifications.getTarget().get(0));
        initCustomVerificationParams();
    }

    private void initCustomVerificationParams() {
        List<TemplateCustomVerificationParam> customVerificationParams = new ArrayList<TemplateCustomVerificationParam>();
        selectedCustomVerification.setCustomVerificationParams(customVerificationParams);
        if (selectedCustomVerification.getId() != null) {
            customVerificationParams = templateCustomVerificationEJB.getCustomVerificationParams(selectedCustomVerification.getId());
        }

        List<CustomParam> customParams = verificationEJB.getCustomParams(selectedCustomVerification.getVerification().getId());
        for (CustomParam param : customParams) {
            List<CustomParamValue> customParamValues = customParamEJB.getCustomParamValues(param.getId());
            param.setCustomParamValues(customParamValues);
            TemplateCustomVerificationParam customVerificationParam = findCustomVerificationParam(customVerificationParams, param);
            customVerificationParam.setCustomParam(param);
            selectedCustomVerification.getCustomVerificationParams().add(customVerificationParam);
        }
        initCustomParameterValueItemsMap();
    }

    private TemplateCustomVerificationParam findCustomVerificationParam(List<TemplateCustomVerificationParam> customVerificationParams, CustomParam param) {
        for (TemplateCustomVerificationParam cvp : customVerificationParams) {
            if (param.equals(cvp.getCustomParam())) {
                return cvp;
            }
        }
        TemplateCustomVerificationParam customVerificationParam = new TemplateCustomVerificationParam();
        customVerificationParam.setCustomVerification(selectedCustomVerification);
        return customVerificationParam;
    }

    private void initCustomParameterValueItemsMap() {
        customParamValueItemsMap = new HashMap<TemplateCustomVerificationParam, List<SelectItem>>();
        for (TemplateCustomVerificationParam cvp : selectedCustomVerification.getCustomVerificationParams()) {
            List<SelectItem> items = new ArrayList<SelectItem>();
            SelectItem defaultItem = new SelectItem(VerificationConfRow.DEFAULT_PARAM_VALUE, VerificationConfRow.DEFAULT_PARAM_VALUE);
            items.add(defaultItem);

            boolean matchFound = false;
            for (CustomParamValue value : cvp.getCustomParam().getCustomParamValues()) {
                if (cvp.getParamValue() == null || cvp.getParamValue().equals(value.getParamValue())) {
                    matchFound = true;
                }
                SelectItem item = new SelectItem(value.getParamValue(), value.getParamValue());
                if (StringUtils.isEmpty(value.getParamValue())) {
                    // Workaround for PrimeFaces bug https://code.google.com/p/primefaces/issues/detail?id=5624
                    item.setLabel(VerificationConfRow.EMPTY_PARAM_VALUE);
                    item.setValue(VerificationConfRow.EMPTY_PARAM_VALUE);
                }

                items.add(item);
            }

            if (!matchFound && cvp.getParamValue() != null) {
                SelectItem matched = new SelectItem(cvp.getParamValue(), cvp.getParamValue());
                if ("".equals(cvp.getParamValue())) {
                    matched.setLabel(VerificationConfRow.EMPTY_PARAM_VALUE);
                    matched.setValue(VerificationConfRow.EMPTY_PARAM_VALUE);
                }
                items.add(1, matched);
            }

            if (cvp.getParamValue() == null) {
                cvp.setParamValue(VerificationConfRow.DEFAULT_PARAM_VALUE);
            } else if ("".equals(cvp.getParamValue())) {
                cvp.setParamValue(VerificationConfRow.EMPTY_PARAM_VALUE);
            }

            customParamValueItemsMap.put(cvp, items);
        }
    }

    public void createCustomParameter() {
        customParameter = new TemplateCustomVerificationParam();
    }

    public void verificationChanged() throws NotFoundException {
        selectedCustomVerification.getCustomVerificationParams().clear();
        initCustomVerificationParams();
    }

    public List<SelectItem> fetchCustomParamValueItems(TemplateCustomVerificationParam param) {
        return customParamValueItemsMap.get(param);
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public List<VerificationConfRow> getVerificationConfRows() {
        return verificationConfRows;
    }

    public void setVerificationConfRows(List<VerificationConfRow> verificationConfRows) {
        this.verificationConfRows = verificationConfRows;
    }

    public DualListModel<Product> getProducts() {
        return products;
    }

    public void setProducts(DualListModel<Product> products) {
        this.products = products;
    }

    public DualListModel<Verification> getVerifications() {
        return verifications;
    }

    public void setVerifications(DualListModel<Verification> verifications) {
        this.verifications = verifications;
    }

    public TemplateCustomVerification getSelectedCustomVerification() {
        return selectedCustomVerification;
    }

    public void setSelectedCustomVerification(TemplateCustomVerification selectedCustomVerification) {
        this.selectedCustomVerification = selectedCustomVerification;
    }

    public Map<TemplateCustomVerificationParam, List<SelectItem>> getCustomParamValueItemsMap() {
        return customParamValueItemsMap;
    }

    public void setCustomParamValueItemsMap(Map<TemplateCustomVerificationParam, List<SelectItem>> customParamValueItemsMap) {
        this.customParamValueItemsMap = customParamValueItemsMap;
    }

    public TemplateCustomVerificationParam getCustomParameter() {
        return customParameter;
    }

    public void setCustomParameter(TemplateCustomVerificationParam customParameter) {
        this.customParameter = customParameter;
    }
}
