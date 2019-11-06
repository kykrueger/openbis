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
}