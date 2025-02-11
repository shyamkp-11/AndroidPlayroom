# Make it work when executed from any location
openssl pkcs8 -topk8 -inform pem -in githubplayroom_private_key.pem -outform pem -nocrypt -out githubplayroom_private_pkcs8.pem