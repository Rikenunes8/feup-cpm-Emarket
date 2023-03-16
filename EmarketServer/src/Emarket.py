import uuid
import rsa
import base64

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
    print(data)
    print(data.encode('utf-8'))
    print(base64.b64decode(data.encode('utf-8')))
    if (private): return rsa.PrivateKey.load_pkcs1(data)
    else:         return rsa.PublicKey.load_pkcs1(data)


  def register(self, data: dict) -> dict:
    pubKey = data.get('pubKey')
    cardNo = data.get('cardNo')
    if (pubKey is None or cardNo is None):
      return {'error': 'Missing pubKey or cardNo property!'}
    if (self._db.findUserByKey(pubKey) != None):
      return {'error': 'A user with this public key already exists!'}

    uid = str(uuid.uuid4())
    print(uid)
    self._db.addUser(uid, pubKey, cardNo)

    pk = self.getUserPublicKey(uid)
    print(pk)
    #uidBytes = rsa.encrypt(uid.encode(), pk, 'SHA-256')
    #uid = base64.b64encode(uidBytes).decode('utf-8')


    # TODO: Encrypt uid with user pubKey?
    # TODO: Sign response with private key?
    return {'uuid': uid, 'serverPubKey': self._pubkey.save_pkcs1().decode('utf-8')}
      
  def getUserPublicKey(self, uuid: str) -> rsa.PublicKey:
    user = DB().findUserById(uuid)
    key : str = user.get('pubKey')
    print(key)
    if (key is None): return None
    return rsa.PublicKey.load_pkcs1(key)