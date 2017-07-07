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
            self.create_user(username)
            #self.send_error(401, message="User {} does not exist on host system".format(username))

        notebook_file = os.path.join(
            user.pw_dir, 
            folder,
            filename
        )

        # create necessary directories
        os.makedirs(os.path.dirname(notebook_file), exist_ok=True)
        
        # add sequence to the filename if file already exists
        notebook_file_new = notebook_file
        i = 1
        while os.path.isfile(notebook_file_new):
            i += 1
            notebook_file_new = "{} {}".format(notebook_file, i)
        notebook_file = notebook_file_new

        with open(notebook_file, 'wb') as f:
            f.write(content)
        os.chown(notebook_file, user.pw_uid, user.pw_gid)
        print(notebook_file)
        
        link_to_notebook = {
            "url": "http://localhost:8888/notebooks/" +  os.path.join(folder, os.path.basename(notebook_file))
        }
        self.write(json.dumps(link_to_notebook))

    def create_user(self, username):
        os.system("useradd " + username)

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

@click.command()
@click.option('--port', default=8123, help='Port where this server listens to')
@click.option('--cert', default='cert.pem', help='Path to your cert-file in PEM format')
@click.option('--key',  default='key.pem', help='Path to your key-file in PEM format')
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
