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
  res = emarket.checkout(request.data)
  return makeResponse(res)

@routes.get('/user')
def user():
  res = emarket.getUser(request.args.get('user'), request.args.get('signature'), request.args.get('date'))
  return makeResponse(res)

@routes.post('/user')
def userUpdate():
  if (not isContentJson(request)): return notJson()
  res = emarket.updateUser(request.json)
  return makeResponse(res)

@routes.get('/product/<uuid>')
def getProduct(uuid):
  res = emarket.getProduct(uuid)
  return makeResponse(res)
