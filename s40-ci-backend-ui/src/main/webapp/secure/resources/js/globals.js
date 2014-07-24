var s40_ci_products = ["evo", "titan_ss", "titan_ds", "gaia_ds", "gaia_ss"];
var s40_ci_branches = ["develop", "development", "master", "integration"];
var s40_ci_testtypes = ["isattcn", "cppunit", "tdk"];
var s40_ci_covtypes = ["ctc"];
var s40_ci_branchtypes = ["DEVELOPMENT", "SINGLE_COMMIT", "TOOLBOX", "MASTER"];
var s40_ci_scales = ["per_build", "daily", "weekly", "monthly"];
//var s40_ci_metrics_server_prefix = "http://localhost:8080";
var s40_ci_metrics_server_prefix = "http://oucitst01.europe.nokia.com:8080";
//http://oucitst01.europe.nokia.com:8080/s40-ci-metrics-restful-service/metrics/test_component_coverage_trend?productName=evo&branchName=develop&testType=tdk&covType=ctc&callback=jQuery17107694353246116488_1339494807229&_=1339494807522
function initOperations(){
    var index = 0;
    
    s40CIProducts = document.getElementById("s40_ci_products");

    if (s40CIProducts != null){
        for(index = 0; index < s40_ci_products.length; index++) {
            s40CIProducts.options[s40CIProducts.options.length] = new Option(s40_ci_products[index], s40_ci_products[index]);
        }
    }
    
    s40CIBranchs = document.getElementById("s40_ci_branches");
    
    if (s40CIBranchs != null){
        for(index = 0; index < s40_ci_branches.length; index++) {
            s40CIBranchs.options[s40CIBranchs.options.length] = new Option(s40_ci_branches[index], s40_ci_branches[index]);
        }
    }
    
    s40CITestTypes = document.getElementById("s40_ci_testtypes");
    
    if (s40CITestTypes != null){    
        for(var index = 0; index < s40_ci_testtypes.length; index++) {
            s40CITestTypes.options[s40CITestTypes.options.length] = new Option(s40_ci_testtypes[index], s40_ci_testtypes[index]);
        }
    }
    
    s40CICovTypes = document.getElementById("s40_ci_covtypes");
    
    if (s40CICovTypes != null){    
        for(var index = 0; index < s40_ci_covtypes.length; index++) {
            s40CICovTypes.options[s40CICovTypes.options.length] = new Option(s40_ci_covtypes[index], s40_ci_covtypes[index]);
        }
    }    
    
    s40CIBranchTypes = document.getElementById("s40_ci_branchtypes");
    
    if (s40CIBranchTypes != null){
        for(var index = 0; index < s40_ci_branchtypes.length; index++) {
            s40CIBranchTypes.options[s40CIBranchTypes.options.length] = new Option(s40_ci_branchtypes[index], s40_ci_branchtypes[index]);
        }
    }

    s40CIScales = document.getElementById("s40_ci_scales");
    
    if (s40CIScales != null){
        for(var index = 0; index < s40_ci_scales.length; index++) {
            s40CIScales.options[s40CIScales.options.length] = new Option(s40_ci_scales[index], s40_ci_scales[index]);
        }
    }
}