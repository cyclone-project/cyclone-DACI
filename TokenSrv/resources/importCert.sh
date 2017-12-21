#!/bin/bash

ALIAS=$1

CERTFILE=$2

KEYPASS="trusted"

STORETYPE="JKS"

KEYSTORE='trusted-keystore.jks'

STOREPASS="trusted"



keytool -importcert -noprompt -alias "$ALIAS" -file tokenSrvFiles/"$CERTFILE" -keypass $KEYPASS -storetype $STORETYPE -storepass $STOREPASS -keystore tokenSrvFiles/$KEYSTORE
