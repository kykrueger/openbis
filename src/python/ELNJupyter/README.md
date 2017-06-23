# ELN-Jupyter webservice

This is a small webservice which is running on a server and allows to write Jupyter notebook files directly into the users' home folder.

- always use utf-8
- always use https
- the body should contain the content of the Jupyter notebook. 
- respones are always in JSON
- if the POST request fails, a response with HTTP status code ≠ 200 is sent back, including an error message
- the openBIS token is always tested for validity against the openBIS server
- the user is extracted from the token
- the user must exist on the server (no automatic user creation)
- if folder or file exists on the system, it is being overwritten

## Usage

```
POST
https://servername:8123?token=[openBIS token]&folder=my_folder&filename=20160929145446460-369.ipynb

{
  "cells": [
    {
      "cell_type": "code",
      "execution_count": null,
      "metadata": {
        "collapsed": false
      },
      "outputs": [],
      "source": [
        "from pybis import Openbis\n",
        "o = Openbis(url='https://localhost:8443', verify_certificates=False)"
      ]
    },
    {
      "cell_type": "code",
      "execution_count": null,
      "metadata": {
        "collapsed": true
      },
      "outputs": [],
      "source": [
        "ds = o.get_dataset('20160929145446460-367')"
      ]
    }
  ],
  "metadata": {
    "kernelspec": {
      "display_name": "Python 3",
      "language": "python",
      "name": "python3"
    },
    "language_info": {
      "codemirror_mode": {
        "name": "ipython",
        "version": 3
      },
      "file_extension": ".py",
      "mimetype": "text/x-python",
      "name": "python",
      "nbconvert_exporter": "python",
      "pygments_lexer": "ipython3",
      "version": "3.5.2"
    }
  },
  "nbformat": 4,
  "nbformat_minor": 2
}

```

## Response from server (not yet implemented)
```
{
    "link": "http://servername:8000/home/testuser/notebooks/my_folder/20160929145446460-369.ipynb"
}
```

Using this link, the user should be directly routed to the notebook we just created.
If the user never used JupyterHub before or is logged out, he is asked for his password before being redirected to the notebook.