# ACME Emarket Server

This is the server for the ACME Emarket project. It is a REST API that allows the customer to interact with the system via their application.

## API

The swagger documentation for the API is available at [`/swagger`](http://localhost:5000/swagger) endpoint when the server is running. 

## How to run

First of all, you need to have a MongoDB instance running. You can find more instructions on how to install and run MongoDB [here](https://docs.mongodb.com/manual/installation/).

Then you should install the dependencies and run the application.

To install the dependecies, you may run the following command:

```bash
pip install -r requirements.txt
```

You can configure the MongoDB URI by setting the `MONGO_URI` environment variable in your `.env` file.

```bash
MONGO_URI=mongodb://localhost:27017/ # default value
```

Before you run the application you should populate the database with the products corresponing to QR codes already available in the system. To do so, open a MongoDB client like MongoDB Compass and import the `products.json` file in the Emarket Server folder.

Run the following command to start the flask application exposing the REST API to the private network on port 5000 (by default):

```bash
python3 app.py
```

Register the ip address where the server is running because you will need it to configure the other apps of the system.