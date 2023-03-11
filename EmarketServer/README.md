## API

The swagger documentation for the API is available at /swagger endpoint when the server is running. 

**NOTE:** The documentation is not self generated, so it can be outdated sometimes.

## Requirements

To install the dependecies, you may run the following command:

```bash
pip install -r requirements.txt
```

### How to run

You can have a `.env` file with the following variables:

```bash
MONGO_URI=mongodb://localhost:27017/ # default value
```

Run the following command to start the flask application exposing the REST API to the private network on port 5000 (by default):

```bash
python3 app.py
```
