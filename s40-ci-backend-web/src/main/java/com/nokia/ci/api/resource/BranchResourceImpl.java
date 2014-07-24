package com.nokia.ci.api.resource;

import com.nokia.ci.api.util.ViewModelUtils;
import com.nokia.ci.client.model.BranchView;
import com.nokia.ci.client.rest.BranchResource;
import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST resource for {@link Branch} entities.
 *
 * @author vrouvine
 */
@Named
@RequestScoped
public class BranchResourceImpl implements BranchResource {

    private static Logger log = LoggerFactory.getLogger(BranchResourceImpl.class);
    /**
     * Branch EJB.
     */
    @Inject
    BranchEJB branchEJB;

    @Override
    public Response getBranches() {
        log.debug("Requesting all branches");
        List<Branch> branches = branchEJB.readAll();
        List<BranchView> views = viewsFromEntityList(branches);
        return Response.ok(views).build();
    }

    @Override
    public Response getBranch(Long id) {
        log.debug("Requesting branch by id {}", id);
        Branch b = null;
        try {
            b = branchEJB.read(id);
        } catch (NotFoundException ex) {
            log.debug("Branch not found", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        BranchView view = viewFromEntity(b);
        return Response.ok(view).build();
    }

    @Override
    public Response createBranch(BranchView branch) throws URISyntaxException {
        Branch b = branch.transformTo(Branch.class);
        log.info("Creating new branch with name '{}'", b.getName());
        branchEJB.create(b);
        URI uri = UriBuilder.fromResource(BranchResource.class).path(b.getId().toString()).build();
        log.info("New branch created to uri: {}", uri.toString());
        return Response.created(uri).build();
    }

    @Override
    public Response updateBranch(Long id, BranchView branch) {
        log.info("Updating branch with id {}", id);
        Branch b = branch.transformTo(Branch.class);
        b.setId(id);
        Branch result = null;
        try {
            result = branchEJB.update(b);
        } catch (NotFoundException ex) {
            log.debug("Branch not found", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("Successfully updated branch with id {}", id);
        return Response.ok(ViewModelUtils.getModel(result, BranchView.class)).build();
    }

    private List<BranchView> viewsFromEntityList(List<Branch> branches) {
        List<BranchView> views = new ArrayList<BranchView>(branches.size());
        for (Branch branch : branches) {
            views.add(viewFromEntity(branch));
        }
        return views;
    }

    private BranchView viewFromEntity(Branch branch) {
        BranchView view = new BranchView();
        view.copyValuesFrom(branch);
        if (branch.getProject() != null) {
            view.setProjectId(branch.getProject().getId());
        }
        return view;
    }
}
