from flask import Blueprint, jsonify, request, make_response
from src.Emarket import Emarket

routes = Blueprint('routes', __name__)
emarket = Emarket()

def notJson():
  return make_response(jsonify({'error': 'Content-Type not supported!'}), 400)
def isContentJson(request):
  return request.headers.get('Content-Type') == 'application/json'
def makeResponse(res):
  if (res.get('error') is not None): return make_response(jsonify(res), 400)
  return jsonify(res)

@routes.get('/heartbeat')
def heartbeat():
  return jsonify({'status': 'ok'})

@routes.post('/register')
def register():
  if (not isContentJson(request)): return notJson()
  res = emarket.register(request.json)
  return makeResponse(res)

@routes.post('/checkout')
def checkout():
  if (not isContentJson(request)): return notJson()
  res = emarket.checkout(request.json)
  return makeResponse(res)

@routes.get('/user')
def user():
  res = emarket.getUser(request.args.get('user'))
  return makeResponse(res)

@routes.post('/user')
def userUpdate():
  if (not isContentJson(request)): return notJson()
  res = emarket.updateUser(request.json)
  return makeResponse(res)
  
@routes.post('/products/add')
def addProduct():
  if (not isContentJson(request)): return notJson()
  res = emarket.addProduct(request.json)
  return makeResponse(res)

@routes.post('/products/generate/<uuid>')
def generateProduct(uuid):
  res = emarket.generate_qr_code(uuid)
  return makeResponse(res)
  

