package com.nokia.ci.api.resource;

import com.nokia.ci.api.session.SessionManager;
import com.nokia.ci.api.session.SessionManager.Session;
import com.nokia.ci.client.model.JobVerificationConfView;
import com.nokia.ci.client.model.JobView;
import com.nokia.ci.client.rest.JobResource;
import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobVerificationConf;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Verification;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST resource for {@link Job} entities.
 *
 * @author vrouvine
 */
@Named
@RequestScoped
public class JobResourceImpl implements JobResource {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(JobResourceImpl.class);
    /**
     * Job EJB.
     */
    @Inject
    JobEJB jobEJB;
    @Inject
    BuildEJB buildEJB;
    @Inject
    SessionManager sessionManager;
    @Inject
    ProjectEJB projectEJB;

    @Override
    public Response getJobs(String token) {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @Override
    public Response getJob(Long id) {
        log.debug("Requesting job by id {}", id);
        Job j = null;
        try {
            j = jobEJB.read(id);
        } catch (NotFoundException ex) {
            log.debug("Job not found", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        JobView view = viewFromEntity(j);
        return Response.ok(view).build();
    }

    @Override
    public Response start(Long id, String refSpec) {
        log.info("starting job with id: {} and refSpec", id, refSpec);

        try {
            jobEJB.start(id, refSpec, null);
        } catch (BackendAppException ex) {
            log.warn("Job start failed", ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.ok().build();
    }

    @Override
    public Response createJob(JobView job, Long projectId, String token)
            throws URISyntaxException {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @Override
    public Response updateJob(Long id, JobView job) {
        log.info("Updating job with id {}", id);
        Job j = job.transformTo(Job.class);
        j.setId(id);
        Job result = null;
        try {
            result = jobEJB.update(j);
        } catch (NotFoundException ex) {
            log.debug("job update failed", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.debug("Successfully updated job with id {}", id);
        return Response.ok(viewFromEntity(result)).build();
    }

    @Override
    public Response deleteJob(Long id, String token) {
        log.info("Deleting job with id {} and token {}", id, token);

        Session session = sessionManager.getSession(token);
        if (session == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Job job = null;
        try {
            job = jobEJB.read(id);
            jobEJB.delete(job, session.getSysUserId());
        } catch (NotFoundException ex) {
            log.debug("Job not found", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (UnauthorizedException ex) {
            log.warn("job delete failed", ex);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok().build();
    }

    @Override
    public Response getVerificationConfs(Long id) {
        List<JobVerificationConf> verificationConfs = jobEJB.getVerificationConfs(id);
        List<JobVerificationConfView> views = new ArrayList<JobVerificationConfView>(verificationConfs.size());
        for (JobVerificationConf conf : verificationConfs) {
            JobVerificationConfView view = new JobVerificationConfView();
            view.setId(conf.getId());
            view.setJobId(conf.getJob().getId());
            view.setProductId(conf.getProduct().getId());
            view.setVerificationId(conf.getVerification().getId());
            views.add(view);
        }
        return Response.ok(views).build();
    }

    @Override
    public Response saveVerificationConfs(Long id, List<JobVerificationConfView> confs) {
        List<JobVerificationConf> jvcs = new ArrayList<JobVerificationConf>(confs.size());
        for (JobVerificationConfView view : confs) {
            JobVerificationConf jvc = new JobVerificationConf();
            Product p = new Product();
            p.setId(view.getProductId());
            Verification v = new Verification();
            v.setId(view.getVerificationId());
            jvc.setProduct(p);
            jvc.setVerification(v);
            jvcs.add(jvc);
        }
        try {
            jobEJB.saveVerificationConfs(id, jvcs);
        } catch (NotFoundException ex) {
            log.debug("Job not found with id {}", id, ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok().build();
    }

    private JobView viewFromEntity(Job job) {
        JobView jobView = new JobView();
        jobView.setId(job.getId());
        jobView.setName(job.getName());
        jobView.setUrl(null);
        jobView.setDisplayName(job.getDisplayName());

        // null check required. There are database entries that
        // do not have branch.
        if (job.getBranch() != null) {
            jobView.setBranchId(job.getBranch().getId());
        }

        return jobView;
    }
}
