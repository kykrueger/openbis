#!/usr/bin/env python
import tornado.web
import tornado.ioloop
import json
import os
import pwd
import ssl
import sys
import click
from pybis import Openbis


class CreateNotebook(tornado.web.RequestHandler):

    def set_default_headers(self):
        self.set_header("Access-Control-Allow-Origin", "*")
        self.set_header("Access-Control-Allow-Headers", "x-requested-with")
        self.set_header('Access-Control-Allow-Methods', 'POST, GET, OPTIONS')

    def get(self):
        self.write('some get')

    def options(self):
        # no body
        self.set_status(204)
        self.finish()

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
            self.create_user(username)
            #self.send_error(401, message="User {} does not exist on host system".format(username))

        path_to_notebook = os.path.join(
            user.pw_dir, 
            folder,
            filename
        )

        # create necessary directories
        os.makedirs(os.path.dirname(path_to_notebook), exist_ok=True)
        
        # add sequence to the filename if file already exists
        filename_name_end = filename.rfind('.')
        filename_name = filename[:filename_name_end]
        filename_extension = filename[filename_name_end:]
        filename_new = filename_name + filename_extension

        path_to_notebook_new = os.path.join(
            user.pw_dir, 
            folder,
            filename_new
        )

        i = 1
        while os.path.isfile(path_to_notebook_new):
            i += 1
            filename_new = filename_name + " " + str(i) + filename_extension
            path_to_notebook_new = os.path.join(
                user.pw_dir, 
                folder,
                filename_new
            )
        path_to_notebook = path_to_notebook_new

        with open(path_to_notebook, 'wb') as f:
            f.write(content)
        os.chown(path_to_notebook, user.pw_uid, user.pw_gid)
        os.chmod(path_to_notebook, 0o777)
        path_to_notebook_folder = os.path.join(
            user.pw_dir, 
            folder
        )
        os.chmod(path_to_notebook_folder, 0o777)
        print(path_to_notebook)
        
        link_to_notebook = {
            "fileName": filename_new
        }
        self.write(json.dumps(link_to_notebook))

    def create_user(self, username):
        os.system("useradd " + username)

    def send_error(self, status_code=500, **kwargs):
        self.set_status(status_code)
        self.write(json.dumps(kwargs))

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

@click.command()
@click.option('--port', default=8123, help='Port where this server listens to')
@click.option('--ssl-cert', '--cert', multiple=True, default='cert.pem', help='Path to your cert-file in PEM format')
@click.option('--ssl-key', '--key', multiple=True, default='key.pem', help='Path to your key-file in PEM format')
@click.option('--openbis', default='https://localhost:8443', help='URL and port of your openBIS installation')
def start_server(port, cert, key, openbis):
    o = Openbis(url=openbis, verify_certificates=False)

    application = make_app(o)
    application.listen(
        port,
        ssl_options={
            "certfile": cert,
            "keyfile":  key
        }
    )
    tornado.ioloop.IOLoop.current().start()


if __name__ == "__main__":
    start_server()
