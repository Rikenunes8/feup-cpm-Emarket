import uuid

from src.database.DB import DB


class EmarketMeta(type):
  _instances = {}
  def __call__(cls, *args, **kwargs):
    if cls not in cls._instances:
      instance = super().__call__(*args, **kwargs)
      cls._instances[cls] = instance
    return cls._instances[cls]

class Emarket(metaclass=EmarketMeta):
  def __init__(self) -> None:
    self._db = DB()


  def regist(self, data: dict) -> dict:
    pubKey = data.get('pubKey')
    cardNo = data.get('cardNo')
    if (pubKey is None or cardNo is None):
      return {'error': 'Missing pubKey or cardNo property!'}
    
    uid = str(uuid.uuid4())
    self._db.regist(uid, pubKey, cardNo)

    # TODO: Encrypt uid with user pubKey
    # TODO: get server pubKey
    return {'uuid': uid, 'serverPubKey': 'serverPubKey'}

