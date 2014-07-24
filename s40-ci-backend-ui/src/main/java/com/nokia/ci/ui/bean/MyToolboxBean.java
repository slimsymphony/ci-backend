package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.WidgetEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.model.Widget;
import com.nokia.ci.ejb.model.WidgetSetting;
import com.nokia.ci.ui.model.BuildGroupsLazyDataModel;
import com.nokia.ci.ui.model.ChangesLazyDataModel;
import com.nokia.ci.ui.model.ExtendedBuildGroup;
import com.nokia.ci.ui.model.GroupedProjectModel;
import com.nokia.ci.ui.widget.BaseWidget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.Application;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.component.dashboard.Dashboard;
import org.primefaces.component.panel.Panel;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.DashboardReorderEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 11/8/12 Time: 9:56 AM To change
 * this template use File | Settings | File Templates.
 */
@Named
@ViewScoped
public class MyToolboxBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(MyToolboxBean.class);
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    private SysUserEJB sysUserEJB;
    @Inject
    private WidgetEJB widgetEJB;
    @Inject
    private JobEJB jobEJB;
    @Inject
    private ProjectEJB projectEJB;
    @Inject
    private BranchEJB branchEJB;
    @Inject
    private BuildGroupEJB buildGroupEJB;
    @Inject
    private SysConfigEJB sysConfigEJB;
    private List<Job> userJobs;
    private Long userProject;
    private Long userPrjGroup;
    private GroupedProjectModel groupedProjectModel;
    private List<Project> projectsWithToolboxOrDraftBranch;
    private Map<Long, LazyDataModel<ExtendedBuildGroup>> userBuildsLazyDataModels;
    private Map<Long, LazyDataModel<Change>> userChangesLazyDataModels;
    private Map<Long, LazyDataModel<ExtendedBuildGroup>> verificationBuildsLazyDataModels;
    private Dashboard board;
    private DashboardModel boardModel;
    private List<BaseWidget> widgets;

    @Override
    protected void init() throws NotFoundException {
        initProjects();
        groupedProjectModel = new GroupedProjectModel(getProjectsWithToolboxOrDraftBranch());
        initBoard();
        userBuildsLazyDataModels = new HashMap<Long, LazyDataModel<ExtendedBuildGroup>>();
        userChangesLazyDataModels = new HashMap<Long, LazyDataModel<Change>>();
        verificationBuildsLazyDataModels = new HashMap<Long, LazyDataModel<ExtendedBuildGroup>>();
    }

    private void initBoard() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();
        board = (Dashboard) application.createComponent(fc, "org.primefaces.component.Dashboard", "org.primefaces.component.DashboardRenderer");
        board.setId("Board");

        boardModel = new DefaultDashboardModel();
        DashboardColumn column = new DefaultDashboardColumn();
        boardModel.addColumn(column);
        board.setModel(boardModel);

        widgets = new ArrayList<BaseWidget>();
        List<Widget> wgts = sysUserEJB.getWidgetsInOrder(httpSessionBean.getSysUserId());

        for (Widget w : wgts) {
            BaseWidget bw = getWidgetClass(w);
            if (bw == null) {
                continue;
            }

            initWidgetSettings(w, bw);
            bw.init(w);

            bw.addCloseBehavior("#{myToolboxBean.handleClose}");
            bw.addToggleBehavior("#{myToolboxBean.handleToggle}");
            Panel p = bw.getPanel();

            if (p == null) {
                log.warn("Could not initialize widget {}", w);
                continue;
            }

            widgets.add(bw);
            board.getChildren().add(p);
            boardModel.getColumn(0).addWidget(p.getId());
        }
    }

    private BaseWidget getWidgetClass(Widget w) {
        BaseWidget ret = null;
        try {
            Class c = Class.forName(w.getClassName());
            ret = (BaseWidget) c.newInstance();
        } catch (Exception ex) {
            log.warn("Could not find widget class {} for widget {}", w.getClassName(), w);
        }

        return ret;
    }

    private void initWidgetSettings(Widget w, BaseWidget impl) {
        List<WidgetSetting> settings = w.getSettings();
        Map<String, String> settingMap = new HashMap<String, String>();
        for (WidgetSetting s : settings) {
            settingMap.put(s.getSettingKey(), s.getSettingValue());
        }
        impl.setSettings(settingMap);
    }

    public List<Project> getProjectsWithToolboxOrDraftBranch() {
        return projectsWithToolboxOrDraftBranch;
    }

    public void setProjectsWithToolboxOrDraftBranch(List<Project> projectsWithToolboxOrDraftBranch) {
        this.projectsWithToolboxOrDraftBranch = projectsWithToolboxOrDraftBranch;
    }    
    
    private void initProjects() {
        List<Project> allProjects = projectEJB.getProjectsWithToolboxOrDraftBranch();
        projectsWithToolboxOrDraftBranch = new ArrayList<Project>();
        for (Project p : allProjects) {
            if (httpSessionBean.hasAccessToProject(p.getId())) {
                projectsWithToolboxOrDraftBranch.add(p);
            }
        }
    }

    public Dashboard getBoard() {
        return board;
    }

    public void setBoard(Dashboard board) {
        this.board = board;
    }

    public void handleReorder(DashboardReorderEvent event) throws NotFoundException {
        String id = event.getWidgetId();
        Widget widget = getWidgetById(id);
        if (widget == null) {
            log.warn("Could not find widget with id {}", id);
            return;
        }

        List<Widget> wgts = sysUserEJB.getWidgetsInOrder(httpSessionBean.getSysUserId());
        wgts.remove(widget);
        wgts.add(event.getItemIndex(), widget);

        sysUserEJB.saveWidgets(httpSessionBean.getSysUserId(), wgts);
    }

    public void handleClose(CloseEvent event) throws NotFoundException {
        String id = event.getComponent().getId();
        Widget w = getWidgetById(id);
        if (w != null) {
            widgetEJB.delete(w);
        }
    }

    public void handleToggle(ToggleEvent event) throws NotFoundException {
        String id = event.getComponent().getId();
        Widget w = getWidgetById(id);
        if (w != null) {
            Boolean minimized = (w.getMinimized() == null ? true : w.getMinimized() == false ? true : false);
            w.setMinimized(minimized);
            widgetEJB.update(w);
        }
    }

    public Widget getWidgetById(String id) {
        for (BaseWidget w : widgets) {
            if (w.getIdentifier().equals(id)) {
                return w.getPersistentWidget();
            }
        }

        return null;
    }

    private void initUserJobs() {
        SysUser sysUser = httpSessionBean.getSysUser();
        userJobs = sysUserEJB.getOwnedJobs(sysUser.getId());
    }

    public List<BaseWidget> getWidgets() {
        return widgets;
    }

    public void setWidgets(List<BaseWidget> widgets) {
        this.widgets = widgets;
    }

    public List<Job> getUserJobs() {
        initUserJobs();
        return userJobs;
    }

    public HttpSessionBean getHttpSessionBean() {
        return httpSessionBean;
    }

    public void setHttpSessionBean(HttpSessionBean httpSessionBean) {
        this.httpSessionBean = httpSessionBean;
    }

    public void setUserProject(Long userPrj) {
        this.userProject = userPrj;
    }

    public void clearUserProject() {
        this.userProject = null;
    }

    public Long getUserProject() {
        return userProject;
    }

    public void setUserPrjGroup(Long group) {
        this.userPrjGroup = group;
    }

    public Long getUserPrjGroup() {
        return userPrjGroup;
    }

    public GroupedProjectModel getGroupedProjectModel() {
        return groupedProjectModel;
    }

    public LazyDataModel<ExtendedBuildGroup> getUserBuildsLazyDataModel(String id) {
        Long userId = Long.parseLong(id);
        if (userBuildsLazyDataModels.containsKey(userId)) {
            return userBuildsLazyDataModels.get(userId);
        }

        LazyDataModel<ExtendedBuildGroup> ret = new BuildGroupsLazyDataModel(sysUserEJB, buildGroupEJB, sysConfigEJB, userId);
        userBuildsLazyDataModels.put(userId, ret);
        return ret;
    }

    public LazyDataModel<ExtendedBuildGroup> getVerificationBuildsLazyDataModel(String id) {
        Long verificationId = Long.parseLong(id);

        if (verificationBuildsLazyDataModels.containsKey(verificationId)) {
            return verificationBuildsLazyDataModels.get(verificationId);
        }

        LazyDataModel<ExtendedBuildGroup> ret = new BuildGroupsLazyDataModel(jobEJB, buildGroupEJB, verificationId);
        verificationBuildsLazyDataModels.put(verificationId, ret);
        return ret;
    }

    public LazyDataModel<Change> getUserChangesLazyDataModel(String id) {
        Long userId = Long.parseLong(id);
        if (userChangesLazyDataModels.containsKey(userId)) {
            return userChangesLazyDataModels.get(userId);
        }

        LazyDataModel<Change> ret = new ChangesLazyDataModel(sysUserEJB, userId);
        userChangesLazyDataModels.put(userId, ret);
        return ret;
    }

    public List<Job> getProjectJobs(String id) throws NotFoundException {
        Long projectId = Long.parseLong(id);
        Project project = projectEJB.read(projectId);

        List<Job> jobs = new ArrayList<Job>();
        if (project.getId() == null) {
            return jobs;
        }

        try {
            List<Branch> branches = projectEJB.getBranches(project.getId());
            for (Branch b : branches) {
                jobs.addAll(branchEJB.getJobs(b.getId(),
                        httpSessionBean.getSysUserId()));
            }
        } catch (NotFoundException ex) {
            log.warn("Can not fetch jobs for project {}", project, ex);
            jobs.clear();
        }

        return jobs;
    }
}
