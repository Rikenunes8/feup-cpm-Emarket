import uuid
import rsa
import base64
import json

from src.database.DB import DB
from src.checkout import checkout as checkout_, validateCheckout
from src.user_information import userInformation, updateUserInformation
from src.utils import *

class Emarket:

  def __init__(self) -> None:
    self._db = DB()
    self._certificate = self._readCertificate('certificate.pem')

  def _readCertificate(self, path) -> str:
    with open(path, 'r') as f: data = f.read()
    return data


  def register(self, data: dict) -> dict:
    pubKeyPKCS8 = data.get('pubKey')
    cardNo = data.get('cardNo')
    if (pubKeyPKCS8 is None or cardNo is None):
      return {'error': 'Missing pubKey or cardNo property!'}
    if (self._db.findUserByKey(pubKeyPKCS8) != None):
      return {'error': 'A user with this public key already exists!'}

    uid = str(uuid.uuid4())
    self._db.addUser(uid, pubKeyPKCS8, cardNo)

    pubKey = pkcs8ToPublicKey(pubKeyPKCS8)
    uidEncrypted = rsa.encrypt(uid.encode(), pubKey)
    uidEncoded = base64.b64encode(uidEncrypted).decode('utf-8')

    return {'uuid': uidEncoded, 'certificate': self._certificate}
  
  def checkout(self, data : dict) -> dict:
    validation = validateCheckout(self._db, data)
    if (type(validation) == str): return {'error': validation}
    return checkout_(self._db, validation)

  '''
  Get user data. If date is provided, only transactions after that date will be returned.
  @param user_id: user id
  @param signature: signature of the user id
  @param date: date in format 2023/04/03 - 17:24:44
  '''
  def getUser(self, user_id : str, signature : str, date : str = None) -> dict:
    user = self._db.findUserById(user_id)
    if (user == None): return {'error': 'User not found!'}
    if (signature == None): return {'error': 'Missing signature property!'}
    
    signatureDecoded = base64.b64decode(signature.encode())
    key = getUserPublicKey(self._db, user_id)
    try: rsa.verify(user_id.encode(), signatureDecoded, key)
    except: return {'error': 'Signature verification failed!'}
    
    return userInformation(user, date)
  

  def updateUser(self, data : dict) -> dict:
    if data.get('data') == None: return {'error': 'Missing data property!'}
    if data.get('signature' == None): return {'error': 'Missing signature property!'}
    
    userStr = data['data']
    user = json.loads(userStr)
    uid = user.get('id')
    if uid == None: return {'error': 'Missing id property!'}

    signatureDecoded = base64.b64decode(data['signature'].encode())
    key = getUserPublicKey(self._db, uid)
    try: rsa.verify(userStr.encode(), signatureDecoded, key)
    except: return {'error': 'Signature verification failed!'}

    return updateUserInformation(self._db, user)
  
  def getProduct(self, uuid: str) -> dict:
    product = self._db.findProductById(uuid)
    if product is None:
      return {'error': 'Product not found!'}
    else:
      # Check if all the required fields are present in the document
      if 'name' not in product or 'price' not in product or 'url' not in product:
        return {'error': 'Product data is incomplete!'}
      else:
        return {'product': {'uuid': product['uuid'], 'name': product['name'], 'price': product['price'], 'url': product['url']}}
