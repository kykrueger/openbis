var UserTests = new function() {

    this.startUserTests = function() {
        testChain = Promise.resolve();
                 //5. User Manager (end of test)
        testChain.then(() => TestUtil.deleteCookies("suitename"))
                 .then(() => TestUtil.login("testId", "pass"))
                 .then(() => this.inventorySpaceForTestUser())
                 //6. Sample Form - Creation
                 //todo remove this comment before commit!
                 //.then(() => this.creationSampleForm())
                 //13. Inventory Table - Imports for Create - Automatic Codes
                 .then(() => this.importsAutomaticCodes())
                 //15. Sample Form - Storage
                 .then(() => this.storageTest())
                 //16. Storage Manager - Moving Box
                 .then(() => this.movingBoxTest())
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

    this.creationSampleForm = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => UserTests.createBacteria("BAC1", "Aurantimonas"))
                             .then(() => UserTests.createBacteria("BAC2", "Burantimonas"))
                             .then(() => UserTests.createBacteria("BAC3", "Curantimonas"))
                             .then(() => UserTests.createBacteria("BAC4", "Durantimonas"))
                             .then(() => resolve());
        });
    }

    this.createBacteria = function(code, name) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            var richText = '<p><strong><span style="color:#000080">F<sup>-</sup>&nbsp;tonA21 thi-1 thr-1 leuB6 lacY1</span>&nbsp;<em><span style="color:#008000">glnV44 rfbC1 fhuA1 ?? mcrB e14-(mcrA<sup>-</sup>)</span>&nbsp;</em><u><span style="color:#cc99ff">hsdR(r<sub>K</sub>&nbsp;<sup>-</sup>m<sub>K</sub>&nbsp;<sup>+</sup>) λ<sup>-</sup></span></u></strong></p>';

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("create-btn"))
                             .then(() => e.click("create-btn"))
                             .then(() => e.waitForId("sampleFormTitle"))
                             .then(() => e.equalTo("sampleFormTitle", "Create Object Bacteria", false))
                             .then(() => e.waitForId("codeId"))
                             .then(() => e.waitForFill("codeId"))
                             .then(() => e.equalTo("codeId", code, false))
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", name, false))
                             //Paste from Word
                             .then(() =>  UserTests.ckeditorSetData("BACTERIA.GENOTYPE", richText))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             //Check saving results
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.equalTo("NAME", name, false))
                             .then(() => e.equalTo("BACTERIAGENOTYPE", richText, false))
                             .then(() => resolve());
        });
    }

    this.ckeditorSetData = function(id, data) {
        return new Promise(function executor(resolve, reject) {
            CKEDITOR.instances[id].setData(data);
            CKEDITOR.instances[id].fire('change');
            resolve();
        });
    }

    this.importsAutomaticCodes = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("options-menu-btn"))
                             .then(() => e.click("options-menu-btn"))
                             .then(() => e.waitForId("register-object-btn"))
                             .then(() => e.click("register-object-btn"))
                             .
                             .then(() => resolve());
        });
    }

    this.storageTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => resolve());
        });
    }

    this.movingBoxTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => resolve());
        });
    }
}