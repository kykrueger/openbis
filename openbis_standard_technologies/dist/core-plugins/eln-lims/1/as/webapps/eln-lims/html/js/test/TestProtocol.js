var TestProtocol = new function () {
    this.start = function() {
        this.login();
    }

    this.login = function() {
        var api = EventUtil;
        api.waitForId("username", function() {
            api.write("username", "admin");
            api.write("password", "a");
            api.click("login-button");
        });
    }
}