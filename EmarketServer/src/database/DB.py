import os
import pymongo
from dotenv import load_dotenv

# Metaclass to make sure there is only one instance of the DB class
class DBMeta(type):
  _instances = {}
  def __call__(cls, *args, **kwargs):
    if cls not in cls._instances:
      instance = super().__call__(*args, **kwargs)
      cls._instances[cls] = instance
    return cls._instances[cls]

# Singleton class to connect to the database
# Only one instance of this class can exist, so when DB() is called, 
# an instance is created if it doesn't exist or the existing instance is returned
class DB(metaclass=DBMeta):
  _name = 'Emarket'

  def __init__(self):
    load_dotenv('.env')
    mongo_uri = os.environ.get('MONGO_URI') or 'mongodb://localhost:27017/'
    self._client = pymongo.MongoClient(mongo_uri)
    dblist = self._client.list_database_names()
    if DB._name not in dblist:
      print(f"Creating database {DB._name}.")
    self._db = self._client[DB._name]
    self._users = self._db['users']
    self._products = self._db['products']

  # ------------- Users --------------

  def addUser(self, uuid, pubKey: str, cardNo: str):
    user = {
      'uuid': uuid,
      'pubKey': pubKey,
      'cardNo': cardNo,
      'transactions': [],
      'vouchers': []
    }
    res = self._users.insert_one(user)
    print(f"Inserted user with id {uuid}")
    return res.inserted_id

  def addUserTransaction(self, uuid, transaction: dict):
    res = self._users.update_one(
      {'uuid': uuid},
      {'$push': {'transactions': transaction}}
    )
    print(f"Updated user with id {uuid}")
  
  def findUserByKey(self, key) -> dict:
    return self._users.find_one({'pubKey': key})

  def findUserById(self, id) -> dict:
    return self._users.find_one({'uuid': id})


  # ------------ Products ------------

  def addProduct(self, data: dict) -> dict:
    res = self._products.insert_one({**data})
    return res.inserted_id

  def findProductById(self, id) -> dict:
    return self._products.find_one({'uuid': id})