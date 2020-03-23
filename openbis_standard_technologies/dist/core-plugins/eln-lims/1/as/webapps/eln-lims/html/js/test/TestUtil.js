var TestUtil = new function() {

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

    this.testPassed = function(id) {
        return new Promise(function executor(resolve, reject) {
            console.log("%cTest " + id +" passed", "color: green");
            resolve();
        });
    }

    this.testNotExist = function(id) {
        return new Promise(function executor(resolve, reject) {
            console.log("%cTest " + id +" is not exist", "color: grey");
            resolve();
        });
    }

    this.testLocally = function(id) {
        return new Promise(function executor(resolve, reject) {
            console.log("%cTest " + id +" should be tested locally", "color: blue");
            resolve();
        });
    }

    this.allTestsPassed = function() {
        return new Promise(function executor(resolve, reject) {
            alert("Tests passed!");
            resolve();
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

    this.setFile = function(name, url, mimeType) {
        var _this = this;
        return new Promise(function executor(resolve, reject) {
                _this.fetchBytes(url, function(file) {
                file.name = name;
                mainController.currentView.typeAndFileController.setFile(file);
                resolve();
            });
        });
    }

    this.fetchBytes = function(url, action) {
	    var xhr = new XMLHttpRequest();
        xhr.open('GET', url, true);
        xhr.responseType = 'blob';

        xhr.onload = function(e) {
            if (this.status == 200) {
            // get binary data as a response
            action(this.response);
            }
        };

        xhr.send();
    }

    this.ckeditorSetData = function(id, data) {
        return new Promise(function executor(resolve, reject) {
            editor = CKEditorManager.getEditorById(id);
            editor.setData(data);
            resolve();
        });
    }

    this.ckeditorTestData = function(id, data) {
        return new Promise(function executor(resolve, reject) {
            try {
                editor = CKEditorManager.getEditorById(id);
                var realData = editor.getData();

                if (realData === data) {
                    resolve();
                } else {
                    throw "CKEditor #" + elementId + " should be equal " + data;
                }
            } catch(error) {
                reject(error);
            }
        });
    }

    this.ckeditorDropFile = function(id, fileName, url) {
        return new Promise(function executor(resolve, reject) {
            editor = CKEditorManager.getEditorById(id);
            TestUtil.fetchBytes(url, function(file) {
                editor = CKEditorManager.getEditorById(id);

                file.name = fileName;

                editor.model.enqueueChange( 'default', () => {
                    editor.execute( 'imageUpload', { file: [file] } );
                } );
                resolve();
            });
        });
    }
}