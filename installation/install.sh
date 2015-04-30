#!/bin/bash
read -p "Please enter desired admin username: " uname
stty -echo
read -p "Enter desired password: " passw; echo
stty echo

BAREHOST="http://127.0.0.1:5984"

curl -X PUT $BAREHOST/_config/admins/$uname -d '"'$passw'"' ; echo

git clone https://github.com/awaigand/couchdb-dbperuser-provisioning.git
(cd /vagrant/couchdb-dbperuser-provisioning; sudo npm -g install)

HOST=http://$uname:$passw@127.0.0.1:5984 # or whatever you got


curl -X PUT $HOST/_config/httpd/enable_cors -d '"true"'
curl -X PUT $HOST/_config/cors/origins -d '"*"'
curl -X PUT $HOST/_config/cors/credentials -d '"true"'
curl -X PUT $HOST/_config/cors/methods -d '"GET, PUT, POST, HEAD, DELETE"'
curl -X PUT $HOST/_config/httpd/bind_address -d '"0.0.0.0"'
curl -X PUT $HOST/_config/couch_httpd_auth/allow_persistent_cookies -d '"true"'
curl -X PUT $HOST/_config/couch_httpd_auth/timeout -d '"36000000"'


#Depending on your OS  these paths might change. Be careful!
curl -X PUT $HOST/_config/os_daemons/hone_provision_daemon -d '"/usr/bin/nodejs /usr/local/bin/couchdb-hexprovision hone_provisioning"'
curl -X PUT $HOST/_config/cors/headers -d '"accept, authorization, content-type, origin, referer, x-csrf-token,cookies"'
curl -X PUT $HOST/_config/httpd/port -d '"8080"'

curl -X POST http://$uname:$passw@localhost:8080/_restart -H"Content-Type: application/json"
