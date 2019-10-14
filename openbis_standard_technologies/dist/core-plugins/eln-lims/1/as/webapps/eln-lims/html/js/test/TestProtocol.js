var TestProtocol = new function () {
    this.start = function() {
        this.login();
        this.inventorySpace();
    }

    this.login = function() {
        var api = EventUtil;
        api.waitForId("username")
           .then(function() {
                api.write("username", "admin");
                api.write("password", "a");
                api.click("login-button");
           })
           .catch(error => { alert(error);});
    }

    this.inventorySpace = function() {
        var api = EventUtil;

        api.waitForId("tree")
           .then(function() {
                api.waitForId("LAB_NOTEBOOK")
           })
           .then(function() {
                api.waitForId("INVENTORY")
           })
           .then(function() {
                api.waitForId("MATERIALS")
           })
           .then(function() {
                api.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_CELL_LINES_CELL_LINE_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_FLIES_FLY_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_PLANTS_PLANT_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_PLASMIDS_PLASMID_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_POLYNUCLEOTIDES_OLIGO_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_POLYNUCLEOTIDES_RNA_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_REAGENTS_ANTIBODY_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_REAGENTS_CHEMICAL_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_REAGENTS_ENZYME_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_REAGENTS_MEDIA_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_REAGENTS_SOLUTION_BUFFER_COLLECTION")
           })
           .then(function() {
                api.waitForId("_MATERIALS_YEASTS_YEAST_COLLECTION")
           })
           .then(function() {
                api.waitForId("METHODS")
           })
           .then(function() {
                api.waitForId("_METHODS_PROTOCOLS_GENERAL_PROTOCOLS")
           })
           .then(function() {
                api.waitForId("_METHODS_PROTOCOLS_PCR_PROTOCOLS")
           })
           .then(function() {
                api.waitForId("_METHODS_PROTOCOLS_WESTERN_BLOTTING_PROTOCOLS")
           })
           .then(function() {
                api.waitForId("PUBLICATIONS")
           })
           .then(function() {
                api.waitForId("_PUBLICATIONS_PUBLIC_REPOSITORIES_PUBLICATIONS_COLLECTION")
           })
           .then(function() {
                api.waitForId("STOCK")
           })
           // Stock Catalog and Stock Orders might be disabled.
           // This is why it is skipped for the test.
           .then(function() {
                api.waitForId("USER_PROFILE")
           })
           .then(function() {
                api.waitForId("SAMPLE_BROWSER")
           })
           .then(function() {
                api.waitForId("VOCABULARY_BROWSER")
           })
           .then(function() {
                api.waitForId("ADVANCED_SEARCH")
           })
           .then(function() {
                api.waitForId("STORAGE_MANAGER")
           })
           .then(function() {
                api.waitForId("USER_MANAGER")
           })
           .then(function() {
                api.waitForId("TRASHCAN")
           })
           .then(function() {
                api.waitForId("SETTINGS")
           })
           .catch(error => { alert(error);});
    }
}