#!/bin/sh

if [ "$AUTH_TYPE" = "BASIC" ]; then
  if [ -z "$BASIC_AUTH_USER" ] || [ -z "$BASIC_AUTH_PASSWORD" ]; then
    echo "BASIC_AUTH_USER and BASIC_AUTH_PASSWORD must be set when AUTH_TYPE is BASIC"
    exit 1
  fi

  htpasswd -bc /etc/nginx/.htpasswd "${BASIC_AUTH_USER}" "${BASIC_AUTH_PASSWORD}"
fi
