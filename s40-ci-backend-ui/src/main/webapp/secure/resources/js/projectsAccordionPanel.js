function updateCookies() {
    if (projectsAccordion.cfg.multiple) {
        console.log("Setting open groups to " + projectsAccordion.cfg.active.join(','));
        $.cookie('openProjectGroups', projectsAccordion.cfg.active.join(','));
    } else {
        $.cookie('openProjectGroups', projectsAccordion.cfg.active);
    }
}

$(document).ready(function() {
    if (!$.cookie('openProjectGroups')) {
        updateCookies();
    } else {
        console.log("initializing groups to " + $.cookie('openProjectGroups'));
        var groups = $.cookie('openProjectGroups').split(',');
        for (i = 0; i < groups.length; i++) {
            projectsAccordion.select(groups[i]);
        }
    }

    projectsAccordion.headers.click(function() {
        updateCookies();
    });
});

