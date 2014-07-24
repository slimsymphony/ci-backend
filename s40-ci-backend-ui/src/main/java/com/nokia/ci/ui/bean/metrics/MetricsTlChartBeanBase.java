package com.nokia.ci.ui.bean.metrics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.primefaces.event.SelectEvent;
import org.primefaces.extensions.model.timeline.DefaultTimeLine;
import org.primefaces.extensions.model.timeline.DefaultTimelineEvent;
import org.primefaces.extensions.model.timeline.Timeline;

/**
 * Bean base class for job time line related metrics.
 *
 * @author larryang
 */
public abstract class MetricsTlChartBeanBase extends MetricsBeanBase {

    private List<Timeline> timelines;
    
    class CustomTimelineEvent extends DefaultTimelineEvent {
        private Long itemId;

        public CustomTimelineEvent(String title, Date startDate, Date endDate) {
            super(title, startDate, endDate);
        }
        
        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }
    }
    
    @Override
    public void init() {
        super.init();
        timelines = new ArrayList<Timeline>();
        timelines.add(new DefaultTimeLine("", ""));
    }

    @Override
    public void updateDataModel() {
        super.updateDataModel();
        timelines.clear();
    }

    public void eventSelect(SelectEvent event) {
        CustomTimelineEvent selectedEvent = (CustomTimelineEvent) event.getObject();
        open(selectedEvent.itemId);
    }

    public List<Timeline> getTimelines() {
        return timelines;
    }

    public void setTimelines(List<Timeline> timelines) {
        this.timelines = timelines;
    }
}