I2PControl
Server implementing JSON-RPC2 API for remote control of I2P.

See i2pcontrol.py for a test client.
Default host is 127.0.0.1.
Default port is 7650.
Default password is "itoopie".

You may change the API password via the API,
or via a browser at https://127.0.0.1:7650/

Version 1 API specification:
http://i2p-projekt.i2p/en/docs/api/i2pcontrol
https://geti2p.net/en/docs/api/i2pcontrol

Version 2 API proposal:
http://i2p-projekt.i2p/spec/proposals/118-i2pcontrol-api-2
https://geti2p.net/spec/proposals/118-i2pcontrol-api-2

To build as a router console plugin: ant
To build as a router console webapp: ant war

Command line test client:
scripts/i2pcontrol.py in this package

GUI client itoopie version 0.3 (2015-03-02):
Clearnet installer: https://github.com/robertfoss/itoopie.net/raw/master/files/itoopie-install.exe
Clearnet SHA512: https://raw.githubusercontent.com/robertfoss/itoopie.net/master/files/itoopie-install.exe.sha512
I2P installer: http://stats.i2p/i2p/plugins/others/itoopie-install.exe
I2P SHA512: http://stats.i2p/i2p/plugins/others/itoopie-install.exe.sha512
Source: i2p.itoopie branch in monotone, or https://github.com/i2p/i2p.itoopie
java -jar itoopie-install.exe to install on non-Windows.

Discussion forum:
http://zzz.i2p/forums/16

Bugs:
Report on above forum, or http://trac.i2p2.i2p/ or https://trac.i2p2.de/

License: Apache 2
