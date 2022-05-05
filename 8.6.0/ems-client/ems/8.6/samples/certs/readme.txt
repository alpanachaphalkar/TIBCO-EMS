 =================================================================
 TIBCO Enterprise Message Service
 Copyright (c) 2001-2017 TIBCO Software Inc.
 ALL RIGHTS RESERVED
 =================================================================

 #################################################################
 # Important Notice:                                             #
 #                                                               #
 # Different versions of TIBCO Enterprise Message Service may    #
 # deliver newly generated certificate samples.  While the       #
 # certificate file names are the same, the actual certificates  #
 # might be DIFFERENT. Please only use the certificates          #
 # and private keys from the same set of samples, do *not* mix   #
 # certificates samples from different versions of               #
 # TIBCO Enterprise Message Service.                             #
 #################################################################

 This directory contains sample certificates and private keys
 used by other samples and in the sample configuration files.

 *****************************************************************
 * All PKCS8, PKCS12 and other files encrypted with password:    *
 *                                                               *
 *                     "password"                                *
 *                                                               *
 *****************************************************************

 server_root.cert.pem
 server_root.key.pem
 server_root.key.p8

        These certificates represent 
	root, self-signed certificate and corresponding
        private key in encrypted PEM and PKCS8 formats.


 server.cert.pem
 server.key.pem
 server.key.p8

        These certificates represent 
        server certificate and corresponding
        private key in encrypted PEM and PKCS8 formats.
        This certificate is issued by server_root.cert.pem
        and is used in the server.


 client_root.cert.pem
 client_root.key.pem
 client_root.key.p8

        These certificates represent 
        root, self-signed certificate and corresponding
        private key in encrypted PEM and PKCS8 formats.


 client.cert.pem
 client.key.pem
 client.key.p8

        These certificates represent 
        client certificate and corresponding
        private key in encrypted PEM and PKCS8 formats.
        This certificate is issued by client_root.cert.pem
        and is used by the clients.


 client_identity.p12

        This file is a PKCS12 file including:
        - client certificate 'client.cert.pem'
        - client private key 'client.key.pem'
        - issuer certificate 'client_root.cert.pem'


 test_client_ca.pem
 test_client_cert.pem
 test_client_crl.pem
 test_client_identity.p12

        These files demonstrate certificate revocation.
        - 'test_client_ca.pem' is the root certificate.
        - 'test_client_cert.pem' is a certificate signed by the above CA
        - 'test_client_crl.pem' is the CRL for the CA with 'test_client_cert'
          revoked.
        - 'test_client_identity.p12' is a PKCS12 file includes 
          'test_client_cert.pem' and 'test_client_ca.pem'.


 emsca_https_identity.p12
 emsca_https_root.cert.pem
 
        These files can be used to demonstrate configuring https for EMSCA.
        - 'emsca_https_identity.p12' is an identity file to be used with the
          --https-identity parameter of EMSCA. Because it holds CN=localhost,
          and has localhost among its Subject Alternative Names, EMSCA can be
          reached only through https://localhost:<port>.
        - 'emsca_https_root.cert.pem' is the corresponding self-signed root
          certificate to be used by Web browsers connecting to EMSCA through
          https.


  All certificates, private keys, PKCS8 and PKCS12 files have
  been generated with the OpenSSL toolkit.

