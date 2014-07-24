package com.nokia.ci.api.resource;

import com.nokia.ci.api.util.ViewModelUtils;
import com.nokia.ci.client.model.ProductView;
import com.nokia.ci.client.model.ProjectVerificationConfView;
import com.nokia.ci.client.model.ProjectView;
import com.nokia.ci.client.model.VerificationView;
import com.nokia.ci.client.rest.ProjectResource;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST resource for {@link Project} entities.
 *
 * @author vrouvine
 */
@Named
@RequestScoped
public class ProjectResourceImpl implements ProjectResource {

    private static Logger log = LoggerFactory.getLogger(
            ProjectResourceImpl.class);
    /**
     * Project EJB.
     */
    @Inject
    ProjectEJB projectEJB;

    @Override
    public Response getProjects() {
        log.debug("Requesting all projects");
        List<Project> projects = projectEJB.readAll();
        List<ProjectView> views = ViewModelUtils.copyValuesFromList(projects,
                ProjectView.class);
        return Response.ok(views).build();
    }

    @Override
    public Response getProject(Long id) {
        log.debug("Requesting project by id {}", id);
        Project project = null;
        try {
            project = projectEJB.read(id);
        } catch (NotFoundException ex) {
            log.debug("Project not found", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        ProjectView view = ViewModelUtils.getModel(project, ProjectView.class);
        return Response.ok(view).build();
    }

    @Override
    public Response getProducts(Long id) {
        log.debug("Requesting products for project with id {}", id);
        List<Product> products = projectEJB.getProducts(id);
        List<ProductView> views = ViewModelUtils.copyValuesFromList(
                products, ProductView.class);
        return Response.ok(views).build();
    }

    @Override
    public Response getVerifications(Long id) {
        log.debug("Requesting verifications for project with id {}", id);
        List<Verification> verifications = projectEJB.getVerifications(id);
        List<VerificationView> views = ViewModelUtils.copyValuesFromList(
                verifications, VerificationView.class);
        return Response.ok(views).build();
    }

    @Override
    public Response getVerificationConfs(Long id) {
        log.debug("Requesting verification configurations for project with id {}", id);
        List<ProjectVerificationConf> confs = projectEJB.getVerificationConfs(id);
        List<ProjectVerificationConfView> views =
                new ArrayList<ProjectVerificationConfView>(confs.size());
        for (ProjectVerificationConf conf : confs) {
            ProjectVerificationConfView view = new ProjectVerificationConfView();
            view.setId(conf.getId());
            view.setProductId(conf.getProduct().getId());
            view.setProjectId(conf.getProject().getId());
            view.setVerificationId(conf.getVerification().getId());
            views.add(view);
        }
        return Response.ok(views).build();
    }
}
