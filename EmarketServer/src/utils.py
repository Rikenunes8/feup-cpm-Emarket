import rsa
from src.database.DB import DB

DATE_FORMAT = "%Y/%m/%d - %H:%M:%S"

def getUserPublicKey(db: DB, uuid: str) -> rsa.PublicKey:
  user = db.findUserById(uuid)
  key : str = user.get('pubKey')
  return pkcs8ToPublicKey(key)

def pkcs8ToPublicKey(pkcs8: str) -> rsa.PublicKey:
  if (pkcs8 is None): return None
  return rsa.PublicKey.load_pkcs1_openssl_pem(pkcs8)