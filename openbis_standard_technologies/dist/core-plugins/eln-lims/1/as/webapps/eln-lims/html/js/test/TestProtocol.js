var TestProtocol = new function () {

    this.startAdminTests = function() {
        testChain = Promise.resolve();
        testChain.then(() => AdminTests.startAdminTests())
                 .catch(error => { console.log(error) });
    }

    this.startUserTests = function() {
        testChain = Promise.resolve();
        testChain.then(() => UserTests.startUserTests())
                 .catch(error => { console.log(error) });
    }

    this.finishTests = function() {
        testChain = Promise.resolve();
        testChain.then(() => AdminTests.finishTests())
                 .catch(error => { console.log(error) });
    }
}