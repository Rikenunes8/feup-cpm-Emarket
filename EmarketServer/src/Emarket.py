import uuid
import rsa
import base64
import qrcode
import json
from datetime import datetime

from src.database.DB import DB

class Emarket:

  DATE_FORMAT = "%Y/%m/%d - %H:%M:%S"


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
  
  def _validateCheckout(self, data: dict) -> str:
    # Check if data json structure is valid
    if data.get('data') == None: 'Missing data property!'
    if data.get('signature' == None): 'Missing signature property!'
    paymentStr = data['data']
    payment = json.loads(paymentStr)
    uid = payment.get('userUUID')
    basket = payment.get('basket')
    if uid == None: 'Missing userUUID property!'
    if basket == None: 'Missing basket property!'

    # Check if user with uuid exists
    user = DB().findUserById(uid)
    if (user == None): 'User not found!'

    # Verify signature
    signatureDecoded = base64.b64decode(data['signature'].encode())
    key = self._getUserPublicKey(uid)
    try: rsa.verify(paymentStr.encode(), signatureDecoded, key)
    except: return 'Signature verification failed!'

    # Check if to_discount is valid
    to_discount = basket.get('toDiscount')
    if to_discount == None: return 'Missing toDiscount property!'

    # Check if voucher is valid
    voucher = None
    voucher_id = basket.get('voucher')
    if voucher_id != None:
      userVouchers = user.get('vouchers')
      vouchers = list(filter(lambda v: v['id'] == voucher_id, userVouchers))
      if len(vouchers) == 0: return 'Voucher not found!'
      elif len(vouchers) > 1: return 'Multiple vouchers found!'
      voucher = vouchers[0]
        
    # Check if products are valid
    products = basket.get('products')
    if products == None: return 'Missing products property!'
    if len(products) == 0: return 'Basket is empty!'
    for product in products:
      if product.get('uuid') == None: return 'Missing id property in product!'
      if product.get('price') == None: return 'Missing price property in product!'
      if product.get('quantity') == None: return 'Missing quantity property in product!'

      p = DB().findProductById(product['uuid'])
      if p == None: return 'Product not found!'
      if p['price'] != product['price']: return 'Product price changed!'
      product['name'] = p['name']

    return (user, products, voucher, to_discount)
  
  def checkout(self, data : dict) -> dict:
    validation = self._validateCheckout(data)
    if (type(validation) == str): return {'error': validation}
    else: (user, products, voucher, to_discount) = validation

    # Calculate total price
    total = sum(map(lambda p: p['price'] * p['quantity'], products))
    discounted = min(user['amountToDiscount'], total) if to_discount else None

    transaction = {
      'products': products,
      'voucher': voucher,
      'discounted': discounted,
      'total': total,
      'date': datetime.now().strftime(self.DATE_FORMAT)
    }

    DB().addUserTransaction(user['uuid'], transaction)
    discounted = discounted if discounted != None else 0.0

    # Add vouchers and recalculate the total spent
    total_paid = total - discounted
    previous_total_spent = user['totalSpent']
    total_spent = previous_total_spent + total_paid
    spent_since_last_voucher_generated = total_paid + (previous_total_spent % 100)
    for _ in range(int(spent_since_last_voucher_generated // 100)):
      new_voucher = {'id': str(uuid.uuid4()), 'discount': 15}
      DB().addUserVoucher(user['uuid'], new_voucher)

    # Calculate amount to discount
    amount_to_discount = user.get('amountToDiscount') - discounted
    if voucher != None:
      amount_to_discount += total_paid * voucher['discount']/100
      DB().removeUserVoucher(user['uuid'], voucher['id'])

    DB().updateUserValues(user['uuid'], {'totalSpent': total_spent, 'amountToDiscount': amount_to_discount})

    return {'success': 'You are free to go!', 'total': total_paid}

  '''
  Get user data. If date is provided, only transactions after that date will be returned.
  @param user_id: user id
  @param date: date in format 2023/04/03 - 17:24
  '''
  def getUser(self, user_id : str, date : str = None) -> dict:
    user = self._db.findUserById(user_id)
    if (user == None): return {'error': 'User not found!'}
    
    transactions = self.getTransactions(user)
    if (date != None):
      # check date format
      try: date = datetime.strptime(date, self.DATE_FORMAT)
      except: return {'error': 'Invalid date format!'}

      if date > datetime.now():
        return {'error': 'Invalid date. Provide a date representing the past!'}
      transactions['transactions'] = list(filter(lambda t: datetime.strptime(t['date'], self.DATE_FORMAT) > date, transactions['transactions']))

    vouchers = self.getVouchers(user)
    amount_to_discount = self.getAmountToDiscount(user)
    total_spent = self.getTotalSpent(user)

    return {**transactions, **vouchers, **amount_to_discount, **total_spent}
  def getTransactions(self, user : dict):
    return { 'transactions': user.get('transactions', []) }
  def getVouchers(self, user : dict):
    return { 'vouchers': user.get('vouchers', []) }
  def getAmountToDiscount(self, user : dict):
    return { 'amountToDiscount': user.get('amountToDiscount', 0) }
  def getTotalSpent(self, user : dict):
    return { 'totalSpent': user.get('totalSpent', 0) }

  def updateUser(self, data : dict) -> dict:
    if data.get('data') == None: return {'error': 'Missing data property!'}
    if data.get('signature' == None): return {'error': 'Missing signature property!'}
    
    userStr = data['data']
    user = json.loads(userStr)
    uid = user.get('id')
    cardNumber = user.get('cardNumber')
    if uid == None: return {'error': 'Missing id property!'}
    if cardNumber == None: return {'error': 'Missing cardNumber property!'}

    signatureDecoded = base64.b64decode(data['signature'].encode())
    key = self._getUserPublicKey(uid)
    try: rsa.verify(userStr.encode(), signatureDecoded, key)
    except: return {'error': 'Signature verification failed!'}

    self._db.updateUserValues(uid, {'cardNo': cardNumber})
    return {'success': 'User updated!', 'user': user}

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

    signature = rsa.sign(str(content).encode(), self._privkey, 'SHA-256')
    signatureEncoded = base64.b64encode(signature).decode('utf-8')

    img = qrcode.make(str({'product': str(content), 'signature': signatureEncoded}))
    img.save(f'qrcodes/{uid}.png')
    return content
    