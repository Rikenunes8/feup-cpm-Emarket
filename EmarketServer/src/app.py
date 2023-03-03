from flask import Flask, jsonify, request
import sqlite3

app = Flask(__name__)

@app.get('/heartbeat')
def heartbeat():
  return jsonify({'status': 'ok'})

if __name__ == '__main__':
  # Create the database table if it doesn't exist
  # db = sqlite3.connect('users.db')
  # cursor = db.cursor()
  # cursor.execute('CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, username TEXT, password TEXT)')
  # db.close()

  # Start the server
  app.run(debug=True)