from datetime import datetime
from src.utils import DATE_FORMAT
from src.database.DB import DB

def userInformation(user: dict, date: str):
  transactions = getTransactions(user)
  if (date != None):
    # check date format
    try: date = datetime.strptime(date, DATE_FORMAT)
    except: return {'error': 'Invalid date format!'}

    if date > datetime.now():
      return {'error': 'Invalid date. Provide a date representing the past!'}
    transactions['transactions'] = list(filter(lambda t: datetime.strptime(t['date'], DATE_FORMAT) > date, transactions['transactions']))

  vouchers = getVouchers(user)
  amount_to_discount = getAmountToDiscount(user)
  total_spent = getTotalSpent(user)

  return {**transactions, **vouchers, **amount_to_discount, **total_spent}

def getTransactions(user : dict):
    return { 'transactions': user.get('transactions', []) }
def getVouchers(user : dict):
  return { 'vouchers': user.get('vouchers', []) }
def getAmountToDiscount(user : dict):
  return { 'amountToDiscount': user.get('amountToDiscount', 0) }
def getTotalSpent(user : dict):
  return { 'totalSpent': user.get('totalSpent', 0) }

def updateUserInformation(db: DB, user: dict):
  uid = user.get('id')
  cardNumber = user.get('cardNumber')
  if cardNumber == None: return {'error': 'Missing cardNumber property!'}

  db.updateUserValues(uid, {'cardNo': cardNumber})
  return {'success': 'User updated!', 'user': user}

