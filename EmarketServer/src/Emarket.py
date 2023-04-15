import uuid
import rsa
import base64
import qrcode
import json
from datetime import datetime

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

  ''' This is not being used for the reason explained at the end of this file
  def _readKey(self, path: str, private = True) -> rsa.PrivateKey or rsa.PublicKey:
    with open(path, 'r') as f: data = f.read()
    if (private): return rsa.PrivateKey.load_pkcs1(data)
    else:         return rsa.PublicKey.load_pkcs1(data)
  '''


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
  @param date: date in format 2023/04/03 - 17:24:44
  '''
  def getUser(self, user_id : str, date : str = None) -> dict:
    user = self._db.findUserById(user_id)
    if (user == None): return {'error': 'User not found!'}
    
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


  # The following 2 comment methods allowed to generate a QR code for a product inisde this server and sign it with the private key of the server
  # This is not used in the final version of the project because the requirmenets are to **encrypt** the data with the private key of the server and not to **sign** it
  # This behavior is not easy to deal with this implementation since the rsa module doesn't seem to like encrypting data with the privte key and, on the customer side, it was not being possible to decrypt it
  '''
  def addProduct(self, data: dict) -> dict:
    product_uuid = str(uuid.uuid4())
    name = data.get('name')
    if (name is None or not isinstance(name, str)): 
      return {'error': 'Missing name property or invalid type!'}
    price = data.get('price')
    if (price is None or not isinstance(price, float)): 
      return {'error': 'Missing price property or invalid type!'}
    content = {'uuid': product_uuid, 'name': name, 'price': price, 'url': data.get('url')}

    return self.generate_qr_code(product_uuid, content)
  
  def generate_qr_code(self, uid: str, content: dict = None) -> dict:
    product = self._db.findProductById(uid)
    if content == None: 
      if product == None: return {'error': 'Product not found!'}
      else: content = {'uuid': product['uuid'], 'name': product['name'], 'price': product['price'], 'url': product.get('url')}
    else:
      if product != None: return {'error': 'A product with this uuid already exists!'}
      else: self._db.addProduct(content)

    qrcode_content = {'uuid': uid}

    signature = rsa.sign(str(qrcode_content).encode(), self._privkey, 'SHA-256')
    signatureEncoded = base64.b64encode(signature).decode('utf-8')
    # qrcode_encrypted = rsa.encrypt(str(qrcode_content).encode(), self._privkey)
    # qrcode_encrypted = base64.b64encode(qrcode_encrypted).decode('utf-8')

    img = qrcode.make('data': qrcode_content, 'signature': signatureEncoded}))
    img.save(f'qrcodes/{uid}.png')
    return content
  '''