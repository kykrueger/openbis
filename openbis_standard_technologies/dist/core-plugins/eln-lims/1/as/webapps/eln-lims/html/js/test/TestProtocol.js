var TestProtocol = new function () {

    this.startAdmitTests = function() {
        testChain = Promise.resolve();
        testChain.then(() => this.login("admin", "a"))
                 .then(() => this.inventorySpace())
                 .then(() => this.enableBacteriaToShowInDropDowns())
                 .then(() => this.userManager())
                 .catch(error => { console.log(error) });
    }

    this.startTestUserTests = function() {
        testChain = Promise.resolve();
        testChain.then(() => this.deleteCookies("suitename"))
                 .then(() => this.login("testId", "pass"))
                 .then(() => this.inventorySpaceForTestUser())
                 .catch(error => { console.log(error) });
    }

    this.login = function(username, password) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            testChain = Promise.resolve();
            testChain.then(() => e.waitForId("username"))
                     .then(() => e.write("username", username))
                     .then(() => e.write("password", password))
                     .then(() => e.click("login-button"))
                     .then(() => resolve())
                     .catch(() => reject(error));
        });
    }

    this.inventorySpace = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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

            Promise.resolve().then(() => TestProtocol.verifyInventory(ids)).then(() => resolve());
        });
    }

    this.enableBacteriaToShowInDropDowns = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();
            testChain.then(() => e.waitForId("SETTINGS"))
                     .then(() => e.click("SETTINGS"))
                     .then(() => e.waitForId("settingsDropdown"))
                     .then(() => e.change("settingsDropdown", "/ELN_SETTINGS/GENERAL_ELN_SETTINGS"))
                     .then(() => e.waitForId("edit-btn"))
                     .then(() => e.click("edit-btn"))
                     // we wait for the save-button, cause page contains settings-section-sample type-BACTERIA
                     // even when page can't be edit. So we wait when page be reloaded.
                     .then(() => e.waitForId("save-btn"))
                     .then(() => e.click("settings-section-sampletype-BACTERIA"))
                     .then(() => e.waitForId("BACTERIA_show_in_drop_downs"))
                     .then(() => e.checked("BACTERIA_show_in_drop_downs", true))
                     .then(() => e.click("save-btn"))
                     // wait until the save
                     .then(() => e.waitForId("edit-btn"))
                     .then(() => resolve())
                     .catch(() => reject(error));
        });
    }

    this.userManager = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();
            testChain.then(() => e.waitForId("USER_MANAGER"))
                     .then(() => e.click("USER_MANAGER"))
                     .then(() => e.waitForId("optionsMenu"))
                     .then(() => e.click("optionsMenu"))
                     .then(() => e.waitForId("createUser"))
                     .then(() => e.click("createUser"))
                     .then(() => e.waitForId("userId"))
                     .then(() => e.change("userId", "testId"))
                     .then(() => e.click("createUserBtn"))
                     .then(() => TestProtocol.createPassword())
                     .then(() => TestProtocol.userExist())
                     .then(() => TestProtocol.setCookies("suitename", "testId"))
                     .then(() => e.click("logoutBtn"))
                     .then(() => resolve())
                     .catch(() => reject(error));
        });
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

            Promise.resolve().then(() => TestProtocol.verifyInventory(ids))
                             .then(() => e.verifyExistence("USER_MANAGER", false))
                             .then(() => resolve());
        });
    }

    this.setCookies = function(key, value) {
        return new Promise(function executor(resolve, reject) {
            $.cookie(key, value);
            resolve();
        });
    }

    this.deleteCookies = function(key) {
        return new Promise(function executor(resolve, reject) {
            $.removeCookie(key);
            resolve();
        });
    }

    this.verifyInventory  = function(ids) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            chain = Promise.resolve();
            for (let i = 0; i < ids.length; i++) {
                chain = chain.then(() => e.waitForId(ids[i])).catch(error => { reject(error)});
            }
            chain.then(() => resolve());
        });
    }

    // Sometimes it ask for password. Try to fill it.
    this.createPassword = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();

            testChain.then(() => e.waitForId("passwordId", true, 2000))
                     .then(() => e.change("passwordId", "pass", true))
                     .then(() => e.change("passwordRepeatId", "pass", true))
                     .then(() => e.click("createUserBtn", true))
                     .then(() => resolve())
                     .catch(() => reject(error));
        });
    }

    // If the user already exists, we will see an error.
    // This is not a problem for the script, and we can continue.
    this.userExist = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();

            testChain.then(() => e.waitForId("jError", true, 2000))
                     .then(() => e.waitForId("jNotifyDismiss", true, 2000))
                     .then(() => e.click("jNotifyDismiss", true))
                     .then(() => e.click("cancelBtn", true))
                     .then(() => resolve())
                     .catch(() => reject(error));
        });
    }
}