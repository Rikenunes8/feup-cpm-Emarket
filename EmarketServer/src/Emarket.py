import uuid
import rsa
import base64
import qrcode
import json

from src.database.DB import DB

# Metaclass to make sure there is only one instance of the Emarket class
class EmarketMeta(type):
  _instances = {}
  def __call__(cls, *args, **kwargs):
    if cls not in cls._instances:
      instance = super().__call__(*args, **kwargs)
      cls._instances[cls] = instance
    return cls._instances[cls]

# Singleton Emarket class
# Only one instance of this class can exist, so when Emarket() is called, 
# an instance is created if it doesn't exist or the existing instance is returned
class Emarket(metaclass=EmarketMeta):
  def __init__(self) -> None:
    self._db = DB()
    # TODO: should we handle the error of not finding the keys?
    self._privkey : rsa.PrivateKey = self._readKey('private.pem', True)
    self._pubkey : rsa.PublicKey = self._readKey('public.pem', False)

  def _readKey(self, path: str, private = True) -> rsa.PrivateKey or rsa.PublicKey:
    with open(path, 'r') as f: data = f.read()
    if (private): return rsa.PrivateKey.load_pkcs1(data)
    else:         return rsa.PublicKey.load_pkcs1(data)
  
  def _getUserPublicKey(self, uuid: str) -> rsa.PublicKey:
    user = DB().findUserById(uuid)
    key : str = user.get('pubKey')
    return self._pkcs8ToPublicKey(key)
  
  def _pkcs8ToPublicKey(self, pkcs8: str) -> rsa.PublicKey:
    if (pkcs8 is None): return None
    return rsa.PublicKey.load_pkcs1_openssl_pem(pkcs8)


  def register(self, data: dict) -> dict:
    pubKeyPKCS8 = data.get('pubKey')
    cardNo = data.get('cardNo')
    if (pubKeyPKCS8 is None or cardNo is None):
      return {'error': 'Missing pubKey or cardNo property!'}
    if (self._db.findUserByKey(pubKeyPKCS8) != None):
      return {'error': 'A user with this public key already exists!'}

    uid = str(uuid.uuid4())
    self._db.addUser(uid, pubKeyPKCS8, cardNo)

    pubKey = self._pkcs8ToPublicKey(pubKeyPKCS8)
    uidEncrypted = rsa.encrypt(uid.encode(), pubKey)
    uidEncoded = base64.b64encode(uidEncrypted).decode('utf-8')

    print(self._pubkey.save_pkcs1().decode())

    return {'uuid': uidEncoded, 'serverPubKey': self._pubkey.save_pkcs1().decode('utf-8')}
  
  def checkout(self, data : dict) -> dict:
    if (data.get('data') == None): return {'error': 'Missing data property!'}
    if (data.get('signature' == None)): return {'error': 'Missing signature property!'}

    paymentStr = data['data']
    payment = json.loads(paymentStr)
    if (payment.get('userUUID') == None): return {'error': 'Missing userUUID property!'}
    if (payment.get('transaction') == None): return {'error': 'Missing transaction property!'}

    signatureDecoded = base64.b64decode(data['signature'].encode())
    uid = payment['userUUID']
    key = self._getUserPublicKey(uid)

    try: rsa.verify(paymentStr.encode(), signatureDecoded, key)
    except: return {'error': 'Signature verification failed!'}

    DB().addUserTransaction(uid, payment['transaction'])
    return {'success': 'You are free to go!'}

  def getTransactions(self, user_id : str):
    user = self._db.findUserById(user_id)
    if (user == None): return {'error': 'User not found!'}
    return { 'transactions': user.get('transactions', []) }

  def addProduct(self, data: dict) -> dict:
    uuid = data.get('uuid')
    if (uuid is None): return {'error': 'Missing uuid property!'}
    name = data.get('name')
    if (name is None or not isinstance(name, str)): 
      return {'error': 'Missing name property or invalid type!'}
    price = data.get('price')
    if (price is None or not isinstance(price, float)): 
      return {'error': 'Missing price property or invalid type!'}
    content = {'uuid': uuid, 'name': name, 'price': price}

    if (self._db.findProductById(uuid) != None):
      return {'error': 'A product with this uuid already exists!'}
    self._db.addProduct(content)
    
    signature = rsa.sign(str(content).encode(), self._privkey, 'SHA-256')
    signatureEncoded = base64.b64encode(signature).decode('utf-8')

    img = qrcode.make(str({'product': str(content), 'signature': signatureEncoded}))
    img.save(f'qrcodes/{uuid}.png')
    return content
    