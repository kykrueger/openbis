import click
from datetime import datetime

def click_echo(message):
    timestamp = datetime.now().strftime("%H:%M:%S")
    click.echo("{} {}".format(timestamp, message))
