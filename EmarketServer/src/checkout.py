import uuid
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
  total_paid = round(total - discounted, 2)
  previous_total_spent = user['totalSpent']
  total_spent = round(previous_total_spent + total_paid, 2)
  spent_since_last_voucher_generated = total_paid + (previous_total_spent % VOUCHER_GENERATION_THRESHOLD)
  for _ in range(int(spent_since_last_voucher_generated // VOUCHER_GENERATION_THRESHOLD)):
    new_voucher = {'id': str(uuid.uuid4()), 'discount': VOUCHER_DISCOUNT}
    db.addUserVoucher(user['uuid'], new_voucher)

  # Calculate amount to discount
  amount_to_discount = user.get('amountToDiscount') - discounted
  if voucher != None:
    amount_to_discount += total_paid * voucher['discount']/100
    db.removeUserVoucher(user['uuid'], voucher['id'])

  db.updateUserValues(user['uuid'], {'totalSpent': total_spent, 'amountToDiscount': round(amount_to_discount, 2)})

  return {'success': 'You are free to go!', 'total': total_paid}


def validateCheckout(db: DB, origin) -> str:
  data = origin
  signature = data[:64]
  data = data[64:]
  userUUID = str(uuid.UUID(bytes=data[:16]))
  hasDiscount = data[16]
  hasVoucher = data[17]

  voucherId = None
  if (hasVoucher):
    voucherId = str(uuid.UUID(bytes=data[18:34]))
    data = data[34:]
  else:
    data = data[18:]

  products = []
  productsSize = data[0]
  data = data[1:]
  for _ in range(productsSize):
    productUUID = str(uuid.UUID(bytes=data[:16]))
    priceInteger = int.from_bytes(data[16:18], "big", signed=True)
    priceDecimal = data[18]
    quantity = data[19]
    data = data[20:]
    product = {
      "uuid": productUUID,
      "price": round(priceInteger + priceDecimal/100, 2),
      "quantity": quantity
    }
    products.append(product)


  # Check if user with uuid exists
  user = db.findUserById(userUUID)
  if (user == None): 'User not found!'

  # Verify signature
  key = getUserPublicKey(db, userUUID)
  try: rsa.verify(origin[64:], signature, key)
  except: return 'Signature verification failed!'

  # Check if voucher is valid
  voucher = None
  if voucherId != None:
    userVouchers = user.get('vouchers')
    vouchers = list(filter(lambda v: v['id'] == voucherId, userVouchers))
    if len(vouchers) == 0: return 'Voucher not found!'
    elif len(vouchers) > 1: return 'Multiple vouchers found!'
    voucher = vouchers[0]
      
  # Check if products are valid
  if len(products) == 0: return 'Basket is empty!'
  for product in products:
    p = db.findProductById(product['uuid'])
    if p == None: return 'Product not found!'
    if p['price'] != product['price']: return 'Product price changed!'
    product['name'] = p['name']

  return (user, products, voucher, hasDiscount)