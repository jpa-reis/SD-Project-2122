rm -f *.jks

keytool -genkey -alias files -keyalg RSA -validity 365 -keystore ./files.jks -storetype pkcs12 << EOF
tp2pass
tp2pass
Files.Files
TP2
SD2021
LX
LX
PT
yes
EOF

keytool -exportcert -alias files -keystore files.jks -file files.cert << EOF
tp2pass
EOF

keytool -genkey -alias proxyFiles -keyalg RSA -validity 365 -keystore ./proxyFiles.jks -storetype pkcs12 << EOF
tp2pass
tp2pass
Files.Files
TP2
SD2021
LX
LX
PT
yes
EOF

cp cacerts client-ts.jks
keytool -exportcert -alias proxyFiles -keystore proxyFiles.jks -file proxyFiles.cert << EOF
tp2pass
EOF

keytool -genkey -alias users -keyalg RSA -validity 365 -keystore ./users.jks -storetype pkcs12 << EOF
tp2pass
tp2pass
Files.Files
TP2
SD2021
LX
LX
PT
yes
EOF

keytool -exportcert -alias users -keystore users.jks -file users.cert << EOF
tp2pass
EOF

keytool -genkey -alias directories -keyalg RSA -validity 365 -keystore ./directories.jks -storetype pkcs12 << EOF
tp2pass
tp2pass
Files.Files
TP2
SD2021
LX
LX
PT
yes
EOF

keytool -exportcert -alias directories -keystore directories.jks -file directories.cert << EOF
tp2pass
EOF

cp cacerts client-ts.jks
keytool -importcert -file users.cert -alias users -keystore client-ts.jks << EOF
changeit
yes
EOF
keytool -importcert -file files.cert -alias files -keystore client-ts.jks << EOF
changeit
yes
EOF
keytool -importcert -file proxyFiles.cert -alias proxyFiles -keystore client-ts.jks << EOF
changeit
yes
EOF
keytool -importcert -file directories.cert -alias directories -keystore client-ts.jks << EOF
changeit
yes
EOF