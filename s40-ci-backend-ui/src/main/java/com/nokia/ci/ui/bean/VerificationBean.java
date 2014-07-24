package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.CustomParamEJB;
import com.nokia.ci.ejb.CustomParamValueEJB;
import com.nokia.ci.ejb.InputParamEJB;
import com.nokia.ci.ejb.ResultDetailsParamEJB;
import com.nokia.ci.ejb.SlaveLabelEJB;
import com.nokia.ci.ejb.VerificationEJB;
import com.nokia.ci.ejb.VerificationFailureReasonEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.CustomParam;
import com.nokia.ci.ejb.model.CustomParamValue;
import com.nokia.ci.ejb.model.InputParam;
import com.nokia.ci.ejb.model.ResultDetailsParam;
import com.nokia.ci.ejb.model.SlaveLabel;
import com.nokia.ci.ejb.model.TestResultType;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.VerificationFailureReason;
import com.nokia.ci.ejb.model.VerificationFailureReasonSeverity;
import com.nokia.ci.ejb.model.VerificationType;
import com.nokia.ci.ejb.model.VerificationTargetPlatform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for verification.
 *
 * @author vrouvine
 */
@Named
@ViewScoped
public class VerificationBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(VerificationBean.class);
    private Verification verification;
    private CustomParam selectedCustomParam;
    private InputParam selectedInputParam;
    private ResultDetailsParam selectedResultDetailsParam;
    private VerificationFailureReason selectedFailureReason;
    private DualListModel<Verification> parentVerifications;
    private DualListModel<SlaveLabel> slaveLabelsDualList;
    private Set<TestResultType> testResultTypes = new HashSet<TestResultType>();
    @Inject
    private VerificationEJB verificationEJB;
    @Inject
    private CustomParamEJB customParamEJB;
    @Inject
    private CustomParamValueEJB customParamValueEJB;
    @Inject
    private InputParamEJB inputParamEJB;
    @Inject
    private ResultDetailsParamEJB resultDetailsParamEJB;
    @Inject
    private SlaveLabelEJB slaveLabelEJB;
    @Inject
    private VerificationFailureReasonEJB verificationFailureReasonEJB;

    @Override
    protected void init() throws NotFoundException {
        String verificationId = getQueryParam("verificationId");
        verification = new Verification();

        if (verificationId != null) {
            log.debug("Finding verification {} for editing!", verificationId);
            Long id = Long.parseLong(verificationId);
            verification = verificationEJB.read(id);
            testResultTypes.addAll(verification.getTestResultTypes());
        }
        initCustomParams();
        initInputParams();
        initResultDetailsParams();
        initParentVerifications();
        initSlaveLabelsDualList();
        initFailureReasons();
    }

    public Verification getVerification() {
        return verification;
    }

    public void setVerification(Verification verification) {
        this.verification = verification;
    }

    public DualListModel<SlaveLabel> getSlaveLabelsDualList() {
        return slaveLabelsDualList;
    }

    public void setSlaveLabelsDualList(DualListModel<SlaveLabel> slaveLabelsDualList) {
        this.slaveLabelsDualList = slaveLabelsDualList;
    }

    public CustomParam getSelectedCustomParam() {
        return selectedCustomParam;
    }

    public void setSelectedCustomParam(CustomParam selectedCustomParam) {
        this.selectedCustomParam = selectedCustomParam;
    }

    public InputParam getSelectedInputParam() {
        return selectedInputParam;
    }

    public void setSelectedInputParam(InputParam selectedInputParam) {
        this.selectedInputParam = selectedInputParam;
    }

    public ResultDetailsParam getSelectedResultDetailsParam() {
        return selectedResultDetailsParam;
    }

    public void setSelectedResultDetailsParam(ResultDetailsParam selectedResultDetailsParam) {
        this.selectedResultDetailsParam = selectedResultDetailsParam;
    }

    public DualListModel<Verification> getParentVerifications() {
        return parentVerifications;
    }

    public void setParentVerifications(DualListModel<Verification> parentVerifications) {
        this.parentVerifications = parentVerifications;
    }

    public void selectCustomParam(CustomParam customParam) {
        setSelectedCustomParam(customParam);
        if (getSelectedCustomParam().getId() != null) {
            List<CustomParamValue> customParamValues = customParamEJB
                    .getCustomParamValues(getSelectedCustomParam().getId());
            getSelectedCustomParam().setCustomParamValues(customParamValues);
        }
    }

    public void selectInputParam(InputParam inputParam) {
        setSelectedInputParam(inputParam);
    }

    public void selectResultDetailsParam(ResultDetailsParam resultDetailsParam) {
        setSelectedResultDetailsParam(resultDetailsParam);
    }

    public void selectFailureReason(VerificationFailureReason vfr) {
        setSelectedFailureReason(vfr);
    }

    public void addInputParam() {
        setSelectedInputParam(new InputParam());
        getSelectedInputParam().setVerification(verification);
    }

    public void addResultDetailsParam() {
        setSelectedResultDetailsParam(new ResultDetailsParam());
        getSelectedResultDetailsParam().setVerification(verification);
    }

    public void addCustomParam() {
        log.debug("Adding new custom param...");
        setSelectedCustomParam(new CustomParam());
        getSelectedCustomParam().setVerification(verification);
        getSelectedCustomParam().setCustomParamValues(new ArrayList<CustomParamValue>());
    }

    public void addCustomParamValue() {
        log.debug("Adding new param value...");
        CustomParamValue value = new CustomParamValue();
        value.setCustomParam(getSelectedCustomParam());
        value.setParamValue("");
        getSelectedCustomParam().getCustomParamValues().add(value);
    }

    public void addFailureReason() {
        log.debug("Adding new failure reason...");
        setSelectedFailureReason(new VerificationFailureReason());
        getSelectedFailureReason().setVerification(verification);
    }

    public void deleteCustomParamValue(CustomParamValue paramValue) throws NotFoundException {
        log.info("Deleting custom parameter value {} for {}", paramValue, selectedCustomParam);
        selectedCustomParam.getCustomParamValues().remove(paramValue);
        if (paramValue.getId() != null) {
            customParamValueEJB.delete(paramValue);
        }
    }

    public void saveCustomParam() throws NotFoundException {
        log.info("Saving custom parameter for verification {}", verification);

        if (!verification.getCustomParams().contains(selectedCustomParam)) {
            verification.getCustomParams().add(selectedCustomParam);
        }

        if (verification.getId() == null) {
            return;
        }

        //persist param
        if (selectedCustomParam.getId() == null) {
            customParamEJB.create(selectedCustomParam);
            return;
        }

        //persist param values
        for (CustomParamValue paramValue : selectedCustomParam.getCustomParamValues()) {
            if (paramValue.getId() != null) {
                customParamValueEJB.update(paramValue);
            }
        }

        Iterator<CustomParamValue> it = selectedCustomParam.getCustomParamValues().iterator();
        while (it.hasNext()) {
            CustomParamValue paramValue = it.next();

            //check if editing existing values produced duplicates
            if (paramValue.getId() != null && customParamEJB.hasDuplicateValue(selectedCustomParam.getId(), paramValue)) {
                customParamValueEJB.delete(paramValue);
                it.remove();
            } //add new value, unless a duplicate exists
            else if (paramValue.getId() == null) {
                if (customParamEJB.hasParamValue(selectedCustomParam.getId(), paramValue)) {
                    it.remove();
                } else {
                    customParamValueEJB.create(paramValue);
                }
            }
        }

        customParamEJB.update(selectedCustomParam);
    }

    public void cancelEditCustomParam() {
        log.info("Cancelling custom parameter edit for verification {}", verification);

        //revert back to param values prior to editing
        if (getSelectedCustomParam().getId() != null) {
            List<CustomParamValue> customParamValues = customParamEJB.getCustomParamValues(getSelectedCustomParam().getId());
            getSelectedCustomParam().setCustomParamValues(customParamValues);
        }
    }

    public void saveInputParam() throws NotFoundException {
        log.info("Saving {} for verification {}", selectedInputParam, verification);
        if (!verification.getInputParams().contains(selectedInputParam)) {
            verification.getInputParams().add(selectedInputParam);
        }

        if (verification.getId() == null) {
            return;
        }

        if (selectedInputParam.getId() == null) {
            inputParamEJB.create(selectedInputParam);
            return;
        }
        inputParamEJB.update(selectedInputParam);
    }

    public void saveFailureReason() throws NotFoundException {
        log.info("Saving {} for verification {}", selectedFailureReason, verification);
        if (!verification.getFailureReasons().contains(selectedFailureReason)) {
            verification.getFailureReasons().add(selectedFailureReason);
        }

        if (verification.getId() == null) {
            return;
        }

        if (selectedFailureReason.getId() == null) {
            verificationFailureReasonEJB.create(selectedFailureReason);
            return;
        }
        verificationFailureReasonEJB.update(selectedFailureReason);
    }

    public void saveResultDetailsParam() throws NotFoundException {
        log.info("Saving {} for verification {}", selectedResultDetailsParam, verification);
        if (!verification.getResultDetailsParams().contains(selectedResultDetailsParam)) {
            verification.getResultDetailsParams().add(selectedResultDetailsParam);
        }

        if (verification.getId() == null) {
            return;
        }

        if (selectedResultDetailsParam.getId() == null) {
            resultDetailsParamEJB.create(selectedResultDetailsParam);
            return;
        }
        resultDetailsParamEJB.update(selectedResultDetailsParam);
    }

    public void deleteCustomParam(CustomParam customParam) throws NotFoundException {
        log.info("Deleting custom paramater {} for verification {}", customParam, verification);
        verification.getCustomParams().remove(customParam);
        if (verification.getId() == null) {
            return;
        }
        customParamEJB.delete(customParam);
    }

    public void deleteInputParam(InputParam inputParam) throws NotFoundException {
        log.info("Deleting {} for verification {}", inputParam, verification);
        verification.getInputParams().remove(inputParam);
        if (verification.getId() == null) {
            return;
        }
        inputParamEJB.delete(inputParam);
    }

    public void deleteResultDetailsParam(ResultDetailsParam resultDetailsParam) throws NotFoundException {
        log.info("Deleting {} for verification {}", resultDetailsParam, verification);
        verification.getResultDetailsParams().remove(resultDetailsParam);
        if (verification.getId() == null) {
            return;
        }
        resultDetailsParamEJB.delete(resultDetailsParam);
    }

    public void deleteFailureReason(VerificationFailureReason vfr) throws NotFoundException {
        log.info("Deleting {} for verification {}", vfr, verification);
        verification.getFailureReasons().remove(vfr);
        if (verification.getId() == null) {
            return;
        }
        verificationFailureReasonEJB.delete(vfr);
    }

    public String save() {
        log.debug("Save triggered!");

        if (verification.getType() != VerificationType.NORMAL) {
            parentVerifications.getTarget().clear();
        }

        verification.setSlaveLabels(slaveLabelsDualList.getTarget());
        verification.setParentVerifications(parentVerifications.getTarget());
        verification.setTestResultTypes(testResultTypes);

        if (verification.getUuid() == null || verification.getUuid().isEmpty()) {
            verification.setUuid(UUID.randomUUID().toString());
        }

        try {
            if (verification.getId() != null) {
                log.debug("Updating existing verification {}", verification);
                if (verification.getType() != VerificationType.NORMAL) {
                    List<CustomParam> params = verificationEJB.getCustomParams(verification.getId());
                    for (CustomParam c : params) {
                        deleteCustomParam(c);
                    }
                }
                verificationEJB.update(verification);
            } else {
                log.debug("Saving new verification!");
                if (verification.getType() != VerificationType.NORMAL) {
                    verification.getCustomParams().clear();
                }
                verificationEJB.create(verification);
            }
            return "verifications?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save verification {}! Cause: {}", verification, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Verification could not be saved!", "");
        }

        return null;
    }

    public String cancelEdit() {
        return "verifications?faces-redirect=true";
    }

    public VerificationFailureReasonSeverity[] getSeverityValues() {
        return VerificationFailureReasonSeverity.values();
    }

    public BuildStatus[] getBuildStatusValues() {
        return BuildStatus.values();
    }

    public VerificationType[] getVerificationTypeValues() {
        return VerificationType.values();
    }

    public TestResultType[] getTestResultTypeValues() {
        return TestResultType.values();
    }

    public VerificationTargetPlatform[] getVerificationTargetPlatformValues() {
        return VerificationTargetPlatform.values();
    }

    private void initCustomParams() {
        setSelectedCustomParam(new CustomParam());
        getSelectedCustomParam().setVerification(verification);
        if (verification.getId() != null) {
            verification.setCustomParams(verificationEJB.getCustomParams(verification.getId()));
        }
    }

    private void initInputParams() {
        setSelectedInputParam(new InputParam());
        getSelectedInputParam().setVerification(verification);
        if (verification.getId() != null) {
            verification.setInputParams(verificationEJB.getInputParams(verification.getId()));
        }
    }

    private void initResultDetailsParams() {
        setSelectedResultDetailsParam(new ResultDetailsParam());
        getSelectedResultDetailsParam().setVerification(verification);
        if (verification.getId() != null) {
            verification.setResultDetailsParams(verificationEJB.getResultDetailsParams(verification.getId()));
        }
    }

    private void initFailureReasons() {
        setSelectedFailureReason(new VerificationFailureReason());
        getSelectedFailureReason().setVerification(verification);
        if (verification.getId() != null) {
            verification.setFailureReasons(verificationEJB.getFailureReasons(verification.getId()));
        }
    }

    private void initParentVerifications() {
        List<Verification> source = verificationEJB.readAll();
        List<Verification> target = new ArrayList<Verification>();
        if (verification.getId() != null) {
            target = verificationEJB.getParentVerifications(verification.getId());
        }
        source.removeAll(target);
        source.remove(verification);
        parentVerifications = new DualListModel<Verification>(source, target);
    }

    private void initSlaveLabelsDualList() throws NotFoundException {
        List<SlaveLabel> source = slaveLabelEJB.readAll();
        List<SlaveLabel> target = new ArrayList<SlaveLabel>();

        if (verification.getId() != null) {
            target = verificationEJB.getSlaveLabels(verification.getId());
        }
        source.removeAll(target);
        slaveLabelsDualList = new DualListModel<SlaveLabel>(source, target);
    }

    public List<SlaveLabel> querySlaveLabels(Long id) throws NotFoundException {
        return verificationEJB.getSlaveLabels(id);
    }

    public VerificationFailureReason getSelectedFailureReason() {
        return selectedFailureReason;
    }

    public void setSelectedFailureReason(VerificationFailureReason selectedFailureReason) {
        this.selectedFailureReason = selectedFailureReason;
    }

    public Set<TestResultType> getTestResultTypes() {
        return testResultTypes;
    }

    public void setTestResultTypes(Set<TestResultType> testResultTypes) {
        this.testResultTypes = testResultTypes;
    }
}
