var TestProtocol = new function () {

    this.start = function() {
        testChain = Promise.resolve();
        testChain.then(() => this.login())
                .catch(error => { console.log(error) });
//                 .then(() => this.inventorySpace())
//                 .then(() => this.enableBacteriaToShowInDropDowns())

    }

    this.login = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            testChain = Promise.resolve();
            testChain.then(() => e.waitForId("username"))
                        .then(() => e.write("username", "admin"))
                        .then(() => e.write("password", "a"))
                        .then(() => e.click("login-button"))
                        .catch(() => reject(error));
        });
    }
//
//    this.inventorySpace = function() {
//        return new Promise(function executor(resolve, reject) {
//            var api = EventUtil;
//
//            var ids = ["tree",
//                       "LAB_NOTEBOOK",
//                       "INVENTORY",
//                       "MATERIALS",
//                       "_MATERIALS_BACTERIA_BACTERIA_COLLECTION",
//                       "_MATERIALS_CELL_LINES_CELL_LINE_COLLECTION",
//                       "_MATERIALS_FLIES_FLY_COLLECTION",
//                       "_MATERIALS_PLANTS_PLANT_COLLECTION",
//                       "_MATERIALS_PLASMIDS_PLASMID_COLLECTION",
//                       "_MATERIALS_POLYNUCLEOTIDES_OLIGO_COLLECTION",
//                       "_MATERIALS_POLYNUCLEOTIDES_RNA_COLLECTION",
//                       "_MATERIALS_REAGENTS_ANTIBODY_COLLECTION",
//                       "_MATERIALS_REAGENTS_CHEMICAL_COLLECTION",
//                       "_MATERIALS_REAGENTS_ENZYME_COLLECTION",
//                       "_MATERIALS_REAGENTS_MEDIA_COLLECTION",
//                       "_MATERIALS_REAGENTS_SOLUTION_BUFFER_COLLECTION",
//                       "_MATERIALS_YEASTS_YEAST_COLLECTION",
//                       "METHODS",
//                       "_METHODS_PROTOCOLS_GENERAL_PROTOCOLS",
//                       "_METHODS_PROTOCOLS_PCR_PROTOCOLS",
//                       "_METHODS_PROTOCOLS_WESTERN_BLOTTING_PROTOCOLS",
//                       "PUBLICATIONS",
//                       "_PUBLICATIONS_PUBLIC_REPOSITORIES_PUBLICATIONS_COLLECTION",
//                       "STOCK",
//                       "USER_PROFILE",
//                       "SAMPLE_BROWSER",
//                       "VOCABULARY_BROWSER",
//                       "ADVANCED_SEARCH",
//                       "STORAGE_MANAGER",
//                       "USER_MANAGER",
//                       "TRASHCAN",
//                       "SETTINGS"];
//
//            chain = Promise.resolve();
//
//            for (let i = 0; i < ids.length; i++) {
//                chain = chain.then(function() {
//                    return api.waitForId(ids[i]);
//                }).catch(error => { reject(error)});
//            }
//
//            chain = chain.then(function() {
//                console.log("Test inventorySpace() passed.");
//                resolve();
//            });
//        });
//    }
//
//    this.enableBacteriaToShowInDropDowns = function() {
//        return new Promise(function executor(resolve, reject) {
//            var api = EventUtil;
//
//            api.waitForId("SETTINGS").then(function() {
//                api.click("SETTINGS");
//                return api.waitForId("settingsDropdown");
//            }).then(function() {
//                api.change("settingsDropdown", "/ELN_SETTINGS/GENERAL_ELN_SETTINGS");
//                return api.waitForId("edit-btn");
//            }).then(function() {
//                api.click("edit-btn");
//                // we wait for the save-button, cause page contains settings-section-sample type-BACTERIA
//                // even when page can't be edit. So we wait when page be reloaded.
//                return api.waitForId("save-btn");
//            }).then(function() {
//                api.click("settings-section-sampletype-BACTERIA");
//                return api.waitForId("BACTERIA_show_in_drop_downs");
//            }).then(function() {
//                api.checked("BACTERIA_show_in_drop_downs", true);
//                api.click("save-btn");
//            }).then(function() {
//                console.log("Test enableBacteriaToShowInDropDowns() passed.");
//                resolve();
//            })
//            .catch(error => { reject(error)});
//        });
//    }
}