import os
import pymongo
from dotenv import load_dotenv

class DB():
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

  def addUser(self, uuid, pubKey: str, name: str, nick: str, cardNo: str):
    user = {
      'uuid': uuid,
      'name': name,
      'nickname': nick,
      'pubKey': pubKey,
      'cardNo': cardNo,
      'totalSpent': 0,
      'amountToDiscount': 0,
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

  def addUserVoucher(self, uuid, voucher):
    res = self._users.update_one(
      {'uuid': uuid},
      {'$push': {'vouchers': voucher}}
    )
    print(f"Updated user with id {uuid}")

  def removeUserVoucher(self, uuid, voucher_id):
    res = self._users.update_one(
      {'uuid': uuid},
      {'$pull': {'vouchers': {'id': voucher_id}}}
    )
    print(f"Updated user with id {uuid}")

  def updateUserValues(self, uuid, values):
    res = self._users.update_one(
      {'uuid': uuid},
      {'$set': values}
    )
    print(f"Updated user with id {uuid} with values {values}")
  

  def findUserByKey(self, key) -> dict:
    return self._users.find_one({'pubKey': key})

  def findUserById(self, id) -> dict:
    return self._users.find_one({'uuid': id})
  
  def findUserByNickname(self, nickname) -> dict:
    return self._users.find_one({'nickname': nickname})



  # ------------ Products ------------

  def addProduct(self, data: dict) -> dict:
    res = self._products.insert_one({**data})
    return res.inserted_id

  def findProductById(self, id) -> dict:
    return self._products.find_one({'uuid': id})
  
