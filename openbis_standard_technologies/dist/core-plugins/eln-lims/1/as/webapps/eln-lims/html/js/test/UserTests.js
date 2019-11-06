var UserTests = new function() {

    this.startUserTests = function() {
        testChain = Promise.resolve();
                 //5. User Manager (end of test)
        testChain.then(() => TestUtil.deleteCookies("suitename"))
                 .then(() => TestUtil.login("testId", "pass"))
                 .then(() => this.inventorySpaceForTestUser())
                 //6. Sample Form - Creation
                 .then(() => this.creationSampleForm())
                 .catch(error => { console.log(error) });
    }


    this.inventorySpaceForTestUser = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            var ids = ["LAB_NOTEBOOK",
                       "TESTID",
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
                       "_METHODS_PROTOCOLS_WESTERN_BLOTTING_PROTOCOLS"];

            Promise.resolve().then(() => TestUtil.verifyInventory(ids))
                             .then(() => e.verifyExistence("USER_MANAGER", false))
                             .then(() => resolve());
        });
    }

    this.creationSampleForm = function(key, value) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => resolve());
        });
    }

}