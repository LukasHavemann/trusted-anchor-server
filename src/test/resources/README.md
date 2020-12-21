openssl genrsa -des3 -out myCA.key 2048

openssl req -x509 -new -nodes -key myCA.key -sha256 -days 1825 -out myCA.pem

passwort: test



https://serverfault.com/questions/979094/openssl-keeps-creating-v1-certificate-instead-of-v3