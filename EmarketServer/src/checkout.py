import uuid
import base64
import json
import rsa
from datetime import datetime

from src.database.DB import DB
from src.utils import *

VOUCHER_DISCOUNT = 15
VOUCHER_GENERATION_THRESHOLD = 100

def checkout(db: DB, validation: tuple) -> dict:
  (user, products, voucher, to_discount) = validation

  # Calculate total price
  total = sum(map(lambda p: p['price'] * p['quantity'], products))
  discounted = min(user['amountToDiscount'], total) if to_discount else None

  transaction = {
    'products': products,
    'voucher': voucher,
    'discounted': discounted,
    'total': total,
    'date': datetime.now().strftime(DATE_FORMAT)
  }

  db.addUserTransaction(user['uuid'], transaction)
  discounted = discounted if discounted != None else 0.0

  # Add vouchers and recalculate the total spent
  total_paid = total - discounted
  previous_total_spent = user['totalSpent']
  total_spent = previous_total_spent + total_paid
  spent_since_last_voucher_generated = total_paid + (previous_total_spent % VOUCHER_GENERATION_THRESHOLD)
  for _ in range(int(spent_since_last_voucher_generated // VOUCHER_GENERATION_THRESHOLD)):
    new_voucher = {'id': str(uuid.uuid4()), 'discount': VOUCHER_DISCOUNT}
    db.addUserVoucher(user['uuid'], new_voucher)

  # Calculate amount to discount
  amount_to_discount = user.get('amountToDiscount') - discounted
  if voucher != None:
    amount_to_discount += total_paid * voucher['discount']/100
    db.removeUserVoucher(user['uuid'], voucher['id'])

  db.updateUserValues(user['uuid'], {'totalSpent': total_spent, 'amountToDiscount': amount_to_discount})

  return {'success': 'You are free to go!', 'total': total_paid}


def validateCheckout(db: DB, data: dict) -> str:
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
  user = db.findUserById(uid)
  if (user == None): 'User not found!'

  # Verify signature
  signatureDecoded = base64.b64decode(data['signature'].encode())
  key = getUserPublicKey(db, uid)
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

    p = db.findProductById(product['uuid'])
    if p == None: return 'Product not found!'
    if p['price'] != product['price']: return 'Product price changed!'
    product['name'] = p['name']

  return (user, products, voucher, to_discount)