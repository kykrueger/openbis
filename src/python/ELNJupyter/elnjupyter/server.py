import tornado.web
import tornado.ioloop
import json
import os
import pwd
import ssl
from pybis import Openbis


class CreateNotebook(tornado.web.RequestHandler):
    def get(self, msg):
        self.write(self.msg)

    def post(self, whatever):
        token = self.get_argument(name='token')
        folder = self.get_argument(name='folder')
        filename = self.get_argument(name='filename')
        content = self.request.body


        # check if token is still valid
        if not self.openbis.is_token_valid(token):
            self.send_error(401, message="token is invalid")
            return

        # extract username
        username, code = token.split('-')

        try:
            user = pwd.getpwnam(username)
        except KeyError:
            self.send_error(401, message="User {} does not exist on host system".format(username))

        notebook_file = os.path.join(
            user.pw_dir, 
            folder,
            filename
        )

        # create necessary directories
        os.makedirs(os.path.dirname(notebook_file), exist_ok=True)
        with open(notebook_file, 'wb') as f:
            f.write(content)
        os.chown(notebook_file, user.pw_uid, user.pw_gid)
        print(notebook_file)

    def send_error(self, status_code=500, **kwargs):
        self.set_status(status_code)
        self.write(json.dumps(kwargs) )

    def initialize(self, openbis):
        self.openbis = openbis
        self.set_header('Content-Type', 'application/json')

def make_app(openbis):
    """All the routing goes here...
    """
    app = tornado.web.Application([
        (r"/(.*)", CreateNotebook, {"openbis": openbis})
    ])
    return app


if __name__ == "__main__":
    openbis = Openbis(url='https://localhost:8443', verify_certificates=False)
    application = make_app(openbis)
    application.listen(
        8123,
        ssl_options={
            "certfile": "/Users/vermeul/tmp/cert.pem",
            "keyfile": "/Users/vermeul/tmp/key.pem",
        }
    )
    tornado.ioloop.IOLoop.current().start()
