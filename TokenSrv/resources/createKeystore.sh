#!/bin/bash

KEYSTORE="tokensvc-keystore.jks"

STORETYPE="JKS"

STOREPASS="cloudsecurity"

KEYPASS="tokensvc-cloud"

ALIAS="tokensvc"

VALIDITY=365

KEYSIZE=2048

keytool -genkey -alias $ALIAS -dname "CN=DACI Token Service, OU=SNE Group, O=UvA, C=NL" -validity $VALIDITY -keypass $KEYPASS -keyalg "RSA" -keysize $KEYSIZE -keystore ./TokenSrv/tokenSrvFiles/$KEYSTORE -storepass $STOREPASS -storetype $STORETYPE

keytool -exportcert -file ./TokenSrv/tokenSrvFiles/"$ALIAS.crt" -keystore ./TokenSrv/tokenSrvFiles/$KEYSTORE -storepass $STOREPASS -alias $ALIAS -rfc
