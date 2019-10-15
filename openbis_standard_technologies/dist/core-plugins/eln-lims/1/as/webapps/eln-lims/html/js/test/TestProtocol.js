var TestProtocol = new function () {

    this.start = function() {
        testChain = Promise.resolve();
        testChain.then(() => this.login())
                 .then(() => this.inventorySpace())
                 .then(() => this.enableBacteriaToShowInDropDowns());
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

        var ids = ["tree",
                   "LAB_NOTEBOOK",
                   "INVENTORY",
                   "MATERIALS",
                   "_MATERIALS_BACTERIA_BACTERIA_COLLECTION",
                   "_MATERIALS_CELL_LINES_CELL_LINE_COLLECTION",
                   "_MATERIALS_FLIES_FLY_COLLECTION",
                   "_MATERIALS_PLANTS_PLANT_COLLECTION",
                   "_MATERIALS_PLASMIDS_PLASMID_COLLECTION",
                   "_MATERIALS_POLYNUCLEOTIDES_OLIGO_COLLECTION",
                   "_MATERIALS_POLYNUCLEOTIDES_RNA_COLLECTION",
                   "_MATERIALS_REAGENTS_ANTIBODY_COLLECTION",
                   "_MATERIALS_REAGENTS_CHEMICAL_COLLECTION",
                   "_MATERIALS_REAGENTS_ENZYME_COLLECTION",
                   "_MATERIALS_REAGENTS_MEDIA_COLLECTION",
                   "_MATERIALS_REAGENTS_SOLUTION_BUFFER_COLLECTION",
                   "_MATERIALS_YEASTS_YEAST_COLLECTION",
                   "METHODS",
                   "_METHODS_PROTOCOLS_GENERAL_PROTOCOLS",
                   "_METHODS_PROTOCOLS_PCR_PROTOCOLS",
                   "_METHODS_PROTOCOLS_WESTERN_BLOTTING_PROTOCOLS",
                   "PUBLICATIONS",
                   "_PUBLICATIONS_PUBLIC_REPOSITORIES_PUBLICATIONS_COLLECTION",
                   "STOCK",
                   "USER_PROFILE",
                   "SAMPLE_BROWSER",
                   "VOCABULARY_BROWSER",
                   "ADVANCED_SEARCH",
                   "STORAGE_MANAGER",
                   "USER_MANAGER",
                   "TRASHCAN",
                   "SETTINGS"];

        for (let i = 0, chain = Promise.resolve(); i < ids.length; i++) {
            chain = chain.then(function() {
                console.log(ids[i]);
                return api.waitForId(ids[i]);
            }).catch(error => { alert(error);});
        }
    }

    this.enableBacteriaToShowInDropDowns = function() {
        var api = EventUtil;

        api.waitForId("SETTINGS")
            .then(function() {
                api.click("SETTINGS");
            })
            .catch(error => { alert(error);});
        }
}