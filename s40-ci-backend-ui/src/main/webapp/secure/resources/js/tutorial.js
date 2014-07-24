// Starts the interactive tutorial
function startTutorial() {
    if (welcomeDlg !== null) {
        welcomeDlg.hide();
    }

    introJs().start();

    // Will do multiple page tutorial when it is necessary
    /* introJs().setOption('doneLabel', 'Next page').start().oncomplete(function() {
     if (window.location.href.indexOf("my/toolbox") !== -1) {
     window.location.href = '/page/projects/?tutorial';
     } else if (window.location.href.indexOf('page/projects') !== -1) {
     window.location.href = '/page/projec/random/?tutorial';
     }
     });
     */
}

if (RegExp('tutorial', 'gi').test(window.location.search)) {
    startTutorial();
}