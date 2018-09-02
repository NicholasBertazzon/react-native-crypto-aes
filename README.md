
# React Native AES
## Currently Android only, since I don't own a Mac. Still no pbkdf2 support.

## Getting started

`$ npm install react-native-crypto-aes --save`

### Mostly automatic installation

`$ react-native link react-native-crypto-aes`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import it.nicholasbertazzon.crypto.aes.RNCryptoAesPackage;` to the imports at the top of the file
  - Add `new RNCryptoAesPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-crypto-aes'
  	project(':react-native-crypto-aes').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-crypto-aes/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      implementation project(':react-native-crypto-aes')
  	```

## Usage (API)

### **hashKey**(params)
Hash the given key, in order to be used for encrypt and decrypt.
<br/>Returns a base64 hashed key, see the example.

**Params (object):**
- **key (required)**: standard utf-8 text
- **algorithm**: default to SHA-256

### **encrypt**(params)
Encrypt the given text.
<br/>Returns an object with the encrypted text and the initial vector, both encoded in base64, see the example.

**Params (object):**
- **key (required) (base64)**
- **text (required)**: standard utf-8 text
- **algorithm**: default to AES/CBC/PKCS5Padding

### **decrypt**(params)
Decrypt the given encrypted text.
<br/>Returns the decrypted text.

**Params (object):**
- **key (required) (base64)**
- **encrypted (required) (base64)**: the encrypted text
- **iv (required) (base64)**: iv given by the encrypt function
- **algorithm**: default to AES/CBC/PKCS5Padding

### Example
```javascript
import AES from 'react-native-react-native-crypto-aes';

AES.hashKey({
            key: "My key"
        }).then(key => {
            AES.encrypt({
                key: key,
                text: "Text to encrypt"
            }).then(response => {
                AES.decrypt({
                    key: key,
                    iv: response.iv,
                    encrypted: response.encrypted
                }).then(text => alert(text));
            });
        });
```
  