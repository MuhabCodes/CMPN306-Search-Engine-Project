from flask import Flask, request, render_template, redirect, url_for
import jpysocket
import socket

app = Flask(__name__)
host='localhost' #Host Name
port=6666    #Port Number
s=socket.socket() #Create Socket
s.connect((host,port)) #Connect to socket

@app.route('/', methods=['GET', 'POST'])
def my_form_post():
    if (request.method == 'POST'):
        text = request.form['searchbar']
        processed_text = text.upper()
        print(processed_text)
        msgsend=jpysocket.jpyencode(processed_text) #Encript The Msg
        s.send(msgsend) #Send Msg
        msgrecv=s.recv(1024) #Recieve msg
        msgrecv=jpysocket.jpydecode(msgrecv) #Decript msg
        print("From Server: ",msgrecv)
        return render_template('home.html')
    if (request.method == 'GET'):
        return render_template('home.html')

