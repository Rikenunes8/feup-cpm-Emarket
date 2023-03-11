from flask import Blueprint, jsonify, request

routes = Blueprint('routes', __name__)

@routes.get('/heartbeat')
def heartbeat():
  return jsonify({'status': 'ok'})